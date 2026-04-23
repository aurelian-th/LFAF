package com.lab2;

import java.util.*;

public class Grammar {
    private final Set<String> vn;
    private final Set<String> vt;
    private final Map<String, List<String>> productions;
    private String startSymbol;

    public Grammar() {
        // to make them mutable so they can get cleared
        this.vn = new HashSet<>(Arrays.asList("S", "A", "B"));
        this.vt = new HashSet<>(Arrays.asList("a", "b", "c", "d"));
        this.startSymbol = "S";
        this.productions = new HashMap<>();

        // Lab 1 rules
        addRule("S", "bS");
        addRule("S", "dA");
        addRule("A", "aA");
        addRule("A", "dB");
        addRule("A", "b");
        addRule("B", "cB");
        addRule("B", "a");
    }

    public void addRule(String key, String value) {
        productions.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    // clear out lab 1 rules
    public void clearRules() {
        this.productions.clear();
        this.vn.clear();
        this.vt.clear();
        this.startSymbol = "";
    }

    public String classifyChomsky() {
        boolean isRegular = true;
        boolean isContextFree = true;
        boolean isContextSensitive = true;

        for (var entry : productions.entrySet()) {
            String lhs = entry.getKey();
            for (String rhs : entry.getValue()) {
                if (lhs.length() > 1) {
                    isRegular = false;
                    isContextFree = false;
                }
                if (lhs.length() > rhs.length() && !rhs.equals("epsilon")) {
                    isContextSensitive = false;
                }
                if (isRegular) {
                    if (rhs.length() > 2)
                        isRegular = false;
                    if (rhs.length() == 1 && !vt.contains(rhs) && !rhs.equals("epsilon"))
                        isRegular = false;
                    if (rhs.length() == 2 && !(vt.contains(String.valueOf(rhs.charAt(0)))
                            && vn.contains(String.valueOf(rhs.charAt(1))))) {
                        isRegular = false;
                    }
                }
            }
        }

        if (isRegular)
            return "Type 3 (Regular Grammar)";
        if (isContextFree)
            return "Type 2 (Context-free Grammar)";
        if (isContextSensitive)
            return "Type 1 (Context-sensitive Grammar)";
        return "Type 0 (Unrestricted Grammar)";
    }

    public void printRules() {
        for (var entry : productions.entrySet()) {
            System.out.println("  " + entry.getKey() + " -> " + String.join(" | ", entry.getValue()));
        }
    }
}