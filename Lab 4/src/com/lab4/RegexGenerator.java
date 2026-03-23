package com.lab4;

import java.util.*;

public class RegexGenerator {

    // AST node definitions

    abstract static class Node {
        abstract String generate(StringBuilder trace, String indent);
    }

    static class LiteralNode extends Node {
        char c;

        LiteralNode(char c) {
            this.c = c;
        }

        @Override
        String generate(StringBuilder trace, String indent) {
            trace.append(indent).append("Literal: '").append(c).append("'\n");
            return String.valueOf(c);
        }
    }

    static class ConcatNode extends Node {
        List<Node> children;

        ConcatNode(List<Node> children) {
            this.children = children;
        }

        @Override
        String generate(StringBuilder trace, String indent) {
            trace.append(indent).append("Concatenation:\n");
            StringBuilder sb = new StringBuilder();
            for (Node child : children) {
                sb.append(child.generate(trace, indent + "  "));
            }
            return sb.toString();
        }
    }

    static class AltNode extends Node {
        List<Node> choices;

        AltNode(List<Node> choices) {
            this.choices = choices;
        }

        @Override
        String generate(StringBuilder trace, String indent) {
            int choice = new Random().nextInt(choices.size());
            trace.append(indent).append("Alternation (Chose branch ").append(choice + 1).append("):\n");
            return choices.get(choice).generate(trace, indent + "  ");
        }
    }

    static class RepeatNode extends Node {
        Node child;
        int min, max;
        String symbol;

        RepeatNode(Node child, int min, int max, String symbol) {
            this.child = child;
            this.min = min;
            this.max = max;
            this.symbol = symbol;
        }

        @Override
        String generate(StringBuilder trace, String indent) {
            int times = min + new Random().nextInt((max - min) + 1);
            trace.append(indent).append("Repetition '").append(symbol).append("' (Looping ").append(times)
                    .append(" times):\n");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < times; i++) {
                sb.append(child.generate(trace, indent + "  "));
            }
            return sb.toString();
        }
    }

    // The recursive descent parser

    static class Parser {
        String input;
        int pos = 0;

        Parser(String input) {
            // Keep the raw input, do not strip spaces globally
            this.input = input;
        }

        // Helper to dynamically skip whitespace during parsing
        private void skipWhitespace() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
        }

        Node parse() {
            return parseExpr();
        }

        private Node parseExpr() {
            List<Node> choices = new ArrayList<>();
            choices.add(parseTerm());

            skipWhitespace();
            while (peek() == '|') {
                advance(); // consume '|'
                choices.add(parseTerm());
                skipWhitespace();
            }

            return choices.size() == 1 ? choices.get(0) : new AltNode(choices);
        }

        private Node parseTerm() {
            List<Node> sequence = new ArrayList<>();

            skipWhitespace();
            while (hasNext() && peek() != '|' && peek() != ')') {
                sequence.add(parseFactor());
                skipWhitespace();
            }

            return sequence.size() == 1 ? sequence.get(0) : new ConcatNode(sequence);
        }

        private Node parseFactor() {
            Node base = parseBase();

            skipWhitespace();
            // Handle Modifiers (*, +, ?, ^n)
            while (hasNext() && (peek() == '*' || peek() == '+' || peek() == '?' || peek() == '^')) {
                char mod = advance();
                if (mod == '*') {
                    base = new RepeatNode(base, 0, 5, "*");
                } else if (mod == '+') {
                    base = new RepeatNode(base, 1, 5, "+");
                } else if (mod == '?') {
                    base = new RepeatNode(base, 0, 1, "?");
                } else if (mod == '^') {
                    StringBuilder numStr = new StringBuilder();
                    skipWhitespace(); // Skip spaces just in case it's written like `^ 5`

                    // This loop will now safely stop at the space after the '5'
                    while (hasNext() && Character.isDigit(peek())) {
                        numStr.append(advance());
                    }

                    int exactTimes = Integer.parseInt(numStr.toString());
                    base = new RepeatNode(base, exactTimes, exactTimes, "^" + exactTimes);
                }
                skipWhitespace();
            }
            return base;
        }

        private Node parseBase() {
            skipWhitespace();
            char c = advance();
            if (c == '(') {
                Node expr = parseExpr();
                skipWhitespace();
                if (hasNext() && peek() == ')') {
                    advance(); // Consume closing parenthesis safely
                }
                return expr;
            } else {
                return new LiteralNode(c);
            }
        }

        private char peek() {
            return (pos >= input.length()) ? '\0' : input.charAt(pos);
        }

        private char advance() {
            return input.charAt(pos++);
        }

        private boolean hasNext() {
            return pos < input.length();
        }
    }
}