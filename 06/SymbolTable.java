import java.util.HashMap;

public class SymbolTable {
    private final HashMap<String, Integer> table;
    //a field for the nexgt address to store a new symbol
    public int nextAvailableAddress;

    //constructor
    public SymbolTable() {
        table = new HashMap<>();
        
        // pre-definded symbols 
        table.put("R0", 0);
        table.put("R1", 1);
        table.put("R2", 2);
        table.put("R3", 3);
        table.put("R4", 4);
        table.put("R5", 5);
        table.put("R6", 6);
        table.put("R7", 7);
        table.put("R8", 8);
        table.put("R9", 9);
        table.put("R10", 10);
        table.put("R11", 11);
        table.put("R12", 12);
        table.put("R13", 13);
        table.put("R14", 14);
        table.put("R15", 15);
        table.put("SP", 0);
        table.put("LCL", 1);
        table.put("ARG", 2);
        table.put("THIS", 3);
        table.put("THAT", 4);
        table.put("SCREEN", 16384);
        table.put("KBD", 24576);
        
        // Start from address 16
        nextAvailableAddress = 16;
    }

    // add a symbol with its address
    public void addEntry(String symbol, int address) {
        table.put(symbol, address);
        if(address >= 16){
        nextAvailableAddress++;
        }
    }

    //getter
    public int getNextAvailableAddress() {
        return nextAvailableAddress;
    }


    // Check if the symbol already exists in the table
    public boolean contains(String symbol) {
        return table.containsKey(symbol);
    }

    // Get the address of a symbol
    public int getAddress(String symbol) {
        // Check if the symbol exists in the table
        if (table.containsKey(symbol)) {
            return table.get(symbol); 
        }
        return -1;
    }

}
