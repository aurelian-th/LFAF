package com.lab3;

public enum TokenType {
    // Isomorph DSL Keywords
    KW_DIAGRAM, KW_TYPE, KW_THEME, KW_RESOLUTION,
    KW_ACTOR, KW_PARTICIPANT, KW_DATABASE, KW_LAYOUT,

    // Data Types
    IDENTIFIER, INTEGER, FLOAT, STRING,

    // Punctuation & Operators
    L_BRACE, R_BRACE, L_PAREN, R_PAREN, L_BRACKET, R_BRACKET,
    COLON, SEMICOLON, COMMA, AT, MINUS, ARROW,

    EOF, UNKNOWN
}