import java.io.*;
import java.util.*;

public class JackTokenizer {
    private BufferedReader reader;
    private String currentToken;
    private String currentLine;
    //position in the current line
    private int currentPosition;
    private TokenType currentTokenType;
    private boolean inBlockComment;

    //set of the jack keywords
    private static final Set<String> keywords = new HashSet<>(Arrays.asList(
        "class", "constructor", "function", "method", "field", "static", "var",
        "int", "char", "boolean", "void", "true", "false", "null", "this",
        "let", "do", "if", "else", "while", "return"
    ));
    
    //set of the jack symbols
    private static final Set<Character> symbols = new HashSet<>(Arrays.asList(
        '{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&',
        '|', '<', '>', '=', '~'
    ));

    public enum TokenType { 
        KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST 
    }

    public JackTokenizer(String inputFile) throws IOException {
        reader = new BufferedReader(new FileReader(inputFile));
        currentLine = "";
        currentPosition = 0;
        inBlockComment = false;
        //gets the first token
        advance();
    }

    public boolean hasMoreTokens() {
        return currentToken != null;
    }

    public void advance() {
        try {
            ignoreSpacesAndComments();
            if (currentLine == null) {
                currentToken = null;
                return;
            }
            processNextToken();
        } catch (IOException e) {
            throw new RuntimeException("Error reading file", e);
        }
    }

    private void ignoreSpacesAndComments() throws IOException {
        while (true) {
            //ignores empty lines
            while (currentPosition >= currentLine.length()) {
                currentLine = reader.readLine();
                if (currentLine == null) {
                    return;
                }
                currentLine = currentLine.trim();
                currentPosition = 0;
            }

            //skip whitespaces in the curren line
            while (currentPosition < currentLine.length() && 
                   Character.isWhitespace(currentLine.charAt(currentPosition))) {
                currentPosition++;
            }
            if (currentPosition >= currentLine.length()){
                continue;
            } 
            // Check for comments
            if (currentPosition + 1 < currentLine.length() &&
                currentLine.charAt(currentPosition) == '/' &&
                currentLine.charAt(currentPosition + 1) == '/') {
                currentLine = reader.readLine();
                if (currentLine == null) {
                    return;
                }
                currentLine = currentLine.trim();
                currentPosition = 0;
                continue;
            }
            if (currentPosition + 1 < currentLine.length() &&
                currentLine.charAt(currentPosition) == '/' &&
                currentLine.charAt(currentPosition + 1) == '*') {
                skipLongComment();
                continue;
            }
            break;
        }
    }
    // skips a comment form this type: "/* ... */"
    private void skipLongComment() throws IOException {
        currentPosition += 2;
        while (true) {
            while (currentPosition >= currentLine.length() || 
                   !currentLine.substring(currentPosition).contains("*/")) {
                currentLine = reader.readLine();
                if (currentLine == null){
                    return;
                } 
                currentLine = currentLine.trim();
                currentPosition = 0;
            }
            int endComment = currentLine.indexOf("*/", currentPosition);
            //the comment ends in this line
            if (endComment != -1) {
                currentPosition = endComment + 2;
                break;
            }
        }
    }

    private void processNextToken() {
        if (currentPosition >= currentLine.length()) {
            currentToken = null;
            return;
        }
        char c = currentLine.charAt(currentPosition);
        if (c == '"') {
            processStringConstant();
        } else if (symbols.contains(c)) {
            currentToken = String.valueOf(c);
            currentTokenType = TokenType.SYMBOL;
            currentPosition++;
        } else if (Character.isDigit(c)) {
            processNumber();
        } else if (isIdentifierStart(c)) {
            processIdentifier();
        } else {
            currentPosition++;
            advance();
        }
    }

    private void processStringConstant() {
        int stringEnd = currentLine.indexOf('"', currentPosition + 1);
        //the string constant doesnt close in this line
        if (stringEnd == -1) {
            throw new RuntimeException("string constant wasn't closed");
        }
        currentToken = currentLine.substring(currentPosition + 1, stringEnd);
        currentTokenType = TokenType.STRING_CONST;
        currentPosition = stringEnd + 1;
    }

    private void processNumber() {
        int start = currentPosition;
        //adavnce the current position while were still in the number
        while (currentPosition < currentLine.length() && Character.isDigit(currentLine.charAt(currentPosition))) {
            currentPosition++;
        }
        currentToken = currentLine.substring(start, currentPosition);
        currentTokenType = TokenType.INT_CONST;
    }
    //procees an identifier and determine - keyword or identifier
    private void processIdentifier() {
        int start = currentPosition;
        //advance the curreent position while we are still in the identifier
        while (currentPosition < currentLine.length() && 
               (Character.isLetterOrDigit(currentLine.charAt(currentPosition)) || 
                currentLine.charAt(currentPosition) == '_')) {
            currentPosition++;
        }
        currentToken = currentLine.substring(start, currentPosition);
        if (keywords.contains(currentToken)) {
            currentTokenType = TokenType.KEYWORD;
        } else {
            currentTokenType = TokenType.IDENTIFIER;
        }    
    }

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    public TokenType tokenType() {
        return currentTokenType;
    }

    public String keyWord() {
        return currentToken;
    }

    public char symbol() {
        return currentToken.charAt(0);
    }

    public String identifier() {
        return currentToken;
    }

    public int intVal() {
        return Integer.parseInt(currentToken);
    }

    public String stringVal() {
        return currentToken;
    }
}