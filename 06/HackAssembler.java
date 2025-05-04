import java.io.*;


public class HackAssembler {
    private Parser parser;
    private SymbolTable symbolTable;
    private Code code;
    private String outputFileName;

    public HackAssembler(String inputFile) throws IOException {
        // Initialize the parser, symbol table, and code
        parser = Parser.getInstance(inputFile);
        symbolTable = new SymbolTable();
        code = Code.getInstance();
        outputFileName = inputFile.replace(".asm", ".hack"); 
    }


    //first pass
    public void firstPass() throws IOException {
        //tracks the current address
        int currentAddress = 0; 
        while (parser.hasMoreLines()) {
            //advance knows to skip comments
            parser.advance();
            if (parser.instructionType().equals("L_INSTRUCTION")) {
                // add to the symbol table with its address
                String symbol = parser.symbol();
                symbolTable.addEntry(symbol, currentAddress);
                
                 
                //increments the nextavailble address only if we took its place
                if((currentAddress >= 16) && (currentAddress != symbolTable.nextAvailableAddress -1)){
                    symbolTable.nextAvailableAddress--;
                } 
            } else {
                // Increment address only for non-label lines
                currentAddress++;
            }
        }
    }

    //second pass - ignores L-instructions
    public void secondPass() throws IOException {
        //reset parser to the beginning
        parser.reset();
        // opens the output file for writing
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
            while (parser.hasMoreLines()) {
                parser.advance();
                String instructionType = parser.instructionType();
    
                //A-instruction
                if (instructionType.equals("A_INSTRUCTION")) {
                    String symbol = parser.symbol();
                    int address;
    
                    // if it is a number
                    if (symbol.matches("\\d+")) {
                        address = Integer.parseInt(symbol);
                    } else {
                        // if it is a symbol - check if its in the table. if not - add it. get his address.
                        if (!symbolTable.contains(symbol)) {
                            symbolTable.addEntry(symbol, symbolTable.nextAvailableAddress);
                        }
                        address = symbolTable.getAddress(symbol);
                    }
    
                    // convert address to binary 16-bit and writeit  to the file
                    String binaryString = Integer.toBinaryString(address);
                    String fixedBinary = String.format("%16s", binaryString).replace(' ', '0');
                    writer.write(fixedBinary);
                    if(parser.hasMoreLines()){
                        writer.newLine();
                    }    
                // If the instruction is a C-instruction
                } else if (instructionType.equals("C_INSTRUCTION")) {
                    String dest = code.dest(parser.dest());
                    String comp = code.comp(parser.comp());
                    String jump = code.jump(parser.jump());
    
                    //connects the String's parts
                    String binaryInstruction = "111" + comp + dest + jump;
                    writer.write(binaryInstruction);
                    if(parser.hasMoreLines()){
                    writer.newLine();
                    }
                }
    
            }
        }
    }
}