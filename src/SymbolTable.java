// SymbolTable.java
import java.util.*;

// Symbol class to represent entries in symbol table
class Symbol {
    private String name;
    private SymbolType type;
    private String dataType;
    private boolean isConstant;
    private boolean isGlobal;
    private Object value;
    private int scope;
    private int line;
    private int column;
    
    public Symbol(String name, SymbolType type, String dataType, boolean isConstant, 
                 boolean isGlobal, Object value, int scope, int line, int column) {
        this.name = name;
        this.type = type;
        this.dataType = dataType;
        this.isConstant = isConstant;
        this.isGlobal = isGlobal;
        this.value = value;
        this.scope = scope;
        this.line = line;
        this.column = column;
    }
    
    public String getName() {
        return name;
    }
    
    public SymbolType getType() {
        return type;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public boolean isConstant() {
        return isConstant;
    }
    
    public boolean isGlobal() {
        return isGlobal;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        if (!isConstant) {
            this.value = value;
        }
    }
    
    public int getScope() {
        return scope;
    }
    
    public int getLine() {
        return line;
    }
    
    public int getColumn() {
        return column;
    }
    
    @Override
    public String toString() {
        return String.format("%-15s %-10s %-10s %-10s %-10s %-15s %d",
                name, type, dataType, isConstant ? "const" : "var", 
                isGlobal ? "global" : "local", value, scope);
    }
}

// Enum for symbol types
enum SymbolType {
    VARIABLE,
    FUNCTION,
    PARAMETER
}

// Symbol Table implementation
public class SymbolTable {
    private Map<String, List<Symbol>> symbols;
    private int currentScope;
    
    public SymbolTable() {
        this.symbols = new HashMap<>();
        this.currentScope = 0;  // Global scope
    }
    
    // Enter a new scope
    public void enterScope() {
        currentScope++;
    }
    
    // Exit the current scope
    public void exitScope() {
        if (currentScope > 0) {
            currentScope--;
        }
    }
    
    // Get current scope level
    public int getCurrentScope() {
        return currentScope;
    }
    
    // Insert a new symbol
    public boolean insert(String name, SymbolType type, String dataType, boolean isConstant, 
                         boolean isGlobal, Object value, int line, int column) {
        // Check if the symbol already exists in the current scope
        if (lookupInCurrentScope(name) != null) {
            return false; // Symbol already exists in current scope
        }
        
        // Use the actual scope (global or current)
        int scope = isGlobal ? 0 : currentScope;
        
        Symbol symbol = new Symbol(name, type, dataType, isConstant, isGlobal, value, scope, line, column);
        
        if (!symbols.containsKey(name)) {
            symbols.put(name, new ArrayList<>());
        }
        
        symbols.get(name).add(symbol);
        return true;
    }
    
    // Lookup a symbol in the current scope
    public Symbol lookupInCurrentScope(String name) {
        if (!symbols.containsKey(name)) {
            return null;
        }
        
        List<Symbol> symbolList = symbols.get(name);
        for (int i = symbolList.size() - 1; i >= 0; i--) {
            Symbol symbol = symbolList.get(i);
            if (symbol.getScope() == currentScope) {
                return symbol;
            }
        }
        
        return null;
    }
    
    // Lookup a symbol in all accessible scopes (current and outer scopes)
    public Symbol lookup(String name) {
        if (!symbols.containsKey(name)) {
            return null;
        }
        
        List<Symbol> symbolList = symbols.get(name);
        
        // First check for global scope (always accessible)
        for (Symbol symbol : symbolList) {
            if (symbol.getScope() == 0) {
                return symbol;
            }
        }
        
        // Then check for the innermost scope that has this symbol
        for (int scope = currentScope; scope > 0; scope--) {
            for (Symbol symbol : symbolList) {
                if (symbol.getScope() == scope) {
                    return symbol;
                }
            }
        }
        
        return null;
    }
    
    // Update a symbol's value
    public boolean updateValue(String name, Object value) {
        Symbol symbol = lookup(name);
        if (symbol == null) {
            return false;
        }
        
        if (symbol.isConstant()) {
            return false; // Cannot update constants
        }
        
        symbol.setValue(value);
        return true;
    }
    
    // Get all symbols from the table
    public List<Symbol> getAllSymbols() {
        List<Symbol> allSymbols = new ArrayList<>();
        for (List<Symbol> symbolList : symbols.values()) {
            allSymbols.addAll(symbolList);
        }
        return allSymbols;
    }
    
    // Get symbols from the current scope
    public List<Symbol> getCurrentScopeSymbols() {
        List<Symbol> scopeSymbols = new ArrayList<>();
        for (List<Symbol> symbolList : symbols.values()) {
            for (Symbol symbol : symbolList) {
                if (symbol.getScope() == currentScope) {
                    scopeSymbols.add(symbol);
                }
            }
        }
        return scopeSymbols;
    }
    
    // Get symbols from a specific scope
    public List<Symbol> getScopeSymbols(int scope) {
        List<Symbol> scopeSymbols = new ArrayList<>();
        for (List<Symbol> symbolList : symbols.values()) {
            for (Symbol symbol : symbolList) {
                if (symbol.getScope() == scope) {
                    scopeSymbols.add(symbol);
                }
            }
        }
        return scopeSymbols;
    }
    
    // Display the entire symbol table
    public void display() {
        System.out.println("SYMBOL TABLE");
        System.out.println(String.format("%-15s %-10s %-10s %-10s %-10s %-15s %s",
                "NAME", "TYPE", "DATA TYPE", "KIND", "SCOPE", "VALUE", "SCOPE LEVEL"));
        System.out.println("-".repeat(80));
        
        List<Symbol> allSymbols = getAllSymbols();
        allSymbols.sort(Comparator
                        .comparing(Symbol::getScope)
                        .thenComparing(Symbol::getName));
        
        for (Symbol symbol : allSymbols) {
            System.out.println(symbol);
        }
    }
    
    // Example usage
   
}