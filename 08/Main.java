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
        // Find all .vm files in directory
        List<Path> vmFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, "*.vm")) {
            for (Path entry: stream) {
                vmFiles.add(entry);
            }
        }

        if (vmFiles.isEmpty()) {
            System.err.println("No .vm files found in directory");
            return;
        }

        // Create output file with same name as directory
        String outputFileName = dirPath.getFileName().toString() + ".asm";
        Path outputPath = dirPath.resolve(outputFileName);

        VMTranslator translator = new VMTranslator(vmFiles, outputPath);
        translator.run();
    }

    private static void processSingleFile(Path filePath) throws IOException {
        if (!filePath.toString().endsWith(".vm")) {
            throw new IllegalArgumentException("Input file must have a .vm extension");
        }

        VMTranslator translator = new VMTranslator(filePath);
        translator.run();
    }
}
