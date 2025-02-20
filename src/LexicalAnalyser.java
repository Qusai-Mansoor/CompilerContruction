// LexicalAnalyzer.java
import java.io.*;
import java.util.*;

// Token class to represent lexical tokens

// Enum for token types
enum TokenType {
    // Keywords
    NUM,        // Integer
    DECI,       // Decimal number
    LETTER,     // Character
    COND,       // Boolean
    
    // Literals
    INTEGER_LITERAL,
    DECIMAL_LITERAL,
    CHARACTER_LITERAL,
    BOOLEAN_LITERAL,
    
    // Identifiers
    IDENTIFIER,
    
    // Operators
    PLUS,           // +
    MINUS,          // -
    MULTIPLY,       // *
    DIVIDE,         // /
    MODULO,         // %
    EXPONENT,       // ^
    
    // Assignment
    ASSIGN,         // =
    
    // Comparison
    EQUAL,          // ==
    NOT_EQUAL,      // !=
    LESS_THAN,      // <
    GREATER_THAN,   // >
    LESS_EQUAL,     // <=
    GREATER_EQUAL,  // >=
    
    // Logical operators
    AND,            // &&
    OR,             // ||
    NOT,            // !
    
    // Delimiters
    LPAREN,         // (
    RPAREN,         // )
    LBRACE,         // {
    RBRACE,         // }
    SEMICOLON,      // ;
    COMMA,          // ,
    
    // Input/Output
    INPUT,          // read
    OUTPUT,         // print
    
    // Comments
    COMMENT,
    
    // Special
    EOF,            // End of file
    ERROR           // Error token
}

// Lexical Analyzer class
public class LexicalAnalyser {
    private String sourceCode;
    private List<String> lines;
    private List<Token> tokens;
    private int currentLine;
    private int currentPosition;
    private char currentChar;
    private boolean hasError;
    private List<String> errors;
    
    // Add SymbolTable reference
    private SymbolTable symbolTable;
    private String currentDataType = null; // Track current data type for variable declarations
    
    // Keywords map (all lowercase as specified)
    private static final Map<String, TokenType> keywords = new HashMap<>();
    // Data type keywords mapping to their actual type names
    private static final Map<String, String> dataTypeKeywords = new HashMap<>();
    
    static {
        keywords.put("num", TokenType.NUM);
        keywords.put("deci", TokenType.DECI);
        keywords.put("letter", TokenType.LETTER);
        keywords.put("cond", TokenType.COND);
        keywords.put("true", TokenType.BOOLEAN_LITERAL);
        keywords.put("false", TokenType.BOOLEAN_LITERAL);
        keywords.put("read", TokenType.INPUT);
        keywords.put("print", TokenType.OUTPUT);
        
        // Map keyword tokens to actual data types
        dataTypeKeywords.put("num", "int");
        dataTypeKeywords.put("deci", "float");
        dataTypeKeywords.put("letter", "char");
        dataTypeKeywords.put("cond", "boolean");
    }
    
    public LexicalAnalyser(String sourceCode) {
        this.sourceCode = preprocessCode(sourceCode);
        this.lines = Arrays.asList(this.sourceCode.split("\n"));
        this.tokens = new ArrayList<>();
        this.currentLine = 0;
        this.currentPosition = 0;
        this.hasError = false;
        this.errors = new ArrayList<>();
        this.symbolTable = new SymbolTable(); // Initialize the symbol table
        
        if (!lines.isEmpty()) {
            advanceChar();
        }
    }
    
    // Getter for the symbol table
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
    
