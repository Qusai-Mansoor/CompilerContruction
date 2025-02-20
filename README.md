# IQ Language - Custom Programming Language

## Overview
IQ is a custom-designed programming language created as part of a **Compiler Construction** course project. The goal of IQ is to explore compiler design principles by defining a unique syntax, keywords, and lexical structure while implementing a **Lexical Analyzer, NFA (Non-Deterministic Finite Automaton), DFA (Deterministic Finite Automaton), and Symbol Table**.

## Features
- **Custom File Extension:** `.iq`
- **Support for Reserved Keywords, Operators, and Punctuation**
- **Lexical Analysis using Thompson’s Algorithm (RE → ε-NFA → DFA Conversion)**
- **Symbol Table for Variable & Function Tracking**
- **Error Handling for Unrecognized Tokens**

## Reserved Words
IQ has the following **reserved words**, categorized as follows:

### Keywords
```plaintext
num, return, void, let, else, repeat, loop, out, in
```

### Data Types
```plaintext
num (integer), deci (decimal), cond (boolean), letter (character)
```

### Operators
```plaintext
=, +, -, *, /
```

### Punctuators
```plaintext
(, ), {, }, ;, ,
```

### Comments
- **Single-line:** `// This is a comment`
- **Multi-line:** `/* This is a multi-line comment */`

## Lexical Analysis Workflow
1. **Regular Expressions (RE):** Defined for different tokens.
2. **Convert RE to ε-NFA:** Using **Thompson’s Algorithm**.
3. **Convert ε-NFA to DFA:** Using **Subset Construction Algorithm**.
4. **Tokenization:** DFA scans the source code and generates tokens.
5. **Symbol Table Handling:** Stores identifiers and their types.
6. **Error Handling:** Reports unrecognized tokens.

## Example Code in IQ
```iq
num main() {
    cond check = true;
    num a = 2;
    deci b = 0.12345;
    letter c = 'c';
    
    let (check) {
        a = a + 1;
    } else {
        return 0;
    }
}
```

## Project Structure
```
IQ-Language/
│── src/
│   │── Main.java         # Entry point for Lexical Analysis
│   │── LexicalAnalyser.java # Tokenizer using DFA
│   │── NFA.java          # NFA Construction (Thompson’s Algorithm)
│   │── DFA.java          # DFA Construction (Subset Construction Algorithm)
│   │── State.java        # State representation for NFA/DFA
│   │── Token.java        # Token structure
│   └── SymbolTable.java  # Stores Identifiers and Functions
│── examples/
│   └── test.iq          # Example IQ source code
└── README.md
```

## How to Run
1. Clone the repository:
   ```sh
   git clone https://github.com/yourusername/IQ-Language.git
   cd IQ-Language/src
   ```
2. Compile and run the program:
   ```sh
   javac Main.java
   java Main ../examples/test.iq
   ```

## Future Enhancements
- Implement **Parsing & Syntax Analysis**.
- Add **Semantic Analysis** & **Code Generation**.
- Extend **Operators & Data Structures**.

## License
This project is for educational purposes. Feel free to contribute and enhance the IQ Language!

---

**Contributors:** Qusai Mansoor (@QusaiMansoor)  Arban Arfan (@ArbanArfan)

