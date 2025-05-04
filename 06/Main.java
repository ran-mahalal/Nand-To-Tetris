import java.io.*;

public class Main {    
    public static void main(String[] args) {
        String inputFile = args[0];        
        try {
            HackAssembler assembler = new HackAssembler(inputFile);
            assembler.firstPass(); 
            assembler.secondPass();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
