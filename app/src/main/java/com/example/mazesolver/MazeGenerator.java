package com.example.mazesolver;

import java.util.*;

public class MazeGenerator {
    private final int rows, cols;
    private final int[][] maze;
    private final boolean[][] visited;

    public MazeGenerator(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.maze = new int[rows][cols];
        this.visited = new boolean[rows][cols];
        generateMaze(0, 0);
    }

    public int[][] getMaze() {
        return maze;
    }

    private void generateMaze(int r, int c) {
        visited[r][c] = true;

        int[][] directions = {
                {0, -1},  // left
                {-1, 0},  // up
                {0, 1},   // right
                {1, 0}    // down
        };

        Collections.shuffle(Arrays.asList(directions), new Random());

        for (int[] dir : directions) {
            int newRow = r + dir[0] * 2;
            int newCol = c + dir[1] * 2;

            if (isInBounds(newRow, newCol) && !visited[newRow][newCol]) {
                maze[r + dir[0]][c + dir[1]] = 1;
                generateMaze(newRow, newCol);
            }
        }
    }

    private boolean isInBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }
}
