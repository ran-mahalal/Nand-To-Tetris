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
           for (Path entry : stream) {
               jackFiles.add(entry);
           }
       }
       if (jackFiles.isEmpty()) {
           System.err.println("No .jack files found in directory");
           return;
       }
       for (Path jackFile : jackFiles) {
           processSingleFile(jackFile);
       }
   }

   private static void processSingleFile(Path filePath) throws IOException {
       if (!filePath.toString().endsWith(".jack")) {
           throw new IllegalArgumentException("Input file must have a .jack extension");
       }
       //generate tokenizer output
       String tokenOutputPath = filePath.toString().replace(".jack", "T.xml");
       generateTokenFile(filePath.toString(), tokenOutputPath);
       //generate parser output  
       String parseOutputPath = filePath.toString().replace(".jack", ".xml");
       CompilationEngine engine = new CompilationEngine(filePath.toString(), parseOutputPath);
       engine.compileClass();
   }

   private static void generateTokenFile(String inputPath, String outputPath) throws IOException {
       JackTokenizer tokenizer = new JackTokenizer(inputPath);
       BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
       writer.write("<tokens>\n"); 
       while (tokenizer.hasMoreTokens()) {
           String tokenType = tokenizer.tokenType().toString().toLowerCase();
           if (tokenType.equals("string_const")) tokenType = "stringConstant";
           if (tokenType.equals("int_const")) tokenType = "integerConstant";  
           String token = getTokenValue(tokenizer);
           if (token != null) {
               writer.write("<" + tokenType + "> " + specialXML(token) + " </" + tokenType + ">\n");
           }
           tokenizer.advance();
       }
       writer.write("</tokens>");
       writer.close();  
   }

   private static String getTokenValue(JackTokenizer tokenizer) {
    if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
        return tokenizer.keyWord();
    } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL) {
        return String.valueOf(tokenizer.symbol());
    } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
        return tokenizer.identifier();
    } else if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST) {
        return String.valueOf(tokenizer.intVal());
    } else if (tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST) {
        return tokenizer.stringVal();
    } else {
        return "";
    }
}

    private static String specialXML(String input) {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }
}