package com.example.mazesolver;

import java.util.*;

public class MultiplePathSolver {

    private final MazeView.Cell[][] grid;
    private final int cols;
    private final int rows;
    private final List<List<MazeView.Cell>> allPaths = new ArrayList<>();

    public MultiplePathSolver(MazeView.Cell[][] grid, int cols, int rows) {
        this.grid = grid;
        this.cols = cols;
        this.rows = rows;
    }

    public List<List<MazeView.Cell>> findAllPaths() {
        MazeView.Cell start = grid[0][0];
        MazeView.Cell goal = grid[cols - 1][rows - 1];

        Set<MazeView.Cell> visited = new HashSet<>();
        LinkedList<MazeView.Cell> currentPath = new LinkedList<>();

        dfs(start, goal, visited, currentPath);

        return allPaths;
    }

    private void dfs(MazeView.Cell current, MazeView.Cell goal, Set<MazeView.Cell> visited, LinkedList<MazeView.Cell> path) {
        if (visited.contains(current)) return;

        visited.add(current);
        path.add(current);

        if (current.equals(goal)) {
            allPaths.add(new ArrayList<>(path));
        } else {
            for (MazeView.Cell neighbor : getNeighbors(current)) {
                dfs(neighbor, goal, visited, path);
            }
        }

        visited.remove(current);
        path.removeLast();
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
