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
        
        // Always write initialization for directory or when Sys.vm exists
        boolean hasSysInit = inputFiles.stream()
            .anyMatch(p -> p.getFileName().toString().equals("Sys.vm"));
        
        if (inputFiles.size() > 1 || hasSysInit) {
            codeWriter.writeInit();
        }
    }
    

    private void translate() throws IOException {
        for (Path inputFile : inputFiles) {
            System.out.println("Processing: " + inputFile.getFileName());
            // Create new parser for each file
            try {
                codeWriter.setFileName(inputFile.toString()); // Add this line
                Parser parser = Parser.getInstance(inputFile.toString());
                // Process all commands in this file
                while (parser.hasMoreCommands()) {
                    parser.advance();
                    if (parser.cur_command == null || parser.cur_command.trim().isEmpty()) {
                        continue;
                    }

                    Parser.CommandType commandType = parser.commandType();

                    if (commandType == parser.commandType().C_ARITHMETIC) {
                        codeWriter.writeArithmetic(parser.arg1());
                    } else if (commandType == Parser.CommandType.C_PUSH || commandType == Parser.CommandType.C_POP) {
                        codeWriter.writePushPop(commandType, parser.arg1(), parser.arg2());
                    } else if (commandType == Parser.CommandType.C_LABEL) {
                        codeWriter.writeLabel(parser.arg1());
                    } else if (commandType == Parser.CommandType.C_GOTO) {
                        codeWriter.writeGoto(parser.arg1());
                    } else if (commandType == Parser.CommandType.C_IF) {
                        codeWriter.writeIf(parser.arg1());
                    } else if (commandType == Parser.CommandType.C_FUNCTION) {
                        codeWriter.writeFunction(parser.arg1(), parser.arg2());
                    } else if (commandType == Parser.CommandType.C_RETURN) {
                        codeWriter.writeReturn();
                    } else if (commandType == Parser.CommandType.C_CALL) {
                        codeWriter.writeCall(parser.arg1(), parser.arg2());
                    }

                }
            } catch (IOException e) {
                System.err.println("Error processing file " + inputFile.getFileName() + ": " + e.getMessage());
            }
        }
        System.out.println("\nTranslation complete.");
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