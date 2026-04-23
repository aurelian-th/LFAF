package com.lab4;

public class Main {
    public static void main(String[] args) {
        // Variant 1
        String[] variant1Regexes = {
                "(a|b)(c|d)E^+G?",
                "P(Q|R|S)T(UV|W|X)^*Z^+",
                "1(0|1)^*2(3|4)^5 36"
        };

        System.out.println("Lab 4: Regular expression AST generator");

        for (String rawRegex : variant1Regexes) {
            // Clean the custom superscripts (^+ and ^*) into standard regex operators (+ and *)
            String cleanRegex = rawRegex.replace("^+", "+").replace("^*", "*");

            System.out.println("\n\n");
            System.out.println("Processing Regex: " + rawRegex);

            // 1. Build the AST
            RegexGenerator.Parser parser = new RegexGenerator.Parser(cleanRegex);
            RegexGenerator.Node root = parser.parse();

            // 2. Generate 3 random strings per regex
            for (int i = 1; i <= 3; i++) {
                StringBuilder traceLog = new StringBuilder();

                // Traverse the tree to generate the string
                String generatedString = root.generate(traceLog, "");

                System.out.println("\n[Generation #" + i + "] Result: " + generatedString);

                // Print the execution trace
                if (i == 1) {
                    System.out.println("\nExecution trace");
                    System.out.println(traceLog.toString());
                }
            }
        }
    }
}