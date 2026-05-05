package com.lab6;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String isomorphSource = """
                diagram OrderSystem {
                    type: Sequence
                    theme: Blueprint
                    resolution: (x:1920, y:1080)
                }

                actor u : "Customer"
                participant f : "Frontend App"
                database d : "SQL Database"

                f -> b : "POST /order" {
                    style: bold
                }

                b -[dashed]-> d : "Insert Row"

                layout {
                    u: @(x:50, y:50)
                }
                """;

        System.out.println("Phase 1: Lexical analysis (Regex)");
        Lexer lexer = new Lexer(isomorphSource);
        List<Token> tokens = lexer.tokenize();
        System.out.println("Tokens generated: " + tokens.size());

        System.out.println("\nPhase 2: Syntax analysis (Parser)");
        Parser parser = new Parser(tokens);
        AST.ProgramNode ast = parser.parse();

        System.out.println("Abstract Syntax Tree:");
        System.out.println(ast);
    }
}