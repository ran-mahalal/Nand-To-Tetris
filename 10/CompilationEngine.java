import java.io.*;
public class CompilationEngine {
    private JackTokenizer tokenizer;
    private BufferedWriter writer;

    public CompilationEngine(String inputPath, String outputPath) throws IOException {
        tokenizer = new JackTokenizer(inputPath);
        writer = new BufferedWriter(new FileWriter(outputPath));
    }

    public void compileClass() throws IOException {
        writer.write("<class>\n");
        writeToken();
        tokenizer.advance();
        //the class name
        writeToken();
        tokenizer.advance();
        //the { symobl
        writeToken();
        tokenizer.advance();
        //compiles the class variables
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && 
            (tokenizer.keyWord().equals("static") || tokenizer.keyWord().equals("field"))) {
            compileClassVarDec();
        } 
        //compiles the subroutines
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
            (tokenizer.keyWord().equals("constructor") || tokenizer.keyWord().equals("function") || 
            tokenizer.keyWord().equals("method"))) {
                compileSubroutine();
        }
        //thw closing }
        writeToken(); 
        writer.write("</class>\n");
        writer.close();
    }

    private void compileClassVarDec() throws IOException {
        writer.write("<classVarDec>\n");
        //field or static
        writeToken();
        tokenizer.advance();
        //variable type
        writeToken(); 
        tokenizer.advance();
        writeToken(); 
        tokenizer.advance();
        //handles a few variables of the same type
        while (tokenizer.symbol() == ',') {
            writeToken(); 
            tokenizer.advance();
            writeToken();
            tokenizer.advance();
        } 
        writeToken();
        tokenizer.advance();
        writer.write("</classVarDec>\n");
    }

    private void compileSubroutine() throws IOException {
        writer.write("<subroutineDec>\n");
        //constructor,method or function
        writeToken(); 
        tokenizer.advance();
        //return type or void
        writeToken();
        tokenizer.advance();
        writeToken(); // subroutineName
        tokenizer.advance();
        writeToken(); // (
        tokenizer.advance();
        compileParameterList();
        writeToken(); // )
        tokenizer.advance();
        compileSubroutineBody();
        writer.write("</subroutineDec>\n");
    }

    private void compileParameterList() throws IOException {
        writer.write("<parameterList>\n");  
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL) {
            //the type
            writeToken(); 
            tokenizer.advance();
            //the veriable name
            writeToken(); 
            tokenizer.advance();
            //more parameters are handled if such exist      
            while (tokenizer.symbol() == ',') {
                writeToken();
                tokenizer.advance();
                writeToken();
                tokenizer.advance();
                writeToken(); 
                tokenizer.advance();
            }
        }   
        writer.write("</parameterList>\n");
    }

    private void compileSubroutineBody() throws IOException {
        writer.write("<subroutineBody>\n");
        //the opening {
        writeToken();
        tokenizer.advance();       
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyWord().equals("var")) {
            compileVarDec();
        }     
        compileStatements();
        //the closing }
        writeToken();
        tokenizer.advance();
        writer.write("</subroutineBody>\n");
    }

    private void compileVarDec() throws IOException {
        writer.write("<varDec>\n");
        //decleration
        writeToken(); 
        tokenizer.advance();
        //variable type
        writeToken(); 
        tokenizer.advance();
        //variablre name
        writeToken(); 
        tokenizer.advance();      
        while (tokenizer.symbol() == ',') {
            writeToken(); 
            tokenizer.advance();
            writeToken(); 
            tokenizer.advance();
        }   
        writeToken();
        tokenizer.advance();
        writer.write("</varDec>\n");
    }

    private void compileStatements() throws IOException {
        writer.write("<statements>\n");
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
        writer.write("</statements>\n");
    }

    private void compileLet() throws IOException {
        writer.write("<letStatement>\n");
        //writing let
        writeToken();
        tokenizer.advance();
        //variable name
        writeToken(); 
        tokenizer.advance();  
        if (tokenizer.symbol() == '[') {
            //'['
            writeToken();
            tokenizer.advance();
            compileExpression();
            //']'
            writeToken();
            tokenizer.advance();
        }
        //'='
        writeToken(); 
        tokenizer.advance();
        //we reached the expression
        compileExpression();
        writeToken(); 
        tokenizer.advance();
        writer.write("</letStatement>\n");
    }

    private void compileIf() throws IOException {
        writer.write("<ifStatement>\n");
        //writes if, (, ), {, }, compiles expressions and statements between them
        writeToken();
        tokenizer.advance();
        writeToken(); 
        tokenizer.advance();
        compileExpression();
        writeToken(); 
        tokenizer.advance();
        writeToken(); 
        tokenizer.advance();
        compileStatements();
        writeToken(); 
        tokenizer.advance();
        //if it contains else    
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyWord().equals("else")) {
            writeToken(); 
            tokenizer.advance();
            writeToken(); 
            tokenizer.advance();
            compileStatements();
            writeToken(); 
            tokenizer.advance();
        }  
        writer.write("</ifStatement>\n");
    }

    private void compileWhile() throws IOException {
        //writes while, (, ), {, }, compiles expressions and statements between them
        writer.write("<whileStatement>\n");
        writeToken(); 
        tokenizer.advance();
        writeToken(); 
        tokenizer.advance();
        compileExpression();
        writeToken(); 
        tokenizer.advance();
        writeToken(); 
        tokenizer.advance();
        compileStatements();
        writeToken(); 
        tokenizer.advance();
        writer.write("</whileStatement>\n");
    }

    private void compileDo() throws IOException {
        writer.write("<doStatement>\n");
        // the 'do'
        writeToken(); 
        tokenizer.advance();
        compileSubroutineCall();
        writeToken(); 
        tokenizer.advance();
        writer.write("</doStatement>\n");
    }

    private void compileReturn() throws IOException {
        writer.write("<returnStatement>\n");
        //writes 'return'
        writeToken();
        tokenizer.advance();      
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || tokenizer.symbol() != ';') {
            compileExpression();
        }      
        writeToken(); // ;
        tokenizer.advance();
        writer.write("</returnStatement>\n");
    }

    private void compileExpression() throws IOException {
        writer.write("<expression>\n");
        compileTerm();
        //compiles the operation and the next term / terms  
        String operation = "+-*/&|<>=";    
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&  operation.indexOf(tokenizer.symbol()) != -1) {
            writeToken();
            tokenizer.advance();
            compileTerm();
        }       
        writer.write("</expression>\n");
    }

    private void compileTerm() throws IOException {
        writer.write("<term>\n");     
        if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST || 
            tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST || 
            tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            writeToken();
            tokenizer.advance();
        } 
        else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL) {
            //in this case we expect an expression
            if (tokenizer.symbol() == '(') {
                writeToken(); 
                tokenizer.advance();
                compileExpression();
                writeToken(); 
                tokenizer.advance();
            } else {
                //we will expect a term
                writeToken(); 
                tokenizer.advance();
                compileTerm();
            }
        }
        else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            //writes the variable or subroutine name
            writeToken(); 
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL) {
                if (tokenizer.symbol() == '[') {
                    //writes [] with the expression inside
                    writeToken(); 
                    tokenizer.advance();
                    compileExpression();
                    writeToken(); 
                    tokenizer.advance();
                } 
                else if(tokenizer.symbol() == '(') {
                    //writes () with the expressions inside
                    writeToken(); 
                    tokenizer.advance();
                    compileExpressionList();
                    writeToken(); 
                    tokenizer.advance();
                } else if(tokenizer.symbol() == '.'){
                    //a call to a subroutine
                    writeToken(); 
                    tokenizer.advance();
                    writeToken(); 
                    tokenizer.advance();
                    writeToken(); 
                    tokenizer.advance();
                    compileExpressionList();
                    writeToken();
                    tokenizer.advance();    
                }
            }
        }
        writer.write("</term>\n");
    }

    private void compileExpressionList() throws IOException {
        writer.write("<expressionList>\n");  
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || 
            tokenizer.symbol() != ')') {
            compileExpression();
            //keeps compiling expressions   
            while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && 
                tokenizer.symbol() == ',') {
                writeToken();
                tokenizer.advance();
                compileExpression();
            }
        }   
        writer.write("</expressionList>\n");
    }

    //helper method to compile a call to a subroutine
    private void compileSubroutineCall() throws IOException {
        //the name
        writeToken();
        tokenizer.advance();    
        if (tokenizer.symbol() == '.') { 
            writeToken(); 
            tokenizer.advance();
            writeToken(); 
            tokenizer.advance();
        }
        //the () and expressions inside if there are such  
        writeToken(); 
        tokenizer.advance();
        compileExpressionList();
        writeToken(); 
        tokenizer.advance();
    }

    //helper method to write the value of a token 
    private void writeToken() throws IOException {
        String tokenType = tokenizer.tokenType().toString().toLowerCase();
        if (tokenType.equals("int_const")) tokenType = "integerConstant";
        if (tokenType.equals("string_const")) tokenType = "stringConstant";  
        String token = tokenValue();
        writer.write("<" + tokenType + "> " + specialXML(token) + " </" + tokenType + ">\n");
    }

    private String tokenValue() {
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
    //helper method to replace the representation of the <,>,",&
    private String specialXML(String input) {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }
}