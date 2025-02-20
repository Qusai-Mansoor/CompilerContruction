import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String filePath = "src/Test.iq";
        if (!filePath.endsWith(".iq")) {
            System.err.println("Error: The source file must have a .iq extension.");
            return;
        }
        String sourceCode;
        try {
            sourceCode = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }
        System.out.println("=== Source Code from " + filePath + " ===");
        System.out.println(sourceCode);
        System.out.println("===========================================");
        LexicalAnalyser lexer = new LexicalAnalyser(sourceCode);
        List<Token> tokens = lexer.tokenize();
        
        System.out.println("Tokens found: " + tokens.size());
        for (Token token : tokens) {
            if (token.getType() != TokenType.EOF) {
                System.out.println(token);
            }
        }
        
        if (lexer.hasErrors()) {
            System.out.println("\nErrors found:");
            for (String error : lexer.getErrors()) {
                System.out.println(error);
            }
        } else {
            System.out.println("\nNo lexical errors found.");
        }
        System.out.println("\nSYMBOL TABLE:");
        lexer.getSymbolTable().display();
        // Symbol Table
        
    }
}
