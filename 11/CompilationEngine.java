import java.io.*;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private VMWriter writer;
    private SymbolTable symbolTable;
    private String className;
    private String currentSubroutine;
    private String currentSubroutineType;
    private int labelCounter;
   

    //constructor 
    public CompilationEngine(String inputPath, String outputPath) throws IOException {
        tokenizer = new JackTokenizer(inputPath);
        writer = new VMWriter(outputPath);
        symbolTable = new SymbolTable();
        labelCounter = 0;
    }
    //advances to the next token
    private void advanceToken(String type, String value) throws IOException {
        tokenizer.advance();
    }

    public void compileClass() throws IOException {
        tokenizer.advance();
        className = tokenizer.identifier();
        tokenizer.advance();
        tokenizer.advance();
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && 
              (tokenizer.keyWord().equals("static") || tokenizer.keyWord().equals("field"))) {
            //Compile class variable declarations
            compileClassVarDec();
        }
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
              (tokenizer.keyWord().equals("constructor") || 
               tokenizer.keyWord().equals("function") || 
               tokenizer.keyWord().equals("method"))) {
            compileSubroutine();
        }
        writer.close();
    }
    
    private void compileClassVarDec() throws IOException {
        String kind = tokenizer.keyWord().toUpperCase();
        tokenizer.advance();
        
        String type = tokenizer.getCurrentToken();
        tokenizer.advance();
        
        while (true) {
            String name = tokenizer.identifier();
            symbolTable.define(name, type, kind);
            tokenizer.advance();

            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
                // there are multiple variables
                tokenizer.advance();
            } else {
                break;
            }
        }
        advanceToken("SYMBOL", ";");
    }

    private void compileSubroutine() throws IOException {
        //reset the subroutine symbol tablr
        symbolTable.startSubroutine();
        
        currentSubroutineType = tokenizer.keyWord();
        tokenizer.advance();
        tokenizer.advance();
        
        String subroutineName = tokenizer.identifier();
        currentSubroutine = className + "." + subroutineName;
        tokenizer.advance();

        if (currentSubroutineType.equals("method")) {
            symbolTable.define("this", className, "ARG");
        }
        //advances for ( and )
        tokenizer.advance();
        compileParameterList();
        tokenizer.advance();
        compileSubroutineBody();
    }

    private void compileSubroutineBody() throws IOException {
        //opening {
        tokenizer.advance();  
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && 
               tokenizer.keyWord().equals("var")) {
            compileVarDec();
        }
        writer.writeFunction(currentSubroutine, symbolTable.varCount("VAR"));
        //sets up the constructor
        if (currentSubroutineType.equals("constructor")) {
            writer.writePush("constant", symbolTable.varCount("FIELD"));
            writer.writeCall("Memory.alloc", 1);
            writer.writePop("pointer", 0);
        }
        // sets up the method
        if (currentSubroutineType.equals("method")) {
            writer.writePush("argument", 0);
            writer.writePop("pointer", 0);
        }
        compileStatements();
        //closing }
        tokenizer.advance();
    }

    private void compileParameterList() throws IOException {
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || tokenizer.symbol() != ')') {
            while (true) {
                String type = tokenizer.getCurrentToken();
                tokenizer.advance();
                
                String name = tokenizer.identifier();
                symbolTable.define(name, type, "ARG");
                tokenizer.advance();
                
                if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
                    //there are multiple parameters
                    tokenizer.advance();
                } else {
                    break;
                }
            }
        }
    }

    private void compileVarDec() throws IOException {
        advanceToken("KEYWORD", "var"); 
        String type = tokenizer.getCurrentToken();
        tokenizer.advance();
        while (true) {
            String name = tokenizer.identifier();
            symbolTable.define(name, type, "VAR");
            tokenizer.advance();
            
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
                tokenizer.advance();
            } else {
                break;
            }
        }
        advanceToken("SYMBOL", ";");
    }
    //helper method 
    private String normalizeSegment(String segment) {
        if (segment == null){
            return null;
        } 
        return segment.toLowerCase().replace("var", "local").replace("arg", "argument").replace("field", "this");
    }

    private void compileLet() throws IOException {
        advanceToken("KEYWORD", "let"); 
        String varName = tokenizer.identifier();
        tokenizer.advance(); 
        boolean isArray = false;
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '[') {
            //there is an array assignemnt
            isArray = true;
            tokenizer.advance();
            //compile tyhe expression in the array index
            compileExpression();
            advanceToken("SYMBOL", "]");
            
            String kind = normalizeSegment(symbolTable.kindOf(varName));
            int index = symbolTable.indexOf(varName);
            writer.writePush(kind, index);
            writer.writeArithmetic("add");
        }
        advanceToken("SYMBOL", "=");
        compileExpression();
        advanceToken("SYMBOL", ";");
        if (isArray) {
            writer.writePop("temp", 0);
            writer.writePop("pointer", 1);
            writer.writePush("temp", 0);
            writer.writePop("that", 0);
        } else {
            String kind = normalizeSegment(symbolTable.kindOf(varName));
            int index = symbolTable.indexOf(varName);
            writer.writePop(kind, index);
        }
    }

    private void compileWhile() throws IOException {
        String whileStartLabel = generateLabel("WHILE");
        String whileEndLabel = generateLabel("WHILE_END");  
        writer.writeLabel(whileStartLabel);   
        advanceToken("KEYWORD", "while");
        advanceToken("SYMBOL", "(");
        compileExpression();
        advanceToken("SYMBOL", ")");    
        writer.writeArithmetic("not");
        //jump to the end if the while condition is false 
        writer.writeIf(whileEndLabel);    
        advanceToken("SYMBOL", "{");
        compileStatements();
        advanceToken("SYMBOL", "}");
        //jump back to the start   
        writer.writeGoto(whileStartLabel);
        writer.writeLabel(whileEndLabel);
    }
    //helper  method to generate a label
    private String generateLabel(String prefix) {
        return prefix + "_" + (labelCounter++);
    }

    private void compileIf() throws IOException {
        String elseLabel = generateLabel("IF");
        String endIfLabel = generateLabel("IF_END"); 
        advanceToken("KEYWORD", "if");
        advanceToken("SYMBOL", "(");
        compileExpression();
        advanceToken("SYMBOL", ")");    
        writer.writeArithmetic("not");
        writer.writeIf(elseLabel);   
        advanceToken("SYMBOL", "{");
        compileStatements();
        advanceToken("SYMBOL", "}"); 
        //go to the end of the if-else 
        writer.writeGoto(endIfLabel);
        writer.writeLabel(elseLabel);   
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && 
            tokenizer.keyWord().equals("else")) {
            tokenizer.advance();
            advanceToken("SYMBOL", "{");
            compileStatements();
            advanceToken("SYMBOL", "}");
        }   
        writer.writeLabel(endIfLabel);
    }

    private void compileDo() throws IOException {
        advanceToken("KEYWORD", "do");
        compileSubroutineCall();
        advanceToken("SYMBOL", ";");
        writer.writePop("temp", 0);
    }

    private void compileReturn() throws IOException {
        advanceToken("KEYWORD", "return");    
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || 
            tokenizer.symbol() != ';') {
            compileExpression();
        } else {
            //we push 0 to return void
            writer.writePush("constant", 0);
        }  
        advanceToken("SYMBOL", ";");
        writer.writeReturn();
    }

    private void compileExpression() throws IOException {
        compileTerm();
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL) {
            char op = tokenizer.symbol();
            if ("+-*/&|<>=".indexOf(op) == -1) break;     
            tokenizer.advance();
            compileTerm();
            if (op == '+') {
                writer.writeArithmetic("add");
            } else if (op == '-') {
                writer.writeArithmetic("sub");
            } else if (op == '*') {
                writer.writeCall("Math.multiply", 2);
            } else if (op == '/') {
                writer.writeCall("Math.divide", 2);
            } else if (op == '&') {
                writer.writeArithmetic("and");
            } else if (op == '|') {
                writer.writeArithmetic("or");
            } else if (op == '<') {
                writer.writeArithmetic("lt");
            } else if (op == '>') {
                writer.writeArithmetic("gt");
            } else if (op == '=') {
                writer.writeArithmetic("eq");
            }
        }
    }

    private int compileExpressionList() throws IOException {
        int nArgs = 0;
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || 
            tokenizer.symbol() != ')') {
            compileExpression();
            nArgs++;  
            while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && 
                   tokenizer.symbol() == ',') {
                tokenizer.advance();
                compileExpression();
                nArgs++;
            }
        }
        return nArgs;
    }

    private void compileTerm() throws IOException {
        if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST) {
            writer.writePush("constant", tokenizer.intVal());
            tokenizer.advance();
        }
        else if (tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST) {
            String str = tokenizer.stringVal();
            writer.writePush("constant", str.length());
            writer.writeCall("String.new", 1);
            for (char c : str.toCharArray()) {
                writer.writePush("constant", (int)c);
                writer.writeCall("String.appendChar", 2);
            }
            tokenizer.advance();
        }
        else if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            String keyword = tokenizer.keyWord();
            if (keyword.equals("true")) {
                writer.writePush("constant", 1);
                writer.writeArithmetic("neg");
            } 
            else if (keyword.equals("false") || keyword.equals("null")) {
                writer.writePush("constant", 0);
            }
            else if (keyword.equals("this")) {
                writer.writePush("pointer", 0);
            }
            tokenizer.advance();
        }
        else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL) {
            if (tokenizer.symbol() == '(') {
                tokenizer.advance();
                compileExpression();
                advanceToken("SYMBOL", ")");
            }
            else if (tokenizer.symbol() == '-' || tokenizer.symbol() == '~') {
                char op = tokenizer.symbol();
                tokenizer.advance();
                compileTerm();
                if (op == '-') writer.writeArithmetic("neg");
                else writer.writeArithmetic("not");
            }
        }
        else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            String name = tokenizer.identifier();
            tokenizer.advance();  
            String kind = normalizeSegment(symbolTable.kindOf(name));
            int index = symbolTable.indexOf(name);  
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && 
                tokenizer.symbol() == '[') {
                tokenizer.advance();
                writer.writePush(kind, index);
                compileExpression();
                advanceToken("SYMBOL", "]");
                writer.writeArithmetic("add");
                writer.writePop("pointer", 1);
                writer.writePush("that", 0);
            }
            else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && 
                    (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')) {
                compileSubroutineCall(name);
            }
            else {
                writer.writePush(kind, index);
            }
        }
    }
    
    //helper methods to compile a call to a subroutine
    private void compileSubroutineCall() throws IOException {
        String identifier = tokenizer.identifier();
        tokenizer.advance();
        compileSubroutineCall(identifier);
    }

    private void compileSubroutineCall(String firstPart) throws IOException {
        String functionName;
        int nArgs = 0;
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && 
            tokenizer.symbol() == '.') {
            tokenizer.advance();
            String secondPart = tokenizer.identifier();
            tokenizer.advance();   
            String type = symbolTable.typeOf(firstPart);
            if (!type.equals("NONE")) {
                String kind = normalizeSegment(symbolTable.kindOf(firstPart));
                int index = symbolTable.indexOf(firstPart);
             
                writer.writePush(kind, index);
                functionName = type + "." + secondPart;
                nArgs = 1;
            }
            else {
                functionName = firstPart + "." + secondPart;
            }
        }
        else {
            writer.writePush("pointer", 0);
            functionName = className + "." + firstPart;
            nArgs = 1;
        }
        advanceToken("SYMBOL", "(");
        nArgs += compileExpressionList();
        advanceToken("SYMBOL", ")"); 
        writer.writeCall(functionName, nArgs);
    }

    private void compileStatements() throws IOException {
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            String keyword = tokenizer.keyWord();
            if (keyword.equals("let")) {
                compileLet();
            } else if (keyword.equals("if")) {
                compileIf();
            } else if (keyword.equals("while")) {
                compileWhile();
            } else if (keyword.equals("do")) {
                compileDo();
            } else if (keyword.equals("return")) {
                compileReturn();
            }
        }
    }
}