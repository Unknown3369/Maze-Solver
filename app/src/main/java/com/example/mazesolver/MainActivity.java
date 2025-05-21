package com.example.mazesolver;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
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
        Button nextPathButton = findViewById(R.id.nextPathButton);
        Spinner algorithmSelector = findViewById(R.id.algorithmSelector);
//        SeekBar speedSlider = findViewById(R.id.speedSlider);

        // Populate algorithm choices
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Dijkstra", "A*"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        algorithmSelector.setAdapter(adapter);

        // Solve Maze
        solveButton.setOnClickListener(v -> {
            String selectedAlgo = algorithmSelector.getSelectedItem().toString();

            if (selectedAlgo.equals("A*")) {
                AStarSolver solver = new AStarSolver(
                        mazeView.getGrid(), mazeView.getCols(), mazeView.getRows());
                AStarSolver.Result result = solver.solve();
                mazeView.animateSolvedPath(result.path, result.visited);
            } else {
                DijkstraSolver solver = new DijkstraSolver(
                        mazeView.getGrid(), mazeView.getCols(), mazeView.getRows());
                DijkstraSolver.Result result = solver.solve();
                mazeView.animateSolvedPath(result.path, result.visited);
            }
        });

        // Reset maze
        resetButton.setOnClickListener(v -> {
            mazeView.resetMaze();
            allPaths.clear();
            currentPathIndex = 0;
        });

        // Save maze button
        saveButton.setOnClickListener(v -> {
            EditText input = new EditText(this);
            new AlertDialog.Builder(this)
                    .setTitle("Save Maze As")
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

        // Load maze button
        loadButton.setOnClickListener(v -> {
            Set<String> names = MazeView.getSavedMazeNames(this);
            if (names.isEmpty()) {
                Toast.makeText(this, "No saved mazes found", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] nameArray = names.toArray(new String[0]);
            new AlertDialog.Builder(this)
                    .setTitle("Load Maze")
                    .setItems(nameArray, (dialog, which) -> {
                        String selected = nameArray[which];
                        mazeView.loadMaze(this, selected);
                    })
                    .show();
        });

        //Delete maze Button
        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            EditText input = new EditText(this);
            new AlertDialog.Builder(this)
                    .setTitle("Delete Maze")
                    .setView(input)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        String name = input.getText().toString().trim();
                        if (!name.isEmpty()) {
                            mazeView.deleteMaze(this, name);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });


        // Show multiple paths one-by-one
        nextPathButton.setOnClickListener(v -> {
            if (allPaths.isEmpty()) {
                MultiplePathSolver solver = new MultiplePathSolver(
                        mazeView.getGrid(), mazeView.getCols(), mazeView.getRows());
                allPaths = solver.findAllPaths();
                currentPathIndex = 0;
            }

            if (!allPaths.isEmpty()) {
                List<MazeView.Cell> path = allPaths.get(currentPathIndex);
                mazeView.setSolvedPath(path);
                Toast.makeText(this,
                        "Showing path " + (currentPathIndex + 1) + "/" + allPaths.size(),
                        Toast.LENGTH_SHORT).show();
                currentPathIndex = (currentPathIndex + 1) % allPaths.size();
            } else {
                Toast.makeText(this, "No paths found!", Toast.LENGTH_SHORT).show();
            }
        });

        // Set animation speed
//        speedSlider.setMax(200);
//        speedSlider.setProgress(50); // default
//        speedSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                mazeView.setAnimationSpeed(Math.max(10, progress));
//            }
//            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
//            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
//        });
    }
}
