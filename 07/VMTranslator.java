import java.io.*;
import java.nio.file.*;
import java.util.*;

public class VMTranslator {
    private List<Path> inputFiles;
    private Path outputPath;
    private CodeWriter codeWriter;

    // Constructor for single file
    public VMTranslator(Path inputPath) {
        this.inputFiles = Collections.singletonList(inputPath);
        this.outputPath = inputPath.resolveSibling(inputPath.getFileName().toString().replace(".vm", ".asm"));
    }

    // Constructor for directory
    public VMTranslator(List<Path> vmFiles, Path outputPath) {
        this.inputFiles = vmFiles;
        this.outputPath = outputPath;
    }

    private void initialize() throws IOException {
        codeWriter = CodeWriter.getInstance(outputPath.toString());
    }

    private void translate() throws IOException {
        for (Path inputFile : inputFiles) {
            System.out.println("Processing: " + inputFile.getFileName());
            // Create new parser for each file
            try {
                Parser parser = Parser.getInstance(inputFile.toString());
                // Process all commands in this file
                while (parser.hasMoreCommands()) {
                    parser.advance();
                    Parser.CommandType commandType = parser.commandType();
                    
                    // Process each command based on its type
                    if (commandType == Parser.CommandType.C_ARITHMETIC) {
                        codeWriter.writeArithmetic(parser.arg1());
                    } else if (commandType == Parser.CommandType.C_POP || commandType == Parser.CommandType.C_PUSH) {
                        codeWriter.writePushPop(commandType, parser.arg1(), parser.arg2());
                    }
                }
            } catch (IOException e) {
                System.err.println("Error processing file " + inputFile.getFileName() + ": " + e.getMessage());
            }
        }
        System.out.println("\nTranslation complete.");
    }

    private void processCommand(Parser parser) throws IOException {
        Parser.CommandType commandType = parser.commandType();
        if (commandType == Parser.CommandType.C_ARITHMETIC) {
            codeWriter.writeArithmetic(parser.arg1());
        } else if (commandType == Parser.CommandType.C_POP || commandType == Parser.CommandType.C_PUSH) {
            codeWriter.writePushPop(commandType, parser.arg1(), parser.arg2());
        }
    }

    private void close() {
        if (codeWriter != null) {
            codeWriter.close();
        }
    }

    public void run() {
        try {
            initialize();
            translate();
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        } finally {
            close();
        }
    }
}