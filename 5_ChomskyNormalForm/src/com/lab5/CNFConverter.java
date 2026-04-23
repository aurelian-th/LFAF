import java.util.*;

public class CNFConverter {

    private Set<String> nonTerminals;
    private Set<String> terminals;
    private String startSymbol;

    // Maps a non-terminal to a set of its RHS productions.
    // "A" -> { ["b", "C", "a"], ["a", "S"] }
    private Map<String, Set<List<String>>> productions;

    // Counters for generating new variables in Step 5
    private int yCounter = 1;
    private Map<String, String> termVarCache = new HashMap<>();
    private Map<String, String> pairVarCache = new HashMap<>();

    public CNFConverter(Set<String> nonTerminals, Set<String> terminals, String startSymbol) {
        this.nonTerminals = new HashSet<>(nonTerminals);
        this.terminals = new HashSet<>(terminals);
        this.startSymbol = startSymbol;
        this.productions = new HashMap<>();

        for (String nt : nonTerminals) {
            productions.put(nt, new HashSet<>());
        }
    }

    public void addProduction(String lhs, String rhsStr) {
        if (!productions.containsKey(lhs)) {
            productions.put(lhs, new HashSet<>());
            nonTerminals.add(lhs);
        }

        // Convert "bCa" to ["b", "C", "a"] so we can handle multi-char symbols later
        List<String> rhs = new ArrayList<>();
        if (rhsStr.equals("e")) {
            rhs.add("e"); // Epsilon
        } else {
            for (char c : rhsStr.toCharArray()) {
                rhs.add(String.valueOf(c));
            }
        }
        productions.get(lhs).add(rhs);
    }

    public void printGrammar(String stepName) {
        System.out.println(stepName);
        for (String nt : nonTerminals) {
            if (productions.containsKey(nt) && !productions.get(nt).isEmpty()) {
                System.out.print(nt + " -> ");
                List<String> rhsStrings = new ArrayList<>();
                for (List<String> rhs : productions.get(nt)) {
                    rhsStrings.add(String.join("", rhs));
                }
                System.out.println(String.join(" | ", rhsStrings));
            }
        }
        System.out.println();
    }

    public void convertToCNF() {
        eliminateEpsilon();
        printGrammar("Step 1: Eliminate epsilon productions");

        eliminateUnit();
        printGrammar("Step 2: Eliminate unit productions");

        eliminateInaccessible();
        printGrammar("Step 3: Eliminate inaccessible symbols");

        eliminateNonProduction();
        printGrammar("Step 4: Eliminate non-productive symbols");

        convertToStrictCNF();
        printGrammar("Step 5: Chomsky normal form");
    }

    // Step 1 - eliminate epsilon
    private void eliminateEpsilon() {
        Set<String> nullables = new HashSet<>();

        // Find direct nullables (A -> e)
        for (String nt : nonTerminals) {
            for (List<String> rhs : productions.get(nt)) {
                if (rhs.size() == 1 && rhs.get(0).equals("e")) {
                    nullables.add(nt);
                }
            }
        }

        // Remove the explicit 'e' productions
        for (String nt : nonTerminals) {
            productions.get(nt).removeIf(rhs -> rhs.size() == 1 && rhs.get(0).equals("e"));
        }

        // Generate combinations without nullables
        Map<String, Set<List<String>>> newProductions = new HashMap<>();
        for (String nt : nonTerminals) {
            newProductions.put(nt, new HashSet<>());
            for (List<String> rhs : productions.get(nt)) {
                Set<List<String>> combos = new HashSet<>();
                generateCombinations(rhs, 0, nullables, new ArrayList<>(), combos);
                newProductions.get(nt).addAll(combos);
            }
        }
        productions = newProductions;
    }

    private void generateCombinations(List<String> original, int index, Set<String> nullables,
            List<String> current, Set<List<String>> result) {
        if (index == original.size()) {
            if (!current.isEmpty()) {
                result.add(new ArrayList<>(current));
            }
            return;
        }

        String symbol = original.get(index);

        // Branch 1 - keep the symbol
        current.add(symbol);
        generateCombinations(original, index + 1, nullables, current, result);
        current.remove(current.size() - 1);

        // Branch 2 - omit the symbol (if it's nullable)
        if (nullables.contains(symbol)) {
            generateCombinations(original, index + 1, nullables, current, result);
        }
    }

