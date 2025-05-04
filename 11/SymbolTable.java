import java.util.*;

public class SymbolTable {
    private HashMap<String, Symbol> classScope;
    private HashMap<String, Symbol> subroutineScope;
    private HashMap<String, Integer> indexCounters;

    //constructor for the symbol table
    private static class Symbol {
        String type;
        String kind;
        int index;
        Symbol(String type, String kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }
    }

    public SymbolTable() {
        reset();
    }

    //resets the symbol table
    public void reset() {
        classScope = new HashMap<>();
        subroutineScope = new HashMap<>();
        indexCounters = new HashMap<>();
        indexCounters.put("STATIC", 0);
        indexCounters.put("FIELD", 0);
        indexCounters.put("ARG", 0);
        indexCounters.put("VAR", 0);
    }

    //starts a new subroutine scope
    public void startSubroutine() {
        subroutineScope.clear();
        indexCounters.put("ARG", 0);
        indexCounters.put("VAR", 0);
    }

    //define a new identifier 
    public void define(String name, String type, String kind) {
        HashMap<String, Symbol> scope = (kind.equals("STATIC") || kind.equals("FIELD")) ? 
                                      classScope : subroutineScope;
        int index = indexCounters.get(kind);
        scope.put(name, new Symbol(type, kind, index));
        indexCounters.put(kind, index + 1);
    }

    //return the number of variables of the given kind
    public int varCount(String kind) {
        return indexCounters.get(kind);
    }

    //return the kind of the identifier with this name
    public String kindOf(String name) {
        Symbol symbol = subroutineScope.get(name);
        if (symbol == null) {
            symbol = classScope.get(name);
        }
        if (symbol == null) {
            return "NONE";
        } else {
            return symbol.kind;
        }
            }

    //return the type of the  identifier with this name
    public String typeOf(String name) {
        Symbol symbol = subroutineScope.get(name);
        if (symbol == null) {
            symbol = classScope.get(name);
        }
        if (symbol == null) {
            return "NONE";
        } else {
            return symbol.type;
        }
            }

    //return the index of the identifier with this name
    public int indexOf(String name) {
        Symbol symbol = subroutineScope.get(name);
        if (symbol == null) {
            symbol = classScope.get(name);
        }
        if (symbol == null) {
            return -1;
        } else {
            return symbol.index;
        }
            }
}