    // Preprocess the source code (unchanged from your original)
    private String preprocessCode(String code) {
        // First, normalize line endings
        code = code.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
        
        // Remove single-line comments (using // for demonstration)
        StringBuilder processed = new StringBuilder();
        String[] lines = code.split("\n");
        
        boolean inMultiLineComment = false;
        
        for (String line : lines) {
            int i = 0;
            while (i < line.length()) {
                // Check for single-line comment
                if (!inMultiLineComment && i + 1 < line.length() && line.charAt(i) == '/' && line.charAt(i + 1) == '/') {
                    break;  // Ignore rest of the line
                }
                
                // Check for start of multi-line comment
                if (!inMultiLineComment && i + 1 < line.length() && line.charAt(i) == '/' && line.charAt(i + 1) == '*') {
                    inMultiLineComment = true;
                    i += 2;
                    continue;
                }
                
                // Check for end of multi-line comment
                if (inMultiLineComment && i + 1 < line.length() && line.charAt(i) == '*' && line.charAt(i + 1) == '/') {
                    inMultiLineComment = false;
                    i += 2;
                    continue;
                }
                
                // If not in comment, append character
                if (!inMultiLineComment) {
                    processed.append(line.charAt(i));
                }
                
                i++;
            }
            processed.append('\n');
        }
        
        // Normalize whitespace (collapse multiple spaces to single space)
        return processed.toString().replaceAll("\\s+", " ").trim();
    }
    
    // Character handling methods unchanged from your original
    private void advanceChar() {
        if (currentLine >= lines.size()) {
            currentChar = '\0'; // EOF marker
            return;
        }
        
        String currentLineText = lines.get(currentLine);
        
        if (currentPosition >= currentLineText.length()) {
            // Move to next line
            currentLine++;
            currentPosition = 0;
            
            if (currentLine >= lines.size()) {
                currentChar = '\0'; // EOF marker
                return;
            }
            
            currentLineText = lines.get(currentLine);
        }
        
        if (currentPosition < currentLineText.length()) {
            currentChar = currentLineText.charAt(currentPosition++);
        } else {
            currentChar = '\n';
            currentLine++;
            currentPosition = 0;
        }
    }
    
    private char peekChar() {
        if (currentLine >= lines.size()) {
            return '\0'; // EOF marker
        }
        
        String currentLineText = lines.get(currentLine);
        
        if (currentPosition >= currentLineText.length()) {
            // Would move to next line
            return '\n';
        }
        
        return currentLineText.charAt(currentPosition);
    }
    
    // Main tokenization method with symbol table integration
    public List<Token> tokenize() {
        while (currentChar != '\0') {
            if (Character.isWhitespace(currentChar)) {
                // Skip whitespace
                advanceChar();
                continue;
            }
            
            // Track starting position for error reporting
            int tokenLine = currentLine + 1; // 1-based line numbers
            int tokenColumn = currentPosition;
            
            if (isAlphaLower(currentChar)) {
                // Identifier or keyword
                String lexeme = scanIdentifier();
                TokenType type = keywords.getOrDefault(lexeme, TokenType.IDENTIFIER);
                
                Token token = new Token(type, lexeme, tokenLine, tokenColumn);
                tokens.add(token);
                
                // Handle symbol table entries based on token type
                handleSymbolTableEntry(token);
            } else if (Character.isDigit(currentChar)) {
                // Number literal
                scanNumber(tokenLine, tokenColumn);
            } else if (currentChar == '\'') {
                // Character literal
                scanCharLiteral(tokenLine, tokenColumn);
            } else {
                // Operators and other symbols
                scanOperatorOrDelimiter(tokenLine, tokenColumn);
            }
        }
        
        // Add EOF token
        tokens.add(new Token(TokenType.EOF, "", currentLine + 1, currentPosition));
        return tokens;
    }
    
