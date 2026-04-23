import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Define V_N and V_T
        Set<String> nonTerminals = new HashSet<>(Arrays.asList("S", "A", "B", "C", "D"));
        Set<String> terminals = new HashSet<>(Arrays.asList("a", "b"));

        CNFConverter grammar = new CNFConverter(nonTerminals, terminals, "S");

        // Variant 25 Rules
        grammar.addProduction("S", "bA");
        grammar.addProduction("S", "BC");

        grammar.addProduction("A", "a");
        grammar.addProduction("A", "aS");
        grammar.addProduction("A", "bCaCa");

        grammar.addProduction("B", "A");
        grammar.addProduction("B", "bS");
        grammar.addProduction("B", "bCAa");

        grammar.addProduction("C", "e"); // Epsilon
        grammar.addProduction("C", "AB");

        grammar.addProduction("D", "AB");

        // Display original
        grammar.printGrammar("Initial grammar");

        // Process
        grammar.convertToCNF();
    }
}