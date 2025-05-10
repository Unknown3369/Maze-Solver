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

    public static class Result {
        public final List<MazeView.Cell> path;
        public final Set<MazeView.Cell> visited;

        public Result(List<MazeView.Cell> path, Set<MazeView.Cell> visited) {
            this.path = path;
            this.visited = visited;
        }
    }

    public Result solve() {
        Set<MazeView.Cell> visited = new HashSet<>();
        Map<MazeView.Cell, MazeView.Cell> cameFrom = new HashMap<>();
        Map<MazeView.Cell, Integer> gScore = new HashMap<>();

        PriorityQueue<MazeView.Cell> openSet = new PriorityQueue<>(Comparator.comparingInt(c -> gScore.getOrDefault(c, Integer.MAX_VALUE) + heuristic(c)));

        MazeView.Cell start = grid[0][0];
        MazeView.Cell goal = grid[cols - 1][rows - 1];

        gScore.put(start, 0);
        openSet.add(start);

        while (!openSet.isEmpty()) {
            MazeView.Cell current = openSet.poll();
            visited.add(current);

            if (current.equals(goal)) break;

            for (MazeView.Cell neighbor : getNeighbors(current)) {
                int tentativeGScore = gScore.getOrDefault(current, Integer.MAX_VALUE) + 1;
                if (tentativeGScore < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        List<MazeView.Cell> path = reconstructPath(cameFrom, goal);
        return new Result(path, visited);
    }

    private int heuristic(MazeView.Cell cell) {
        return Math.abs(cell.x - (cols - 1)) + Math.abs(cell.y - (rows - 1));
    }

    private List<MazeView.Cell> reconstructPath(Map<MazeView.Cell, MazeView.Cell> cameFrom, MazeView.Cell goal) {
        List<MazeView.Cell> path = new ArrayList<>();
        MazeView.Cell current = goal;

        while (current != null && cameFrom.containsKey(current)) {
            path.add(current);
            current = cameFrom.get(current);
        }

        path.add(grid[0][0]);
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
