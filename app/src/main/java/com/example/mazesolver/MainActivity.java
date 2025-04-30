package com.example.mazesolver;

import android.os.Bundle;
import android.widget.Button;
import android.os.Handler;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.view.View;


import androidx.appcompat.app.AppCompatActivity;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private MazeView mazeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mazeView = findViewById(R.id.mazeView);

        Button solveButton = findViewById(R.id.solveButton);
        Button resetButton = findViewById(R.id.resetButton);

        solveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DijkstraSolver solver = new DijkstraSolver(
                        mazeView.getGrid(),
                        mazeView.getCols(),
                        mazeView.getRows()
                );
                List<MazeView.Cell> path = solver.solve();
                mazeView.animateSolvedPath(path); // 🎉 Call animation method
                Toast.makeText(MainActivity.this, "Solving...", Toast.LENGTH_SHORT).show();
            }
        });


        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mazeView.resetMaze();
                Toast.makeText(MainActivity.this, "Maze Reset!", Toast.LENGTH_SHORT).show();
            }
        });
        Button saveButton = findViewById(R.id.saveButton);
        Button loadButton = findViewById(R.id.loadButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mazeView.saveMaze(getApplicationContext());
                Toast.makeText(MainActivity.this, "Maze saved!", Toast.LENGTH_SHORT).show();
            }
        });

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mazeView.loadMaze(getApplicationContext());
                Toast.makeText(MainActivity.this, "Maze loaded!", Toast.LENGTH_SHORT).show();
            }
        });

    }


}
