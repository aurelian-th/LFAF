package com.lab6;

import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public AST.ProgramNode parse() {
        AST.ProgramNode program = new AST.ProgramNode();

        while (!isAtEnd()) {
            Token token = peek();
            if (token.type == TokenType.KW_DIAGRAM) {
                program.header = parseHeader();
            } else if (token.type == TokenType.KW_ACTOR || token.type == TokenType.KW_PARTICIPANT
                    || token.type == TokenType.KW_DATABASE) {
                program.entities.add(parseEntity());
            } else if (token.type == TokenType.IDENTIFIER) {
                program.relationships.add(parseRelationship());
            } else if (token.type == TokenType.KW_LAYOUT) {
                program.layout = parseLayout();
            } else {
                advance(); // Skip unexpected tokens for fault tolerance
            }
        }
        return program;
    }

    private AST.HeaderNode parseHeader() {
        AST.HeaderNode header = new AST.HeaderNode();
        consume(TokenType.KW_DIAGRAM);
        header.name = consume(TokenType.IDENTIFIER).lexeme;
        consume(TokenType.L_BRACE);

        while (peek().type != TokenType.R_BRACE && !isAtEnd()) {
            if (match(TokenType.KW_TYPE)) {
                consume(TokenType.COLON);
                header.type = consume(TokenType.IDENTIFIER).lexeme;
            } else if (match(TokenType.KW_THEME)) {
                consume(TokenType.COLON);
                header.theme = consume(TokenType.IDENTIFIER).lexeme;
            } else if (match(TokenType.KW_RESOLUTION)) {
                consume(TokenType.COLON);
                consume(TokenType.L_PAREN);
                consume(TokenType.IDENTIFIER); // x
                consume(TokenType.COLON);
                header.resX = Integer.parseInt(consume(TokenType.INTEGER).lexeme);
                consume(TokenType.COMMA);
                consume(TokenType.IDENTIFIER); // y
                consume(TokenType.COLON);
                header.resY = Integer.parseInt(consume(TokenType.INTEGER).lexeme);
                consume(TokenType.R_PAREN);
            }
        }
        consume(TokenType.R_BRACE);
        return header;
    }

    private AST.EntityNode parseEntity() {
        String type = advance().lexeme; // actor, participant, etc.
        String id = consume(TokenType.IDENTIFIER).lexeme;
        consume(TokenType.COLON);
        String label = consume(TokenType.STRING).lexeme.replace("\"", "");
        return new AST.EntityNode(type, id, label);
    }

    private AST.RelationshipNode parseRelationship() {
        AST.RelationshipNode rel = new AST.RelationshipNode();
        rel.sourceId = consume(TokenType.IDENTIFIER).lexeme;

        // Handle standard '->' or complex '-[type]->'
        if (match(TokenType.ARROW)) {
            rel.connectionType = "solid"; // Default
        } else if (match(TokenType.MINUS)) {
            consume(TokenType.L_BRACKET);
            rel.connectionType = consume(TokenType.IDENTIFIER).lexeme;
            consume(TokenType.R_BRACKET);
            consume(TokenType.ARROW);
        }

        rel.targetId = consume(TokenType.IDENTIFIER).lexeme;
        consume(TokenType.COLON);
        rel.label = consume(TokenType.STRING).lexeme.replace("\"", "");

        // Parse optional property block { style: bold }
        if (match(TokenType.L_BRACE)) {
            while (peek().type != TokenType.R_BRACE && !isAtEnd()) {
                String key = consume(TokenType.IDENTIFIER).lexeme;
                consume(TokenType.COLON);
                String val = consume(TokenType.IDENTIFIER).lexeme;
                rel.properties.put(key, val);
            }
            consume(TokenType.R_BRACE);
        }
        return rel;
    }

    private AST.LayoutNode parseLayout() {
        AST.LayoutNode layout = new AST.LayoutNode();
        consume(TokenType.KW_LAYOUT);
        consume(TokenType.L_BRACE);

        while (peek().type != TokenType.R_BRACE && !isAtEnd()) {
            String id = consume(TokenType.IDENTIFIER).lexeme;
            consume(TokenType.COLON);
            consume(TokenType.AT);
            consume(TokenType.L_PAREN);
            consume(TokenType.IDENTIFIER);
            consume(TokenType.COLON);
            String x = consume(TokenType.INTEGER).lexeme;
            consume(TokenType.COMMA);
            consume(TokenType.IDENTIFIER);
            consume(TokenType.COLON);
            String y = consume(TokenType.INTEGER).lexeme;
            consume(TokenType.R_PAREN);

            layout.coordinates.put(id, "x:" + x + ", y:" + y);
        }
        consume(TokenType.R_BRACE);
        return layout;
    }

    // Helper methods
    private boolean match(TokenType type) {
        if (peek().type == type) {
            advance();
            return true;
        }
        return false;
    }

    private Token consume(TokenType type) {
        if (peek().type == type)
            return advance();
        throw new RuntimeException("Expected " + type + " but found " + peek().type);
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token advance() {
        return tokens.get(pos++);
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }
}