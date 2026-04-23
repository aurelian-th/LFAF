package com.lab3;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int pos = 0;
    private int line = 1;
    private int column = 1;

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        var tokens = new ArrayList<Token>();

        while (pos < input.length()) {
            char c = peek();

            // 1. Skip Whitespace
            if (Character.isWhitespace(c)) {
                advance();
                continue;
            }

            // 2. Skip Comments (// ...)
            if (c == '/' && peekNext() == '/') {
                while (pos < input.length() && peek() != '\n') {
                    advance();
                }
                continue; // Go to the next line
            }

            // 3. Extract Strings ("...")
            if (c == '"') {
                tokens.add(readString());
                continue;
            }

            // 4. Extract Identifiers and Keywords
            if (Character.isLetter(c)) {
                tokens.add(readIdentifierOrKeyword());
                continue;
            }

            // 5. Extract Numbers
            if (Character.isDigit(c)) {
                tokens.add(readNumber());
                continue;
            }

            // 6. Extract Symbols and Operators
            int startCol = column;

            // Check for multi-character arrow "->"
            if (c == '-' && peekNext() == '>') {
                advance();
                advance();
                tokens.add(new Token(TokenType.ARROW, "->", line, startCol));
                continue;
            }

            // Check single character symbols
            switch (c) {
                case '{':
                    tokens.add(new Token(TokenType.L_BRACE, "{", line, startCol));
                    advance();
                    break;
                case '}':
                    tokens.add(new Token(TokenType.R_BRACE, "}", line, startCol));
                    advance();
                    break;
                case '(':
                    tokens.add(new Token(TokenType.L_PAREN, "(", line, startCol));
                    advance();
                    break;
                case ')':
                    tokens.add(new Token(TokenType.R_PAREN, ")", line, startCol));
                    advance();
                    break;
                case '[':
                    tokens.add(new Token(TokenType.L_BRACKET, "[", line, startCol));
                    advance();
                    break;
                case ']':
                    tokens.add(new Token(TokenType.R_BRACKET, "]", line, startCol));
                    advance();
                    break;
                case ':':
                    tokens.add(new Token(TokenType.COLON, ":", line, startCol));
                    advance();
                    break;
                case ';':
                    tokens.add(new Token(TokenType.SEMICOLON, ";", line, startCol));
                    advance();
                    break;
                case ',':
                    tokens.add(new Token(TokenType.COMMA, ",", line, startCol));
                    advance();
                    break;
                case '@':
                    tokens.add(new Token(TokenType.AT, "@", line, startCol));
                    advance();
                    break;
                case '-':
                    tokens.add(new Token(TokenType.MINUS, "-", line, startCol));
                    advance();
                    break;
                default:
                    tokens.add(new Token(TokenType.UNKNOWN, String.valueOf(c), line, startCol));
                    advance();
                    break;
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    private Token readString() {
        int startCol = column;
        advance(); // Skip the opening quote
        var sb = new StringBuilder();

        while (pos < input.length() && peek() != '"') {
            sb.append(advance());
        }

        if (pos < input.length())
            advance(); // Skip the closing quote
        return new Token(TokenType.STRING, sb.toString(), line, startCol);
    }

    private Token readIdentifierOrKeyword() {
        int startCol = column;
        var sb = new StringBuilder();

        while (pos < input.length() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            sb.append(advance());
        }

        String text = sb.toString();
        TokenType type = switch (text) {
            case "diagram" -> TokenType.KW_DIAGRAM;
            case "type" -> TokenType.KW_TYPE;
            case "theme" -> TokenType.KW_THEME;
            case "resolution" -> TokenType.KW_RESOLUTION;
            case "actor" -> TokenType.KW_ACTOR;
            case "participant" -> TokenType.KW_PARTICIPANT;
            case "database" -> TokenType.KW_DATABASE;
            case "layout" -> TokenType.KW_LAYOUT;
            default -> TokenType.IDENTIFIER;
        };

        return new Token(type, text, line, startCol);
    }

    private Token readNumber() {
        int startCol = column;
        var sb = new StringBuilder();
        boolean hasDot = false;

        while (pos < input.length() && (Character.isDigit(peek()) || peek() == '.')) {
            if (peek() == '.') {
                if (hasDot)
                    break;
                hasDot = true;
            }
            sb.append(advance());
        }

        return new Token(hasDot ? TokenType.FLOAT : TokenType.INTEGER, sb.toString(), line, startCol);
    }

    private char peek() {
        return (pos >= input.length()) ? '\0' : input.charAt(pos);
    }

    private char peekNext() {
        return (pos + 1 >= input.length()) ? '\0' : input.charAt(pos + 1);
    }

    private char advance() {
        char c = input.charAt(pos++);
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }
}