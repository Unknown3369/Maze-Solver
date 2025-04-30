package com.sagan.astar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Random;
import java.util.Comparator;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JFrame;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;


public class AStarPathfinding extends JPanel {
    private static final int COLS = 60;
    private static final int ROWS = 60;
    private static final int CELL_SIZE = 10;
    
    private Spot[][] grid = new Spot[COLS][ROWS];
    private Spot start, end;
    private java.util.List<Spot> openSet = new ArrayList<>();
    private java.util.List<Spot> closedSet = new ArrayList<>();
    private java.util.List<Spot> path = new ArrayList<>();
    
    public AStarPathfinding() {
        setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        generateGrid();
        generateMaze();
        openSet.add(start);
        new javax.swing.Timer(30, e -> aStarStep()).start();  // Animation timer
    }

    // Spot (Cell) Class
    class Spot {
        int i, j;
        double f, g, h;
        java.util.List<Spot> neighbors = new ArrayList<>();

        Spot previous = null;
        boolean wall = false;

        Spot(int i, int j) {
            this.i = i;
            this.j = j;
        }

        void addNeighbors() {
            if (i < COLS - 1) neighbors.add(grid[i + 1][j]);
            if (i > 0) neighbors.add(grid[i - 1][j]);
            if (j < ROWS - 1) neighbors.add(grid[i][j + 1]);
            if (j > 0) neighbors.add(grid[i][j - 1]);
        }

        void draw(Graphics g) {
            if (this == start) {
                g.setColor(Color.GREEN); // Start is Green
            } else if (this == end) {
                g.setColor(Color.RED); // End is Red
            } else if (wall) {
                g.setColor(Color.BLACK); // Walls are Black
            } else {
                g.setColor(Color.WHITE);
            }
            g.fillRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            g.setColor(Color.GRAY);
            g.drawRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
    }

    // Generate Grid and Neighbors
    private void generateGrid() {
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                grid[i][j] = new Spot(i, j);
            }
        }
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                grid[i][j].addNeighbors();
            }
        }

        // Set Random Start and End
        Random rand = new Random();
        int startX = rand.nextInt(COLS), startY = rand.nextInt(ROWS);
        int endX, endY;
        do {
            endX = rand.nextInt(COLS);
            endY = rand.nextInt(ROWS);
        } while (startX == endX && startY == endY);

        //start = grid[startX][startY];
        start = grid[0][0];
        end = grid[endX][endY];
    }

    // Ensure there is a valid path
    private boolean isPathExists() {
        Queue<Spot> queue = new LinkedList<>();
        Set<Spot> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Spot current = queue.poll();
            if (current == end) return true;
            for (Spot neighbor : current.neighbors) {
                if (!visited.contains(neighbor) && !neighbor.wall) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return false;
    }

    // Generate Maze with Walls while ensuring a path exists
    private void generateMaze() {
        Random rand = new Random();
        do {
            for (int i = 0; i < COLS; i++) {
                for (int j = 0; j < ROWS; j++) {
                    if (grid[i][j] != start && grid[i][j] != end) {
                        grid[i][j].wall = rand.nextDouble() < 0.45; // 30% chance of wall
                    } else {
                        grid[i][j].wall = false;
                    }
                }
            }
        } while (!isPathExists()); // Regenerate until there's a valid path
    }

    // A* Algorithm Step-by-Step
    private void aStarStep() {
        if (openSet.isEmpty()) {
            repaint();
            return;
        }

        // Find the node with the lowest f value
        openSet.sort(Comparator.comparingDouble(s -> s.f));
        Spot current = openSet.get(0);

        if (current == end) {
            path.clear();
            while (current != null) {
                path.add(current);
                current = current.previous;
            }
            repaint();
            return;
        }

        openSet.remove(current);
        closedSet.add(current);

        for (Spot neighbor : current.neighbors) {
            if (closedSet.contains(neighbor) || neighbor.wall) continue;

            double tempG = current.g + 1; // Assuming uniform cost

            if (!openSet.contains(neighbor)) {
                neighbor.g = tempG;
                neighbor.h = heuristic(neighbor, end);
                neighbor.f = neighbor.g + neighbor.h;
                neighbor.previous = current;
                openSet.add(neighbor);
            } else if (tempG < neighbor.g) {
                neighbor.g = tempG;
                neighbor.previous = current;
                neighbor.f = neighbor.g + neighbor.h;
            }
        }

        repaint();
    }

    // Heuristic (Euclidean Distance)
    private double heuristic(Spot a, Spot b) {
        return Math.sqrt(Math.pow(a.i - b.i, 2) + Math.pow(a.j - b.j, 2));
    }

    // Paint the grid
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                grid[i][j].draw(g);
            }
        }

        // Draw Closed Set (Red)
        g.setColor(new Color(255, 0, 0, 100));
        for (Spot s : closedSet) {
            g.fillRect(s.i * CELL_SIZE, s.j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }

        // Draw Open Set (Blue)
        g.setColor(new Color(0, 0, 255, 100));
        for (Spot s : openSet) {
            g.fillRect(s.i * CELL_SIZE, s.j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }

        // Draw Path (Yellow)
        g.setColor(Color.YELLOW);
        for (Spot s : path) {
            g.fillRect(s.i * CELL_SIZE, s.j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
    }

    // Main Method
    public static void main(String[] args) {
        JFrame frame = new JFrame("A* Pathfinding with Walls");
        AStarPathfinding panel = new AStarPathfinding();
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}