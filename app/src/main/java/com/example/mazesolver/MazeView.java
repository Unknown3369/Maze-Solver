package com.example.mazesolver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.*;
import java.util.*;

public class MazeView extends View {

    private int cols = 10, rows = 10;
    private final int cellSize = 80;
    private Cell[][] grid;
    private List<List<Cell>> allPaths = new ArrayList<>();
    private List<Cell> solvedPath = new ArrayList<>();
    private Set<Cell> visitedCells = new HashSet<>();
    private final Handler handler = new Handler();
    private int animationSpeed = 50;

    private int playerX = 0, playerY = 0;
    private final List<Cell> playerPath = new ArrayList<>();

    private final Paint wallPaint = new Paint();
    private final Paint paint = new Paint();

    private final GestureDetector gestureDetector;

    public MazeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        wallPaint.setColor(Color.WHITE);
        wallPaint.setStrokeWidth(4);

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 50;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();

                if (Math.abs(dx) > Math.abs(dy)) {
                    if (dx > SWIPE_THRESHOLD) movePlayer(1, 0);
                    else if (dx < -SWIPE_THRESHOLD) movePlayer(-1, 0);
                } else {
                    if (dy > SWIPE_THRESHOLD) movePlayer(0, 1);
                    else if (dy < -SWIPE_THRESHOLD) movePlayer(0, -1);
                }
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });

        initMaze();
    }

    private void initMaze() {
        grid = new Cell[cols][rows];
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                grid[x][y] = new Cell(x, y);
            }
        }
        generateMazeWithExtraPaths(10);
    }
    public void resetMaze() {
        initMaze();
        solvedPath.clear();
        visitedCells.clear();
        allPaths.clear();
        playerX = 0;
        playerY = 0;
        invalidate();
        playerPath.clear();
    }

    public void saveMaze(Context context) {
        try (ObjectOutputStream out = new ObjectOutputStream(context.openFileOutput("maze.dat", Context.MODE_PRIVATE))) {
            out.writeObject(grid);
            Toast.makeText(context, "Maze saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMaze(Context context) {
        try (ObjectInputStream in = new ObjectInputStream(context.openFileInput("maze.dat"))) {
            grid = (Cell[][]) in.readObject();
            invalidate();
            Toast.makeText(context, "Maze loaded", Toast.LENGTH_SHORT).show();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.BLACK);

        // 1. Draw maze walls
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(4);
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                Cell cell = grid[x][y];
                int left = cell.x * cellSize;
                int top = cell.y * cellSize;
                int right = (cell.x + 1) * cellSize;
                int bottom = (cell.y + 1) * cellSize;

                if (cell.topWall) canvas.drawLine(left, top, right, top, paint);
                if (cell.leftWall) canvas.drawLine(left, top, left, bottom, paint);
                if (cell.bottomWall) canvas.drawLine(left, bottom, right, bottom, paint);
                if (cell.rightWall) canvas.drawLine(right, top, right, bottom, paint);
            }
        }

        // 2. Draw visited cells (algorithm exploration)
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.FILL);
        for (Cell cell : visitedCells) {
            float cx = (cell.x + 0.5f) * cellSize;
            float cy = (cell.y + 0.5f) * cellSize;
            canvas.drawCircle(cx, cy, cellSize / 6f, paint);
        }

        // 3. Draw solved path (final answer)
        if (solvedPath != null && !solvedPath.isEmpty()) {
            paint.setColor(Color.CYAN);
            paint.setStrokeWidth(6);
            for (int i = 0; i < solvedPath.size() - 1; i++) {
                Cell c1 = solvedPath.get(i);
                Cell c2 = solvedPath.get(i + 1);
                canvas.drawLine((c1.x + 0.5f) * cellSize, (c1.y + 0.5f) * cellSize,
                        (c2.x + 0.5f) * cellSize, (c2.y + 0.5f) * cellSize, paint);
            }
        }

        // 4. Draw player
        paint.setColor(Color.BLUE);
        float px = (playerX + 0.5f) * cellSize;
        float py = (playerY + 0.5f) * cellSize;
        canvas.drawCircle(px, py, cellSize / 3f, paint);

        // 5. Draw Start and End points
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GREEN);
        canvas.drawCircle(cellSize / 2f, cellSize / 2f, cellSize / 4f, paint);
        paint.setColor(Color.RED);
        canvas.drawCircle((cols - 0.5f) * cellSize, (rows - 0.5f) * cellSize, cellSize / 4f, paint);
    }

    private void movePlayer(int dx, int dy) {
        int newX = playerX + dx;
        int newY = playerY + dy;

        if (newX < 0 || newY < 0 || newX >= cols || newY >= rows) return;

        Cell current = grid[playerX][playerY];
        Cell next = grid[newX][newY];

        boolean moved = false;

        if (dx == -1 && !current.leftWall) {
            playerX--;
            moved = true;
        } else if (dx == 1 && !current.rightWall) {
            playerX++;
            moved = true;
        } else if (dy == -1 && !current.topWall) {
            playerY--;
            moved = true;
        } else if (dy == 1 && !current.bottomWall) {
            playerY++;
            moved = true;
        }

        if (moved) {
            Cell visited = grid[playerX][playerY];
            if (!playerPath.contains(visited)) {
                playerPath.add(visited);
            }

            invalidate();

            if (playerX == cols - 1 && playerY == rows - 1) {
                evaluatePlayerPath(); // compare paths
                Toast.makeText(getContext(), "🎉 You reached the goal!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void evaluatePlayerPath() {
        DijkstraSolver solver = new DijkstraSolver(grid, cols, rows);
        DijkstraSolver.Result result = solver.solve();
        List<Cell> optimalPath = result.path;

        int matchCount = 0;
        int len = Math.min(playerPath.size(), optimalPath.size());

        for (int i = 0; i < len; i++) {
            if (playerPath.get(i).equals(optimalPath.get(i))) {
                matchCount++;
            } else {
                break; // only count consecutive matches from start
            }
        }

        float similarity = (float) matchCount / optimalPath.size();
        int score = Math.round(similarity * 10);

        Toast.makeText(getContext(), "Your score: " + score + "/10", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void animateSolvedPath(List<Cell> path, Set<Cell> visited) {
        this.solvedPath = path;
        this.visitedCells = visited;

        Runnable runnable = new Runnable() {
            int index = 0;

            @Override
            public void run() {
                if (index < path.size()) {
                    invalidate();
                    index++;
                    handler.postDelayed(this, animationSpeed);
                }
            }
        };
        handler.post(runnable);
    }

    public void setAnimationSpeed(int speed) {
        animationSpeed = speed;
    }

    public void setSolvedPath(List<Cell> path) {
        this.solvedPath = path;
        invalidate();
    }

    public void setAllPaths(List<List<Cell>> paths) {
        this.allPaths = paths;
        invalidate();
    }

    public Cell[][] getGrid() {
        return grid;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public static class Cell implements Serializable {
        int x, y;
        boolean topWall = true, bottomWall = true, leftWall = true, rightWall = true;
        boolean visited = false;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Cell)) return false;
            Cell other = (Cell) obj;
            return this.x == other.x && this.y == other.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private void generateMazeRecursiveBacktracking(int x, int y) {
        Cell current = grid[x][y];
        current.visited = true;

        List<int[]> directions = Arrays.asList(
                new int[]{0, -1}, new int[]{-1, 0},
                new int[]{0, 1}, new int[]{1, 0}
        );
        Collections.shuffle(directions);

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            if (nx >= 0 && ny >= 0 && nx < cols && ny < rows && !grid[nx][ny].visited) {
                if (dir[0] == 1) {
                    current.rightWall = false;
                    grid[nx][ny].leftWall = false;
                } else if (dir[0] == -1) {
                    current.leftWall = false;
                    grid[nx][ny].rightWall = false;
                } else if (dir[1] == 1) {
                    current.bottomWall = false;
                    grid[nx][ny].topWall = false;
                } else if (dir[1] == -1) {
                    current.topWall = false;
                    grid[nx][ny].bottomWall = false;
                }
                generateMazeRecursiveBacktracking(nx, ny);
            }
        }
    }

    private void generateMazeWithExtraPaths(int extraPassages) {
        generateMazeRecursiveBacktracking(0, 0);
        Random rand = new Random();

        for (int i = 0; i < extraPassages; i++) {
            int x = rand.nextInt(cols);
            int y = rand.nextInt(rows);
            int direction = rand.nextInt(4);

            Cell current = grid[x][y];
            Cell neighbor;

            switch (direction) {
                case 0:
                    if (x < cols - 1) {
                        neighbor = grid[x + 1][y];
                        current.rightWall = false;
                        neighbor.leftWall = false;
                    }
                    break;
                case 1:
                    if (y < rows - 1) {
                        neighbor = grid[x][y + 1];
                        current.bottomWall = false;
                        neighbor.topWall = false;
                    }
                    break;
                case 2:
                    if (x > 0) {
                        neighbor = grid[x - 1][y];
                        current.leftWall = false;
                        neighbor.rightWall = false;
                    }
                    break;
                case 3:
                    if (y > 0) {
                        neighbor = grid[x][y - 1];
                        current.topWall = false;
                        neighbor.bottomWall = false;
                    }
                    break;
            }
        }
    }
}
