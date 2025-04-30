package com.example.mazesolver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.*;
import android.os.Handler;
import android.os.Looper;
import java.io.*;



public class MazeView extends View {

    private List<Cell> animationPath = null;
    private int animationIndex = 0;
    private Handler animationHandler = new Handler(Looper.getMainLooper());
    private int rows = 10;
    private int cols = 10;
    private int cellSize;
    private Cell[][] cells;

    private Paint wallPaint, playerPaint, goalPaint, pathPaint;

    private int playerX = 0, playerY = 0;

    private List<Cell> solvedPath = null;

    private GestureDetector gestureDetector;

    // Constructor for Java (when added programmatically)
    public MazeView(Context context) {
        super(context);
        init(context);
    }

    // Constructor required for XML inflation (Fixes your crash!)
    public MazeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    // Shared initialization logic
    private void init(Context context) {
        wallPaint = new Paint();
        wallPaint.setColor(Color.WHITE);
        wallPaint.setStrokeWidth(5);

        playerPaint = new Paint();
        playerPaint.setColor(Color.BLUE);

        goalPaint = new Paint();
        goalPaint.setColor(Color.GREEN);

        pathPaint = new Paint();
        pathPaint.setColor(Color.CYAN);
        pathPaint.setStrokeWidth(10);

        generateMaze();

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 50;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();

                if (Math.abs(dx) > Math.abs(dy)) {
                    if (dx > SWIPE_THRESHOLD) movePlayer(1, 0); // right
                    else if (dx < -SWIPE_THRESHOLD) movePlayer(-1, 0); // left
                } else {
                    if (dy > SWIPE_THRESHOLD) movePlayer(0, 1); // down
                    else if (dy < -SWIPE_THRESHOLD) movePlayer(0, -1); // up
                }
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private void movePlayer(int dx, int dy) {
        int newX = playerX + dx;
        int newY = playerY + dy;

        if (newX < 0 || newY < 0 || newX >= cols || newY >= rows) return;

        Cell current = cells[playerX][playerY];
        Cell next = cells[newX][newY];

        if (dx == -1 && !current.leftWall) playerX--;
        else if (dx == 1 && !current.rightWall) playerX++;
        else if (dy == -1 && !current.topWall) playerY--;
        else if (dy == 1 && !current.bottomWall) playerY++;

        invalidate();

        if (playerX == cols - 1 && playerY == rows - 1) {
            android.widget.Toast.makeText(getContext(), "🎉 You reached the goal!", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        cellSize = getWidth() / cols;

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                Cell cell = cells[x][y];

                int left = x * cellSize;
                int top = y * cellSize;
                int right = left + cellSize;
                int bottom = top + cellSize;

                if (cell.topWall)
                    canvas.drawLine(left, top, right, top, wallPaint);
                if (cell.leftWall)
                    canvas.drawLine(left, top, left, bottom, wallPaint);
                if (cell.bottomWall)
                    canvas.drawLine(left, bottom, right, bottom, wallPaint);
                if (cell.rightWall)
                    canvas.drawLine(right, top, right, bottom, wallPaint);
            }
        }

        // Draw solved path if available
        if (solvedPath != null) {
            for (Cell cell : solvedPath) {
                float left = cell.x * cellSize + 10;
                float top = cell.y * cellSize + 10;
                float right = left + cellSize - 20;
                float bottom = top + cellSize - 20;
                canvas.drawRect(left, top, right, bottom, pathPaint);
            }
        }

        // Draw player
        float cx = playerX * cellSize + cellSize / 2f;
        float cy = playerY * cellSize + cellSize / 2f;
        canvas.drawCircle(cx, cy, cellSize / 3f, playerPaint);

        // Draw goal
        float gx = (cols - 1) * cellSize + cellSize / 2f;
        float gy = (rows - 1) * cellSize + cellSize / 2f;
        canvas.drawCircle(gx, gy, cellSize / 3f, goalPaint);
    }

    public void setSolvedPath(List<Cell> path) {
        this.solvedPath = path;
        invalidate();
    }

    public Cell[][] getGrid() {
        return cells;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    private void generateMaze() {
        cells = new Cell[cols][rows];

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                cells[x][y] = new Cell(x, y);
            }
        }

        Stack<Cell> stack = new Stack<>();
        Cell start = cells[0][0];
        start.visited = true;
        stack.push(start);

        while (!stack.isEmpty()) {
            Cell current = stack.peek();
            Cell next = getUnvisitedNeighbor(current);

            if (next != null) {
                next.visited = true;
                removeWalls(current, next);
                stack.push(next);
            } else {
                stack.pop();
            }
        }
    }

