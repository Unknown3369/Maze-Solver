package com.example.mazesolver;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private MazeView mazeView;
    private List<List<MazeView.Cell>> allPaths = new ArrayList<>();
    private int currentPathIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mazeView = findViewById(R.id.mazeView);
        Button solveButton = findViewById(R.id.solveButton);
        Button resetButton = findViewById(R.id.resetButton);
        Button saveButton = findViewById(R.id.saveButton);
        Button loadButton = findViewById(R.id.loadButton);
        Button deleteButton = findViewById(R.id.deleteButton);
        Button nextPathButton = findViewById(R.id.nextPathButton);
        Spinner algorithmSelector = findViewById(R.id.algorithmSelector);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Dijkstra", "A*"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        algorithmSelector.setAdapter(adapter);

        solveButton.setOnClickListener(v -> {
            String algo = algorithmSelector.getSelectedItem().toString();
            List<MazeView.Cell> path;
            Set<MazeView.Cell> visited;

            if (algo.equals("A*")) {
                AStarSolver solver = new AStarSolver(mazeView.getGrid(), mazeView.getCols(), mazeView.getRows());
                AStarSolver.Result result = solver.solve();
                path = result.path;
                visited = result.visited;
            } else {
                DijkstraSolver solver = new DijkstraSolver(mazeView.getGrid(), mazeView.getCols(), mazeView.getRows());
                DijkstraSolver.Result result = solver.solve();
                path = result.path;
                visited = result.visited;
            }

            mazeView.animateSolvedPath(path, visited);
        });

        resetButton.setOnClickListener(v -> {
            mazeView.resetMaze();
            allPaths.clear();
            currentPathIndex = 0;
        });

        saveButton.setOnClickListener(v -> {
            EditText input = new EditText(this);
            new AlertDialog.Builder(this)
                    .setTitle("Save Maze As")
                    .setMessage("Enter a name for this maze:")
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String name = input.getText().toString().trim();
                        if (!name.isEmpty()) {
                            mazeView.saveMaze(this, name);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        loadButton.setOnClickListener(v -> {
            Set<String> saved = MazeView.getSavedMazeNames(this);
            if (saved.isEmpty()) {
                Toast.makeText(this, "No saved mazes.", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] mazeNames = saved.toArray(new String[0]);
            new AlertDialog.Builder(this)
                    .setTitle("Load Maze")
                    .setItems(mazeNames, (dialog, which) -> mazeView.loadMaze(this, mazeNames[which]))
                    .show();
        });

        deleteButton.setOnClickListener(v -> {
            Set<String> saved = MazeView.getSavedMazeNames(this);
            if (saved.isEmpty()) {
                Toast.makeText(this, "No saved mazes to delete.", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] mazeNames = saved.toArray(new String[0]);
            new AlertDialog.Builder(this)
                    .setTitle("Delete Maze")
                    .setItems(mazeNames, (dialog, which) -> {
                        MazeView.deleteMaze(this, mazeNames[which]);
                        Toast.makeText(this, "Deleted: " + mazeNames[which], Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });

        nextPathButton.setOnClickListener(v -> {
            if (allPaths.isEmpty()) {
                MultiplePathSolver solver = new MultiplePathSolver(mazeView.getGrid(), mazeView.getCols(), mazeView.getRows());
                allPaths = solver.findAllPaths();
                currentPathIndex = 0;
            }
            if (!allPaths.isEmpty()) {
                mazeView.setSolvedPath(allPaths.get(currentPathIndex));
                Toast.makeText(this, "Path " + (currentPathIndex + 1) + "/" + allPaths.size(), Toast.LENGTH_SHORT).show();
                currentPathIndex = (currentPathIndex + 1) % allPaths.size();
            } else {
                Toast.makeText(this, "No paths found!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
