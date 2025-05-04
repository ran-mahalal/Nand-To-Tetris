import java.io.*;


public class Parser {

    private static Parser instance;
    private BufferedReader reader;  
    private String fileInput;
    public String cur_command;

    //constructor
    private Parser(String fileInput) throws IOException{
        this.fileInput = fileInput;
        reader = new BufferedReader(new FileReader(fileInput));
    }

    //gets the parser instance
    public static Parser getInstance(String fileInput) throws IOException {
        // Create new instance for each file instead of reusing
        instance = new Parser(fileInput);
        return instance;
    }

    //restes the parser to the begining of the inoput file
    public void reset() throws IOException {
        reader.close();
        reader = new BufferedReader(new FileReader(fileInput));
    }


    //check if there are more lines to read
    public boolean hasMoreCommands() throws IOException {
        if(reader.ready()){
            return true;
        }
        return false;
    }


      //a method to read the next good instruction
    public void advance() throws IOException {
        //if the whole file is not read yet
        if(hasMoreCommands()){
            String currentLine;
            // variable to find a comment that is longer than one line
            boolean inlongComment = false;

            // Reads lines from fileInput (breaks if read a good line)
            try{
                while ((currentLine = reader.readLine()) != null) {
                    if(currentLine.isEmpty()){
                        continue;
                    }

                    // if in a comment, check if it ends in this line
                    if (inlongComment) {
                        if (currentLine.endsWith("*/")) {
                            inlongComment = false; 
                            continue;
                        }
                        continue; 
                    }

                    // Check for the start of a long comment
                    if (currentLine.startsWith("/*")) {
                        inlongComment = true;
                        continue; 
                    }

                    // Check for normal comment
                    if (currentLine.startsWith("//")) {
                        continue;
                    }

                    // Handle lines with normal comments
                    int commentIndex = currentLine.indexOf("//");
                    if (commentIndex != -1) {
                        //updates pardser's current instruction to this instruction
                        cur_command = currentLine.substring(0, commentIndex);
                        break;
                    } else {
                        // no comments -updates pardser's current instruction to this instruction
                    cur_command = currentLine;
                    break;                
                    }
                }
            } catch (IOException e) {
                System.out.println("caught an exception" + e);
            }
        }
    } 

    //enum for command types
    public enum CommandType {
        C_ARITHMETIC, //  0
        C_PUSH,       //  1
        C_POP         //  2
    }
    
    //determines command type - returns a constant
    public CommandType commandType() {
        if (cur_command.startsWith("push")) {
            return CommandType.C_PUSH; 
        } else if (cur_command.startsWith("pop")) {
            return CommandType.C_POP;
        } else {
            return CommandType.C_ARITHMETIC;
        }
    }

    //returns the first argument of the current command
    public String arg1() {
        if ((commandType() == CommandType.C_PUSH) || (commandType() == CommandType.C_POP)) {
            return cur_command.split(" ")[1];
        } else { //its an arithmetic operation
            return cur_command;
        }
    }

    //returns the second argument of the current command
    public int arg2() {
        if (commandType() == CommandType.C_PUSH || commandType() == CommandType.C_POP) {
            return Integer.parseInt(cur_command.split(" ")[2]);
        } else {
            throw new IllegalStateException("arg2 can only be called for push or pop commands.");
        }
    }


}