    // Step 2 - eliminate unit productions
    private void eliminateUnit() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String nt : new ArrayList<>(nonTerminals)) {
                Set<List<String>> newRhsSet = new HashSet<>();
                Set<List<String>> toRemove = new HashSet<>();

                for (List<String> rhs : productions.get(nt)) {
                    // Check if it's a unit production (e.g., A -> B)
                    if (rhs.size() == 1 && nonTerminals.contains(rhs.get(0))) {
                        String targetNt = rhs.get(0);
                        toRemove.add(rhs); // Mark for removal

                        // Add target's productions to current
                        for (List<String> targetRhs : productions.get(targetNt)) {
                            if (!targetRhs.equals(Collections.singletonList(nt))) { // Avoid A -> A
                                if (newRhsSet.add(targetRhs)) {
                                    changed = true; // New rule added
                                }
                            }
                        }
                    }
                }
                productions.get(nt).removeAll(toRemove);
                productions.get(nt).addAll(newRhsSet);
            }
        }
    }

    // Step 3 - eliminate inaccessible symbols
    private void eliminateInaccessible() {
        Set<String> reachable = new HashSet<>();
        reachable.add(startSymbol);

        boolean changed = true;
        while (changed) {
            changed = false;
            Set<String> newReachable = new HashSet<>(reachable);
            for (String nt : reachable) {
                if (!productions.containsKey(nt))
                    continue;
                for (List<String> rhs : productions.get(nt)) {
                    for (String symbol : rhs) {
                        if (nonTerminals.contains(symbol)) {
                            newReachable.add(symbol);
                        }
                    }
                }
            }
            if (newReachable.size() > reachable.size()) {
                reachable.addAll(newReachable);
                changed = true;
            }
        }

        // Remove unreachable variables
        nonTerminals.retainAll(reachable);
        productions.keySet().retainAll(reachable);
    }

    // Step 4 - eliminate non-productive symbols
    private void eliminateNonProduction() {
        Set<String> productive = new HashSet<>();
        boolean changed = true;

        while (changed) {
            changed = false;
            for (String nt : nonTerminals) {
                if (productive.contains(nt))
                    continue;

                for (List<String> rhs : productions.get(nt)) {
                    boolean isProductive = true;
                    for (String symbol : rhs) {
                        if (nonTerminals.contains(symbol) && !productive.contains(symbol)) {
                            isProductive = false;
                            break;
                        }
                    }
                    if (isProductive) {
                        productive.add(nt);
                        changed = true;
                        break;
                    }
                }
            }
        }

        nonTerminals.retainAll(productive);
        productions.keySet().retainAll(productive);

        // Clean up RHS containing non-productive symbols
        for (String nt : nonTerminals) {
            productions.get(nt).removeIf(rhs -> {
                for (String symbol : rhs) {
                    if (nonTerminals.contains(symbol) && !productive.contains(symbol)) {
                        return true;
                    }
                }
                return false;
            });
        }
    }

    // Step 5 - strict CNF cascade
    private void convertToStrictCNF() {
        Map<String, Set<List<String>>> newProductions = new HashMap<>();

        for (String nt : new ArrayList<>(nonTerminals)) {
            newProductions.put(nt, new HashSet<>());
            for (List<String> rhs : productions.get(nt)) {
                List<String> currentRhs = new ArrayList<>(rhs);

                // 1. Replace terminals with X_ variables if RHS length > 1
                if (currentRhs.size() > 1) {
                    for (int i = 0; i < currentRhs.size(); i++) {
                        String symbol = currentRhs.get(i);
                        if (terminals.contains(symbol)) {
                            String termVar = getOrCreateTermVar(symbol, newProductions);
                            currentRhs.set(i, termVar);
                        }
                    }
                }

                // 2. Cascade long RHS (Length >= 3) right-to-left
                while (currentRhs.size() > 2) {
                    int lastIdx = currentRhs.size() - 1;
                    String right = currentRhs.remove(lastIdx);
                    String left = currentRhs.remove(lastIdx - 1);

                    String pairVar = getOrCreatePairVar(left, right, newProductions);
                    currentRhs.add(pairVar);
                }

                newProductions.get(nt).add(currentRhs);
            }
        }

        // Merge the newly created variables back into the main grammar
        for (Map.Entry<String, Set<List<String>>> entry : newProductions.entrySet()) {
            if (productions.containsKey(entry.getKey())) {
                productions.get(entry.getKey()).clear();
            }
             else {
                productions.put(entry.getKey(), new HashSet<>());
            }
            productions.get(entry.getKey()).addAll(entry.getValue());
        }
    }

    private String getOrCreateTermVar(String terminal, Map<String, Set<List<String>>> newProductions) {
        if (!termVarCache.containsKey(terminal)) {
            String newNt = "X_" + terminal;
            termVarCache.put(terminal, newNt);
            nonTerminals.add(newNt);

            Set<List<String>> rules = new HashSet<>();
            rules.add(Collections.singletonList(terminal));
            newProductions.put(newNt, rules);
        }
        return termVarCache.get(terminal);
    }

    private String getOrCreatePairVar(String left, String right, Map<String, Set<List<String>>> newProductions) {
        String key = left + "-" + right;
        if (!pairVarCache.containsKey(key)) {
            String newNt = "Y_" + (yCounter++);
            pairVarCache.put(key, newNt);
            nonTerminals.add(newNt);

            Set<List<String>> rules = new HashSet<>();
            rules.add(Arrays.asList(left, right));
            newProductions.put(newNt, rules);
        }
        return pairVarCache.get(key);
    }
}