package com.example.mazesolver;

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
        SeekBar speedSlider = findViewById(R.id.speedSlider);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Dijkstra", "A*"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        algorithmSelector.setAdapter(adapter);

        solveButton.setOnClickListener(v -> {
            String selectedAlgo = algorithmSelector.getSelectedItem().toString();
            List<MazeView.Cell> path;

            if (selectedAlgo.equals("A*")) {
                AStarSolver solver = new AStarSolver(mazeView.getGrid(), mazeView.getCols(), mazeView.getRows());
                path = solver.solve();
            } else {
                DijkstraSolver solver = new DijkstraSolver(mazeView.getGrid(), mazeView.getCols(), mazeView.getRows());
                path = solver.solve();
            }
            mazeView.animateSolvedPath(path);
        });

        resetButton.setOnClickListener(v -> {
            mazeView.resetMaze();
            allPaths.clear();
            currentPathIndex = 0;
        });

        saveButton.setOnClickListener(v -> mazeView.saveMaze(getApplicationContext()));
        loadButton.setOnClickListener(v -> mazeView.loadMaze(getApplicationContext()));

        nextPathButton.setOnClickListener(v -> {
            if (allPaths.isEmpty()) {
                MultiplePathSolver solver = new MultiplePathSolver(mazeView.getGrid(), mazeView.getCols(), mazeView.getRows());
                allPaths = solver.findAllPaths();
                currentPathIndex = 0;
            }
            if (!allPaths.isEmpty()) {
                mazeView.animateSolvedPath(allPaths.get(currentPathIndex));
                Toast.makeText(this, "Showing path " + (currentPathIndex + 1) + "/" + allPaths.size(), Toast.LENGTH_SHORT).show();
                currentPathIndex = (currentPathIndex + 1) % allPaths.size();
            } else {
                Toast.makeText(this, "No paths found!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
