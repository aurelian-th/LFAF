package com.lab3;

public class Main {
    public static void main(String[] args) {
        String isomorphSource = """
                // 1. Header
                diagram OrderSystem {
                    type: Sequence
                    theme: Blueprint
                    resolution: (x:1920, y:1080)
                }

                // 2. Body (Logic & Content)
                actor u : "Customer"
                participant f : "Frontend App"
                database d : "SQL Database"

                // 3. Relationships
                // Complex call with return
                f -> b : "POST /order" {
                    style: bold
                }

                // Database interaction
                b -[dashed]-> d : "Insert Row"

                // 4. Layout
                layout {
                    u: @(x:50, y:50)
                }
                """;

        System.out.println("Isomorph DSL lexer");
        var lexer = new Lexer(isomorphSource);
        var tokens = lexer.tokenize();

        for (var token : tokens) {
            System.out.println(token);
        }
    }
}