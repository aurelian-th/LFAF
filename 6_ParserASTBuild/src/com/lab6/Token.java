package com.lab6;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final int line;
    public final int column;

    public Token(TokenType type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        // Formatted to look clean in the console
        return String.format("Token(%-14s, '%s')", type.name(), lexeme);
    }
}