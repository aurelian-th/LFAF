package com.lab6;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private final String input;

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        StringBuilder regexBuilder = new StringBuilder();

        // Build the master regex pattern using named capture groups
        for (TokenType type : TokenType.values()) {
            if (type == TokenType.EOF)
                continue;

            // FIX: Java named groups cannot contain underscores.
            // We strip them out just for the regex compiler (e.g., KW_DIAGRAM -> KWDIAGRAM)
            String groupName = type.name().replace("_", "");
            regexBuilder.append(String.format("|(?<%s>%s)", groupName, type.pattern));
        }

        Pattern pattern = Pattern.compile(regexBuilder.substring(1));
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            for (TokenType type : TokenType.values()) {
                if (type == TokenType.EOF)
                    continue;

                // Use the same stripped name to fetch the matching group
                String groupName = type.name().replace("_", "");
                String match = matcher.group(groupName);

                if (match != null) {
                    // Ignore whitespace and comments for the AST
                    if (type != TokenType.WHITESPACE && type != TokenType.COMMENT) {
                        tokens.add(new Token(type, match, 0, 0)); // Line/Col omitted for brevity
                    }
                    break;
                }
            }
        }
        tokens.add(new Token(TokenType.EOF, "", 0, 0));
        return tokens;
    }
}