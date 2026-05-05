package com.lab6;

public enum TokenType {
    // Keywords (Must come first to take precedence over identifiers)
    KW_DIAGRAM("diagram\\b"), KW_TYPE("type\\b"), KW_THEME("theme\\b"), KW_RESOLUTION("resolution\\b"),
    KW_ACTOR("actor\\b"), KW_PARTICIPANT("participant\\b"), KW_DATABASE("database\\b"), KW_LAYOUT("layout\\b"),

    // Punctuation & Operators (Escaped where necessary)
    ARROW("->"), MINUS("-"), L_BRACE("\\{"), R_BRACE("\\}"), L_PAREN("\\("), R_PAREN("\\)"),
    L_BRACKET("\\["), R_BRACKET("\\]"), COLON(":"), SEMICOLON(";"), COMMA(","), AT("@"),

    // Data Types
    IDENTIFIER("[a-zA-Z_][a-zA-Z0-9_]*"),
    FLOAT("\\d+\\.\\d+"),
    INTEGER("\\d+"),
    STRING("\"[^\"]*\""),

    // Ignored tokens
    COMMENT("//[^\n]*"),
    WHITESPACE("[ \\t\\n\\r]+"),

    EOF("");

    public final String pattern;

    TokenType(String pattern) {
        this.pattern = pattern;
    }
}