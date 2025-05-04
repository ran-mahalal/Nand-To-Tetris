    import java.io.*;
    
    public class Parser {
        //holds an instance of a parser - for the use as a singelton
        private static Parser instance;

        //will use a bufferreader object to read the file
        private BufferedReader reader;  
        
        //stores the input file for this parser
        private String fileInput;

        //field for current instruction
        public String cur_inst;

        // Getter for cur_inst
        public String getCurInst() {
            return cur_inst;
        }
        
        //constructor
        private Parser(String fileInput) throws IOException{
            this.fileInput = fileInput;
            reader = new BufferedReader(new FileReader(fileInput));
        }

        //gets the parser instance
        public static Parser getInstance(String fileInput) throws IOException {
            if (instance == null) {
                instance = new Parser(fileInput);
            }
            return instance;
        }

        //restes the parser to the begining of the inoput file
        public void reset() throws IOException {
            reader.close();
            reader = new BufferedReader(new FileReader(fileInput));
        }


        //check if there are more lines to read
        public boolean hasMoreLines() throws IOException {
            if(reader.ready()){
                return true;
            }
            return false;
        }


        //a method to read the next good instruction
        public void advance() throws IOException {
            //if the whole file is not read yet
            if(hasMoreLines()){
                String currentLine;
                // variable to find a comment that is longer than one line
                boolean inlongComment = false;

                // Reads lines from fileInput (breaks if read a good line)
                try{
                    while ((currentLine = reader.readLine()) != null) {
                        currentLine = currentLine.trim();
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
                            cur_inst = currentLine.substring(0, commentIndex).trim();
                            break;
                        } else {
                            // no comments -updates pardser's current instruction to this instruction
                        cur_inst = currentLine.trim();
                        break;                
                        }
                    }
                } catch (IOException e) {
                    System.out.println("caught an exception" + e);
                }
            }
        } 
        
        //determines instruction type
        public String instructionType() {
            if (cur_inst.startsWith("@")) {
                return "A_INSTRUCTION"; 
            } else if (cur_inst.startsWith("(") && cur_inst.endsWith(")")) {
                return "L_INSTRUCTION";
            } else {
                return "C_INSTRUCTION";
            }
        }

        //symbol function - returnes the symbol, for A and L instructions
        public String symbol() {
            if (instructionType().equals("A_INSTRUCTION")) {
                return cur_inst.substring(1);
            } else if (instructionType().equals("L_INSTRUCTION")) {
                return cur_inst.substring(1, cur_inst.length() - 1);
            }
            //nothing to do if its a C instruction
            return null;
        }

        //returns dest part as a String for C instructions
        public String dest() {
            //return the first part of the C instruction - dest
            if (instructionType().equals("C_INSTRUCTION")) {
                String[] splitArray = cur_inst.split("=");
                if(splitArray[0].equals(cur_inst)){
                    return null;
                }
                return splitArray[0];
            }
            //if it is not a C instructions
            return null;
        }
            //returns the comp part as a String for C instructions
            public String comp() {
                if (instructionType().equals("C_INSTRUCTION")) {
                    String[] splitArrayEquals = cur_inst.split("[=]");
                    String compAndjump;
                    if(splitArrayEquals.length > 1){
                        compAndjump = splitArrayEquals[1];
                    } else {
                        compAndjump = cur_inst;
                    }

                    String[] splitArraySemicolon = compAndjump.split(";");
                    return splitArraySemicolon[0];
                }
                //if it is not a C instructions
                return null;
            }

            //returns jump part as a String for C instructions
        public String jump() {
            //return the last part of the C instruction - jump
            if (instructionType().equals("C_INSTRUCTION")) {
                String[] splitArray = cur_inst.split(";");
                if(splitArray[0].equals(cur_inst)){
                    return null;
                }
                return splitArray[1];
            }
            //if it is not a C instructions
            return null;
        }

            


    }

        



