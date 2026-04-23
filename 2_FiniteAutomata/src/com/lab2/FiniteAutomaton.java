package com.lab2;

import java.util.*;

public class FiniteAutomaton {
    private String startState;
    private final Set<String> finalStates;
    private final Map<String, Map<Character, Set<String>>> transitions;

    public FiniteAutomaton() {
        this.transitions = new HashMap<>();
        this.finalStates = new HashSet<>();
    }

    public void setStartState(String state) {
        this.startState = state;
    }

    public void addFinalState(String state) {
        this.finalStates.add(state);
    }

    public void addTransition(String from, char input, String to) {
        transitions.computeIfAbsent(from, k -> new HashMap<>())
                .computeIfAbsent(input, k -> new HashSet<>())
                .add(to);
    }

    public boolean isDeterministic() {
        for (var stateTransitions : transitions.values()) {
            for (var destinations : stateTransitions.values()) {
                if (destinations.size() > 1)
                    return false;
            }
        }
        return true;
    }

    public Grammar toRegularGrammar() {
        var grammar = new Grammar();
        grammar.clearRules(); // Erase lab 1 data

        for (var state : transitions.keySet()) {
            var stateTransitions = transitions.get(state);
            for (var input : stateTransitions.keySet()) {
                for (var nextState : stateTransitions.get(input)) {
                    grammar.addRule(state, input + nextState);
                    if (finalStates.contains(nextState)) {
                        grammar.addRule(state, String.valueOf(input));
                    }
                }
            }
        }
        return grammar;
    }

    public FiniteAutomaton toDFA() {
        if (this.isDeterministic())
            return this;

        var dfa = new FiniteAutomaton();
        Queue<Set<String>> queue = new LinkedList<>();
        Set<Set<String>> processed = new HashSet<>();

        Set<String> startMacroState = Set.of(this.startState);
        queue.add(startMacroState);
        processed.add(startMacroState);
        dfa.setStartState(formatName(startMacroState));

        Set<Character> alphabet = Set.of('a', 'b');

        while (!queue.isEmpty()) {
            Set<String> currentMacro = queue.poll();
            String currentName = formatName(currentMacro);

            for (String state : currentMacro) {
                if (this.finalStates.contains(state)) {
                    dfa.addFinalState(currentName);
                    break;
                }
            }

            for (char symbol : alphabet) {
                Set<String> nextMacro = new TreeSet<>();

                for (String state : currentMacro) {
                    var map = transitions.get(state);
                    if (map != null && map.containsKey(symbol)) {
                        nextMacro.addAll(map.get(symbol));
                    }
                }

                if (!nextMacro.isEmpty()) {
                    String nextName = formatName(nextMacro);
                    dfa.addTransition(currentName, symbol, nextName);

                    if (!processed.contains(nextMacro)) {
                        queue.add(nextMacro);
                        processed.add(nextMacro);
                    }
                }
            }
        }
        return dfa;
    }

    private String formatName(Set<String> states) {
        return "[" + String.join(",", new TreeSet<>(states)) + "]";
    }

    public String toGraphviz() {
        var sb = new StringBuilder();
        sb.append("digraph FA {\n  rankdir=LR;\n  node [shape = doublecircle]; ");
        for (String fs : finalStates)
            sb.append("\"").append(fs).append("\" ");
        sb.append(";\n  node [shape = circle];\n");
        sb.append("  start [shape=point];\n  start -> \"").append(startState).append("\";\n");

        for (var from : transitions.keySet()) {
            for (var input : transitions.get(from).keySet()) {
                for (var to : transitions.get(from).get(input)) {
                    sb.append("  \"").append(from).append("\" -> \"").append(to)
                            .append("\" [label=\"").append(input).append("\"];\n");
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public void printTransitions() {
        System.out.println("  Start state: " + startState);
        System.out.println("  Final states: " + finalStates);
        System.out.println("  Transitions:");
        for (var state : transitions.keySet()) {
            for (var input : transitions.get(state).keySet()) {
                System.out.println("    δ(" + state + ", " + input + ") = " + transitions.get(state).get(input));
            }
        }
    }
}