package com.lab2;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        System.out.println("Lab 2: Automata conversion\n");

        // Task 1: Chomsky classification
        var lab1Grammar = new Grammar();
        System.out.println("[Task 1] Lab 1 Grammar classification:");
        System.out.println("  Result: " + lab1Grammar.classifyChomsky() + "\n");

        // Task 2: Setup FA
        var fa = new FiniteAutomaton();
        fa.setStartState("q0");
        fa.addFinalState("q2");

        fa.addTransition("q0", 'a', "q0");
        fa.addTransition("q0", 'a', "q1");
        fa.addTransition("q1", 'a', "q2");
        fa.addTransition("q1", 'b', "q1");
        fa.addTransition("q2", 'a', "q3");
        fa.addTransition("q3", 'a', "q1");

        System.out.println("[Task 2] Original Finite Automaton (v25):");
        fa.printTransitions();

        // Task 3: FA to Grammar
        System.out.println("\n[Task 3] Converting FA to regular grammar:");
        var convertedGrammar = fa.toRegularGrammar();
        convertedGrammar.printRules();

        // Task 4 & 5: NDFA to DFA Conversion
        System.out.println("\n[Task 4] Is original FA deterministic? " + fa.isDeterministic());

        System.out.println("\n[Task 5] Converting NDFA to DFA using subset construction...");
        var dfa = fa.toDFA();

        System.out.println("\nResulting DFA:");
        dfa.printTransitions();
        System.out.println("\nIs the new Automaton deterministic? " + dfa.isDeterministic());

        // Task 6: bonus (auto-generate graph)
        System.out.println("\n[Task 6] Generating visual graph.");
        var dotCode = dfa.toGraphviz();
        saveGraphFromAPI(dotCode, "dfa_variant25.png");
    }

    private static void saveGraphFromAPI(String dotString, String filename) {
        try {
            System.out.println("  Sending Graphviz data to API...");
            var encodedDot = URLEncoder.encode(dotString, StandardCharsets.UTF_8);
            var apiUrl = "https://quickchart.io/graphviz?format=png&graph=" + encodedDot;

            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                Files.write(Path.of(filename), response.body());
                System.out.println("  Success! Graph saved automatically as: " + filename);
            } else {
                System.out.println("  API error. Status code: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("  Failed to generate graph automatically: " + e.getMessage());
        }
    }
}