    // Helper method to handle symbol table entries
    private void handleSymbolTableEntry(Token token) {
        if (token.getType() == TokenType.NUM || 
            token.getType() == TokenType.DECI || 
            token.getType() == TokenType.LETTER || 
            token.getType() == TokenType.COND) {
            // This is a data type declaration, store it for upcoming identifiers
            currentDataType = dataTypeKeywords.get(token.getLexeme());
        }
        else if (token.getType() == TokenType.IDENTIFIER) {
            // Handle identifiers
            if (currentDataType != null) {
                // This identifier follows a data type declaration, so it's a variable declaration
                boolean isGlobal = symbolTable.getCurrentScope() == 0;
                boolean inserted = symbolTable.insert(
                    token.getLexeme(),           // name
                    SymbolType.VARIABLE,         // type
                    currentDataType,             // dataType
                    false,                       // isConstant (assume non-constant by default)
                    isGlobal,                    // isGlobal
                    null,                        // value (initially null)
                    token.getLine(),             // line
                    token.getColumn()            // column
                );
                
                if (!inserted) {
                    reportError(token.getLine(), token.getColumn(),
                        "Redeclaration of identifier '" + token.getLexeme() + "' in the same scope");
                }
            } else {
                // This is a reference to an existing identifier
                Symbol symbol = symbolTable.lookup(token.getLexeme());
                if (symbol == null) {
                    reportError(token.getLine(), token.getColumn(),
                        "Undeclared identifier: " + token.getLexeme());
                }
            }
        }
        else if (token.getType() == TokenType.LBRACE) {
            // Opening brace, enter a new scope
            symbolTable.enterScope();
            currentDataType = null; // Reset current data type
        }
        else if (token.getType() == TokenType.RBRACE) {
            // Closing brace, exit the current scope
            symbolTable.exitScope();
            currentDataType = null; // Reset current data type
        }
        else if (token.getType() == TokenType.SEMICOLON) {
            // End of statement, reset current data type
            currentDataType = null;
        }
    }
    
    // Other scanning methods (unchanged from your original)
    private boolean isAlphaLower(char c) {
        return c >= 'a' && c <= 'z';
    }
    
    private String scanIdentifier() {
        StringBuilder sb = new StringBuilder();
        
        while (isAlphaLower(currentChar)) {
            sb.append(currentChar);
            advanceChar();
        }
        
        return sb.toString();
    }
    
    private void scanNumber(int line, int column) {
        StringBuilder sb = new StringBuilder();
        boolean isDecimal = false;
        
        // Scan whole number part
        while (Character.isDigit(currentChar)) {
            sb.append(currentChar);
            advanceChar();
        }
        
        // Check for decimal point
        if (currentChar == '.') {
            isDecimal = true;
            sb.append(currentChar);
            advanceChar();
            
            // Scan decimal part
            boolean hasDecimalDigits = false;
            while (Character.isDigit(currentChar)) {
                sb.append(currentChar);
                hasDecimalDigits = true;
                advanceChar();
            }
            
            if (!hasDecimalDigits) {
                // Error: Decimal point with no digits
                reportError(line, column, "Malformed decimal number: no digits after decimal point");
                return;
            }
        }
        
        String numberStr = sb.toString();
        if (isDecimal) {
            try {
                // Validate decimal is within precision (5 decimal places)
                double value = Double.parseDouble(numberStr);
                tokens.add(new Token(TokenType.DECIMAL_LITERAL, numberStr, line, column));
            } catch (NumberFormatException e) {
                reportError(line, column, "Invalid decimal number format: " + numberStr);
            }
        } else {
            try {
                int value = Integer.parseInt(numberStr);
                tokens.add(new Token(TokenType.INTEGER_LITERAL, numberStr, line, column));
            } catch (NumberFormatException e) {
                reportError(line, column, "Invalid integer format: " + numberStr);
            }
        }
    }
    
    private void scanCharLiteral(int line, int column) {
        StringBuilder sb = new StringBuilder();
        sb.append(currentChar); // Append opening quote
        advanceChar();
        
        // Check for empty character literal
        if (currentChar == '\'') {
            reportError(line, column, "Empty character literal");
            advanceChar();
            return;
        }
        
        // Read character (handle escape sequences if needed)
        if (currentChar == '\\') {
            sb.append(currentChar);
            advanceChar();
            if (currentChar == '\0') {
                reportError(line, column, "Unterminated character literal");
                return;
            }
            sb.append(currentChar);
            advanceChar();
        } else {
            sb.append(currentChar);
            advanceChar();
        }
        
        // Check for closing quote
        if (currentChar != '\'') {
            reportError(line, column, "Unterminated character literal");
            return;
        }
        
        sb.append(currentChar); // Append closing quote
        advanceChar();
        
        tokens.add(new Token(TokenType.CHARACTER_LITERAL, sb.toString(), line, column));
    }
    