    private Cell getUnvisitedNeighbor(Cell cell) {
        ArrayList<Cell> neighbors = new ArrayList<>();
        int x = cell.x;
        int y = cell.y;

        if (x > 0 && !cells[x - 1][y].visited)
            neighbors.add(cells[x - 1][y]);
        if (x < cols - 1 && !cells[x + 1][y].visited)
            neighbors.add(cells[x + 1][y]);
        if (y > 0 && !cells[x][y - 1].visited)
            neighbors.add(cells[x][y - 1]);
        if (y < rows - 1 && !cells[x][y + 1].visited)
            neighbors.add(cells[x][y + 1]);

        if (!neighbors.isEmpty()) {
            Collections.shuffle(neighbors, new Random());
            return neighbors.get(0);
        }
        return null;
    }

    private void removeWalls(Cell a, Cell b) {
        int dx = a.x - b.x;
        int dy = a.y - b.y;

        if (dx == 1) {
            a.leftWall = false;
            b.rightWall = false;
        } else if (dx == -1) {
            a.rightWall = false;
            b.leftWall = false;
        }

        if (dy == 1) {
            a.topWall = false;
            b.bottomWall = false;
        } else if (dy == -1) {
            a.bottomWall = false;
            b.topWall = false;
        }
    }

    public static class Cell {
        int x, y;
        boolean topWall = true;
        boolean bottomWall = true;
        boolean leftWall = true;
        boolean rightWall = true;
        boolean visited = false;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public boolean isWall() {
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Cell) {
                Cell other = (Cell) obj;
                return this.x == other.x && this.y == other.y;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
    public void resetMaze() {
        generateMaze();        // Regenerate the maze
        playerX = 0;           // Reset player position
        playerY = 0;
        solvedPath = null;     // Clear previous path
        invalidate();          // Redraw
    }
    public void animateSolvedPath(List<Cell> path) {
        animationPath = path;
        animationIndex = 0;

        animationHandler.removeCallbacksAndMessages(null); // Clear old animations

        animationHandler.post(new Runnable() {
            @Override
            public void run() {
                if (animationPath != null && animationIndex < animationPath.size()) {
                    solvedPath = animationPath.subList(0, animationIndex + 1);
                    animationIndex++;
                    invalidate();
                    animationHandler.postDelayed(this, 100); // speed: 100ms per step
                }
            }
        });
    }


    public void saveMaze(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput("saved_maze.txt", Context.MODE_PRIVATE);
            StringBuilder sb = new StringBuilder();

            for (int x = 0; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
                    Cell c = cells[x][y];
                    sb.append(c.x).append(",").append(c.y).append(",")
                            .append(c.topWall ? 1 : 0).append(",")
                            .append(c.bottomWall ? 1 : 0).append(",")
                            .append(c.leftWall ? 1 : 0).append(",")
                            .append(c.rightWall ? 1 : 0).append("\n");
                }
            }

            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMaze(Context context) {
        try {
            FileInputStream fis = context.openFileInput("saved_maze.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;

            cells = new Cell[cols][rows];

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);

                Cell c = new Cell(x, y);
                c.topWall = parts[2].equals("1");
                c.bottomWall = parts[3].equals("1");
                c.leftWall = parts[4].equals("1");
                c.rightWall = parts[5].equals("1");

                cells[x][y] = c;
            }

            playerX = 0;
            playerY = 0;
            solvedPath = null;
            invalidate();

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
