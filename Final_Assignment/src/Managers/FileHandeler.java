// DataManager.java
package Managers;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandeler {
    private static final String FILE_DIR = "data/";

    // Ensure data directory exists
    static {
        File directory = new File(FILE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    // Read all lines from a file
    public static List<String> readFile(String fileName) {
        List<String> lines = new ArrayList<>();
        File file = new File(FILE_DIR + fileName);

        // Create file if it doesn't exist
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {  // Skip empty lines
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    // Write all lines to a file (overwrite)
    public static void writeFile(String fileName, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_DIR + fileName))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Append a single line to a file
    public static void appendToFile(String fileName, String line) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_DIR + fileName, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Delete a file
    public static boolean deleteFile(String fileName) {
        File file = new File(FILE_DIR + fileName);
        return file.delete();
    }

    // Clear all contents of a file
    public static void clearFile(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_DIR + fileName))) {
            writer.write("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Check if a file exists
    public static boolean fileExists(String fileName) {
        File file = new File(FILE_DIR + fileName);
        return file.exists();
    }

    // Get the number of lines in a file
    public static int getLineCount(String fileName) {
        return readFile(fileName).size();
    }

    // Read specific line from file
    public static String readLine(String fileName, int lineNumber) {
        List<String> lines = readFile(fileName);
        if (lineNumber >= 0 && lineNumber < lines.size()) {
            return lines.get(lineNumber);
        }
        return null;
    }

    // Update specific line in file
    public static boolean updateLine(String fileName, int lineNumber, String newContent) {
        List<String> lines = readFile(fileName);
        if (lineNumber >= 0 && lineNumber < lines.size()) {
            lines.set(lineNumber, newContent);
            writeFile(fileName, lines);
            return true;
        }
        return false;
    }

    // Delete specific line from file
    public static boolean deleteLine(String fileName, int lineNumber) {
        List<String> lines = readFile(fileName);
        if (lineNumber >= 0 && lineNumber < lines.size()) {
            lines.remove(lineNumber);
            writeFile(fileName, lines);
            return true;
        }
        return false;
    }
}