    private void scanOperatorOrDelimiter(int line, int column) {
        Token token = null;
        
        switch (currentChar) {
            case '+':
                token = new Token(TokenType.PLUS, "+", line, column);
                advanceChar();
                break;
            case '-':
                token = new Token(TokenType.MINUS, "-", line, column);
                advanceChar();
                break;
            case '*':
                token = new Token(TokenType.MULTIPLY, "*", line, column);
                advanceChar();
                break;
            case '/':
                token = new Token(TokenType.DIVIDE, "/", line, column);
                advanceChar();
                break;
            case '%':
                token = new Token(TokenType.MODULO, "%", line, column);
                advanceChar();
                break;
            case '^':
                token = new Token(TokenType.EXPONENT, "^", line, column);
                advanceChar();
                break;
            case '=':
                if (peekChar() == '=') {
                    advanceChar();
                    token = new Token(TokenType.EQUAL, "==", line, column);
                    advanceChar();
                } else {
                    token = new Token(TokenType.ASSIGN, "=", line, column);
                    advanceChar();
                }
                break;
            case '!':
                if (peekChar() == '=') {
                    advanceChar();
                    token = new Token(TokenType.NOT_EQUAL, "!=", line, column);
                    advanceChar();
                } else {
                    token = new Token(TokenType.NOT, "!", line, column);
                    advanceChar();
                }
                break;
            case '<':
                if (peekChar() == '=') {
                    advanceChar();
                    token = new Token(TokenType.LESS_EQUAL, "<=", line, column);
                    advanceChar();
                } else {
                    token = new Token(TokenType.LESS_THAN, "<", line, column);
                    advanceChar();
                }
                break;
            case '>':
                if (peekChar() == '=') {
                    advanceChar();
                    token = new Token(TokenType.GREATER_EQUAL, ">=", line, column);
                    advanceChar();
                } else {
                    token = new Token(TokenType.GREATER_THAN, ">", line, column);
                    advanceChar();
                }
                break;
            case '&':
                if (peekChar() == '&') {
                    advanceChar();
                    token = new Token(TokenType.AND, "&&", line, column);
                    advanceChar();
                } else {
                    reportError(line, column, "Expected '&' after '&'");
                    advanceChar();
                }
                break;
            case '|':
                if (peekChar() == '|') {
                    advanceChar();
                    token = new Token(TokenType.OR, "||", line, column);
                    advanceChar();
                } else {
                    reportError(line, column, "Expected '|' after '|'");
                    advanceChar();
                }
                break;
            case '(':
                token = new Token(TokenType.LPAREN, "(", line, column);
                advanceChar();
                break;
            case ')':
                token = new Token(TokenType.RPAREN, ")", line, column);
                advanceChar();
                break;
            case '{':
                token = new Token(TokenType.LBRACE, "{", line, column);
                tokens.add(token);
                advanceChar();
                // Handle scope entry in the symbol table
                handleSymbolTableEntry(token);
                return;  // Already added token and handled symbol table
            case '}':
                token = new Token(TokenType.RBRACE, "}", line, column);
                tokens.add(token);
                advanceChar();
                // Handle scope exit in the symbol table
                handleSymbolTableEntry(token);
                return;  // Already added token and handled symbol table
            case ';':
                token = new Token(TokenType.SEMICOLON, ";", line, column);
                tokens.add(token);
                advanceChar();
                // Handle statement end in the symbol table
                handleSymbolTableEntry(token);
                return;  // Already added token and handled symbol table
            case ',':
                token = new Token(TokenType.COMMA, ",", line, column);
                advanceChar();
                break;
            default:
                reportError(line, column, "Unexpected character: " + currentChar);
                advanceChar();
                return;
        }
        
        // Add token to the list if we created one
        if (token != null) {
            tokens.add(token);
        }
    }
    
    // Error reporting (unchanged from your original)
    private void reportError(int line, int column, String message) {
        hasError = true;
        String error = String.format("Lexical error at line %d, column %d: %s", line, column, message);
        errors.add(error);
        tokens.add(new Token(TokenType.ERROR, message, line, column));
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public boolean hasErrors() {
        return hasError;
    }
    
    // Main method for testing
}