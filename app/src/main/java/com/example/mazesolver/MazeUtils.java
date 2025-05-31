package com.example.mazesolver;

import android.content.Context;
import java.io.*;
import java.util.*;

public class MazeUtils {

    // Save maze grid to a file
    public static void saveMaze(Context context, String filename, MazeView.Cell[][] grid) {
        try (ObjectOutputStream out = new ObjectOutputStream(context.openFileOutput(filename, Context.MODE_PRIVATE))) {
            out.writeObject(grid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load maze grid from a file
    public static MazeView.Cell[][] loadMaze(Context context, String filename) {
        try (ObjectInputStream in = new ObjectInputStream(context.openFileInput(filename))) {
            return (MazeView.Cell[][]) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // List all saved maze files ending with .maze
    public static List<String> getSavedMazeNames(Context context) {
        File dir = context.getFilesDir();
        File[] files = dir.listFiles();
        List<String> names = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".maze")) {
                    names.add(file.getName());
                }
            }
        }
        return names;
    }

    // Delete a specific maze file
    public static boolean deleteMaze(Context context, String filename) {
        File file = new File(context.getFilesDir(), filename);
        return file.exists() && file.delete();
    }
}
