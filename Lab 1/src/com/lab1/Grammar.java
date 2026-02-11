package com.lab1;

import java.util.*;

public class Grammar {
    // Fields must still use explicit types (Java limitation)
    private final Set<String> vn;
    private final Set<String> vt;
    private final Map<String, List<String>> productions;
    private final String startSymbol;

    public Grammar() {
        this.vn = Set.of("S", "A", "B");
        this.vt = Set.of("a", "b", "c", "d");
        this.startSymbol = "S";
        
        // Using a HashMap because we need a mutable list for values
        this.productions = new HashMap<>();
        
        // Hardcoding Variant 25 Rules
        addRule("S", "bS");
        addRule("S", "dA");
        addRule("A", "aA");
        addRule("A", "dB");
        addRule("B", "cB");
        addRule("A", "b");  // Terminal rule
        addRule("B", "a");  // Terminal rule
    }

    private void addRule(String key, String value) {
        productions.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    public String generateString() {
        var word = new StringBuilder();
        var currentSymbol = startSymbol;
        var rand = new Random();

        // While our current symbol is a Variable (S, A, B...)
        while (vn.contains(currentSymbol)) {
            var rules = productions.get(currentSymbol);
            var rule = rules.get(rand.nextInt(rules.size())); // Pick random rule

            // Parse the rule (e.g., "bS" or just "b")
            var nextSymbol = "";
            for (char c : rule.toCharArray()) {
                var s = String.valueOf(c);
                if (vt.contains(s)) {
                    word.append(s); // It's a terminal (letter), add to word
                } else if (vn.contains(s)) {
                    nextSymbol = s; // It's a variable, this is our next state
                }
            }
            currentSymbol = nextSymbol; // Update state. If empty, loop ends.
        }
        return word.toString();
    }

    public FiniteAutomaton toFiniteAutomaton() {
        var fa = new FiniteAutomaton();
        fa.setStartState(this.startSymbol);

        // Convert Grammar Rules to Automaton Paths
        for (var entry : productions.entrySet()) {
            var fromState = entry.getKey();
            
            for (var rule : entry.getValue()) {
                var inputChar = rule.charAt(0); // The terminal is always first (e.g., 'b' in "bS")
                
                if (rule.length() > 1) {
                    // Rule like "bS" -> Transition to S
                    var toState = String.valueOf(rule.charAt(1));
                    fa.addTransition(fromState, inputChar, toState);
                } else {
                    // Rule like "b" -> Transition to Finish
                    fa.addTransition(fromState, inputChar, "Final");
                }
            }
        }
        fa.addFinalState("Final");
        return fa;
    }
}