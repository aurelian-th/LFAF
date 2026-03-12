# Laboratory Work 3: Lexer & scanner implementation

### Course: Formal Languages & Finite Automata
### Author: Tihon Aurelian-Mihai
### Group: FAF-241

----

## Theory
Lexical analysis is the foundational stage of any compiler or interpreter. A Lexer (or scanner/tokenizer) acts as the bridge between raw source code and the structural parsing engine. 

When a computer reads a file, it initially processes it as a meaningless, contiguous stream of raw characters. The Lexer's job is to read this stream, discard irrelevant formatting (like whitespace and comments), and group the remaining characters into meaningful chunks called **lexemes**. 


However, a raw lexeme is not enough for a compiler to understand context. The Lexer must categorize each lexeme into a **Token**. A Token is a structured data object that contains the "type" of the string (e.g., Keyword, Integer, Identifier) alongside critical metadata, such as the exact line and column number where it was found in the source code. Under the hood, a Lexer operates as a Deterministic Finite Automaton (DFA), advancing through states based on character lookaheads.

## Objectives
1. Understand the theoretical principles of lexical analysis.
2. Build a functional scanner capable of reading raw strings and outputting a stream of categorized tokens.
3. Move beyond simple calculator logic by implementing a Lexer for a custom Domain-Specific Language (DSL).
4. Integrate the Lexer into an ongoing Project-Based Learning (PBL) initiative.

## Implementation Description
For this laboratory work, I bypassed the standard "math calculator" example and instead implemented a robust lexer specifically designed for **Isomorph**, a bidirectional diagramming DSL I am developing for my PBL project.

The system is split into three core components:
1. **`TokenType.java`**: An enumeration defining the exact grammar vocabulary of the Isomorph DSL (like `KW_DIAGRAM`, `KW_ACTOR`, `ARROW`, `STRING`).
2. **`Token.java`**: A data class that stores the lexeme, its parsed `TokenType`, and positional metadata (`line` and `column`).
3. **`Lexer.java`**: The DFA engine. It processes the input string character-by-character using `peek()`, `peekNext()`, and `advance()` methods. 

The `lexer` intelligently delegates processing based on the current character. For example, if it detects a letter, it triggers `readIdentifierOrKeyword()`, which consumes alphanumeric characters until it hits a delimiter, and then uses a `switch` expression to determine if the lexeme is a reserved keyword (like `diagram` or `actor`) or a user-defined identifier. 

```java
// Snippet demonstrating string literal extraction
private Token readString() {
    int startCol = column;
    advance(); // Skip the opening quote
    var sb = new StringBuilder();
    
    while (pos < input.length() && peek() != '"') {
        sb.append(advance());
    }
    
    if (pos < input.length()) advance(); // Skip the closing quote
    return new Token(TokenType.STRING, sb.toString(), line, startCol);
}
```

## Challenges and difficulties

Implementing a custom DSL lexer presented several edge cases that required careful debugging and architectural planning:

1. **Multi-character operators:** The Isomorph DSL uses complex relational operators like `->` and `-[dashed]->`. Initially, the Lexer struggled to differentiate between a simple minus sign (`-`) and an arrow (`->`). I solved this by implementing a `peekNext()` lookahead function. If the current character is `-` and the next is `>`, it consumes both and produces an `ARROW` token. Otherwise, it defaults to a `MINUS` token, allowing complex strings like `-[dashed]->` to be correctly tokenized sequentially as `MINUS`, `L_BRACKET`, `IDENTIFIER`, `R_BRACKET`, `ARROW`.
2. **String boundaries:** Parsing text strings required a dedicated loop to ensure that spaces inside quotes (`"Frontend App"`) were not accidentally discarded by the global whitespace skipper.
3. **Line tracking consistency:** Properly managing the `line` and `column` metadata was surprisingly complex, especially when handling inline comments (`//`). When the Lexer encounters a comment, it must advance through the characters until it hits `\n`, update the line counter, reset the column counter to `1`, and then correctly resume tokenization without losing its place.

## Conclusions and results

I successfully implemented a production-ready lexer tailored to a real-world Domain-Specific Language. The lexer correctly identifies reserved keywords, groups complex string literals, parses numerical data, and handles multi-character punctuation operators.

By running the `Main.java` client, I fed a complex block of Isomorph diagramming code into the lexer. The output successfully stripped all comments and whitespace, returning a perfectly structured stream of Tokens, fully prepped for the parsing stage of the compiler.

**Console output snippet:**

```text
=== Isomorph DSL Lexer ===
Token(KW_DIAGRAM  , 'diagram') [Line: 2, Col: 1]
Token(IDENTIFIER  , 'OrderSystem') [Line: 2, Col: 9]
Token(L_BRACE     , '{') [Line: 2, Col: 21]
Token(KW_TYPE     , 'type') [Line: 3, Col: 5]
Token(COLON       , ':') [Line: 3, Col: 9]
Token(IDENTIFIER  , 'Sequence') [Line: 3, Col: 11]
...
Token(IDENTIFIER  , 'b') [Line: 19, Col: 1]
Token(MINUS       , '-') [Line: 19, Col: 3]
Token(L_BRACKET   , '[') [Line: 19, Col: 4]
Token(IDENTIFIER  , 'dashed') [Line: 19, Col: 5]
Token(R_BRACKET   , ']') [Line: 19, Col: 11]
Token(ARROW       , '->') [Line: 19, Col: 12]
Token(IDENTIFIER  , 'd') [Line: 19, Col: 15]
Token(COLON       , ':') [Line: 19, Col: 17]
Token(STRING      , 'Insert Row') [Line: 19, Col: 19]

```

## References

1. Course Notes: Formal Languages & Finite Automata.
2. LLVM Documentation: *My First Language Frontend*.
