package com.lab1;

import java.util.*;

public class FiniteAutomaton {
    private String startState;
    private final Set<String> finalStates;
    private final Map<String, Map<Character, String>> transitions;

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
        // "computeIfAbsent" is a great modern Java idiom.
        // It says: "If 'from' isn't in the map, put a new HashMap there, then return it."
        transitions.computeIfAbsent(from, k -> new HashMap<>()).put(input, to);
    }

    public boolean stringBelongToLanguage(String inputString) {
        var currentState = startState;
        
        for (char c : inputString.toCharArray()) {
            // Check if we have a path from currentState using 'c'
            var stateTransitions = transitions.get(currentState);
            
            if (stateTransitions != null && stateTransitions.containsKey(c)) {
                currentState = stateTransitions.get(c); // Move to next state
            } else {
                return false; // Road block! Invalid word.
            }
        }
        
        // We finished reading the word. Are we at a valid finish line?
        return finalStates.contains(currentState);
    }
}