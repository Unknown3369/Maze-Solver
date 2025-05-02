package com.example.mazesolver;

import java.util.*;

public class AStarSolver {

    private final MazeView.Cell[][] grid;
    private final int cols;
    private final int rows;

    public AStarSolver(MazeView.Cell[][] grid, int cols, int rows) {
        this.grid = grid;
        this.cols = cols;
        this.rows = rows;
    }

    public List<MazeView.Cell> solve() {
        Map<MazeView.Cell, MazeView.Cell> cameFrom = new HashMap<>();
        Map<MazeView.Cell, Integer> gScore = new HashMap<>();
        Map<MazeView.Cell, Integer> fScore = new HashMap<>();

        MazeView.Cell start = grid[0][0];
        MazeView.Cell goal = grid[cols - 1][rows - 1];

        Comparator<MazeView.Cell> comparator = Comparator.comparingInt(fScore::get);
        PriorityQueue<MazeView.Cell> openSet = new PriorityQueue<>(comparator);

        gScore.put(start, 0);
        fScore.put(start, heuristic(start, goal));
        openSet.add(start);

        Set<MazeView.Cell> closedSet = new HashSet<>();

        while (!openSet.isEmpty()) {
            MazeView.Cell current = openSet.poll();

            if (current.equals(goal)) break;

            closedSet.add(current);

            for (MazeView.Cell neighbor : getNeighbors(current)) {
                if (closedSet.contains(neighbor)) continue;

                int tentativeG = gScore.getOrDefault(current, Integer.MAX_VALUE) + 1;

                if (tentativeG < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeG);
                    fScore.put(neighbor, tentativeG + heuristic(neighbor, goal));
                    if (!openSet.contains(neighbor)) openSet.add(neighbor);
                }
            }
        }

        // Reconstruct path
        List<MazeView.Cell> path = new ArrayList<>();
        MazeView.Cell step = goal;
        while (step != null && cameFrom.containsKey(step)) {
            path.add(step);
            step = cameFrom.get(step);
        }
        path.add(start);
        Collections.reverse(path);
        return path;
    }

    private int heuristic(MazeView.Cell a, MazeView.Cell b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y); // Manhattan distance
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
