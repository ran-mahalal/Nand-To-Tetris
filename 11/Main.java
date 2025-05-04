import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Error: Path not provided"); 
            return;
        }

        Path path = Paths.get(args[0]);
        try {
            if (Files.isDirectory(path)) {
                processDirectory(path);
            } else {
                processSingleFile(path);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void processDirectory(Path dirPath) throws IOException {
        List<Path> jackFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, "*.jack")) {
            for (Path entry: stream) {
                jackFiles.add(entry);
            }
        }
    
        if (jackFiles.isEmpty()) {
            System.err.println("No .jack files found in directory: " + dirPath);
            return;
        }
    
        boolean hasErrors = false;
        for (Path jackFile : jackFiles) {
            try {
                processSingleFile(jackFile);
            } catch (IOException e) {
                hasErrors = true;
                // Continue processing other files even if one fails
            }
        }
    
        if (hasErrors) {
            throw new IOException("One or more files failed to compile");
        }
    }

    private static void processSingleFile(Path filePath) throws IOException {
        if (!filePath.toString().endsWith(".jack")) {
            throw new IllegalArgumentException("Input file must have a .jack extension");
        }
    
        String vmPath = filePath.toString().replace(".jack", ".vm");
        CompilationEngine engine = null;
        try {
            engine = new CompilationEngine(filePath.toString(), vmPath);
            engine.compileClass();
        } catch (IOException e) {
            System.err.println("Error compiling " + filePath.getFileName() + ": " + e.getMessage());
            // Clean up partially written files if there was an error
            try {
                Files.deleteIfExists(Paths.get(vmPath));
            } catch (IOException deleteError) {
                System.err.println("Warning: Could not clean up partial output file: " + vmPath);
            }
            throw e;
        }
    }
}