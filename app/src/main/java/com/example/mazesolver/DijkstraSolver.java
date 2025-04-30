package com.example.mazesolver;

import java.util.*;

public class DijkstraSolver {

    private final MazeView.Cell[][] grid;
    private final int cols;
    private final int rows;

    public DijkstraSolver(MazeView.Cell[][] grid, int cols, int rows) {
        this.grid = grid;
        this.cols = cols;
        this.rows = rows;
    }

    public List<MazeView.Cell> solve() {
        Map<MazeView.Cell, MazeView.Cell> parentMap = new HashMap<>();
        Queue<MazeView.Cell> queue = new LinkedList<>();
        Set<MazeView.Cell> visited = new HashSet<>();

        MazeView.Cell start = grid[0][0];
        MazeView.Cell goal = grid[cols - 1][rows - 1];

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            MazeView.Cell current = queue.poll();

            if (current == goal) {
                break;
            }

            for (MazeView.Cell neighbor : getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        // Reconstruct path
        List<MazeView.Cell> path = new ArrayList<>();
        MazeView.Cell step = goal;
        while (step != null && parentMap.containsKey(step)) {
            path.add(step);
            step = parentMap.get(step);
        }
        path.add(start);
        Collections.reverse(path);
        return path;
    }

    private List<MazeView.Cell> getNeighbors(MazeView.Cell cell) {
        List<MazeView.Cell> neighbors = new ArrayList<>();
        int x = cell.x;
        int y = cell.y;

        if (x > 0 && !cell.leftWall) neighbors.add(grid[x - 1][y]);
        if (x < cols - 1 && !cell.rightWall) neighbors.add(grid[x + 1][y]);
        if (y > 0 && !cell.topWall) neighbors.add(grid[x][y - 1]);
        if (y < rows - 1 && !cell.bottomWall) neighbors.add(grid[x][y + 1]);

        return neighbors;
    }
}
