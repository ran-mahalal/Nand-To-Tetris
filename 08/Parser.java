
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
                        cur_command = currentLine.substring(0, commentIndex).trim();
                        break;
                    } else {
                        // no comments -updates pardser's current instruction to this instruction
                    cur_command = currentLine.trim();
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
        C_ARITHMETIC, 
        C_PUSH,       
        C_POP,         
        C_LABEL,
        C_GOTO,
        C_IF,
        C_FUNCTION,
        C_RETURN,
        C_CALL
    }
    
    //determines command type - returns a constant
    public CommandType commandType() {
        if (cur_command.startsWith("push")) {
            return CommandType.C_PUSH; 
        } else if (cur_command.startsWith("pop")) {
            return CommandType.C_POP;
        } else if (cur_command.startsWith("label")) {
            return CommandType.C_LABEL;
        } else if (cur_command.startsWith("goto")) {
            return CommandType.C_GOTO;
        } else if (cur_command.startsWith("if-goto")) {
            return CommandType.C_IF;
        } else if (cur_command.startsWith("function")) {
            return CommandType.C_FUNCTION;
        }else if (cur_command.startsWith("call")) {
            return CommandType.C_CALL;
        }else if (cur_command.startsWith("return")) {
            return CommandType.C_RETURN;
        } else {
            return CommandType.C_ARITHMETIC;
        }
    }

    //returns the first argument of the current command
    public String arg1() {
        if (cur_command == null) {
            throw new IllegalStateException("No current command");
        }
        if (commandType() == CommandType.C_RETURN) {
            throw new IllegalStateException("return command has no arguments");
        }
        if ((commandType() == CommandType.C_ARITHMETIC)) {
            return cur_command.split(" ")[0];
        } else { // the other commands have arg1
            return cur_command.split(" ")[1];
        }
    }

    //returns the second argument of the current command
    public int arg2() {
        if (cur_command == null) {
            throw new IllegalStateException("No current command");
        }
        if (commandType() == CommandType.C_PUSH || commandType() == CommandType.C_POP || commandType() == CommandType.C_FUNCTION || commandType() == CommandType.C_CALL){
            return Integer.parseInt(cur_command.split(" ")[2]);
        } else {
            throw new IllegalStateException("arg2 can only be called for push, pop, call and function commands.");
        }
    }


}
