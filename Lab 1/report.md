# Laboratory Work 1: Intro to Formal Languages, Regular Grammars, and Finite Automata

### Course: Formal languages & finite automata

### Author: Tihon Aurelian-Mihai

### Group: FAF-241

---

## Theory

A formal language is essentially a set of strings formed from a specific alphabet that follow a specific set of rules. In this laboratory work, we focus on regular grammars (Type 3 in the Chomsky Hierarchy), which are the simplest form of grammars.

A grammar consists of four parts: $G = (V_N, V_T, P, S)$.
* **$V_N$ (non-terminals):** Variables that can be replaced (ex. S, A, B).
* **$V_T$ (terminals):** The actual characters of the language (ex. a, b, c).
* **$P$ (productions):** The rules for replacement (ex. $S \to aA$).
* **$S$ (start symbol):** Where the generation begins.

## Objectives:

* Understand the structure of a formal language and its components.
* Set up the project environment (Git repository, Java setup).
* Implement a `Grammar` class to define the rules of Variant 25 and generate valid strings.
* Implement a `FiniteAutomaton` class to represent states and transitions.
* Implement a conversion method to transform the Grammar into a Finite Automaton automatically.
* Validate input strings using the generated Automaton.

## Implementation description

The project is implemented in Java and consists of three main files.

1. `Grammar.java`
This class represents the formal grammar. I used a `HashMap<String, List<String>>` to store the production rules. The `generateString()` method uses a loop to randomly select productions until the string contains only terminal characters.

```java
public class Grammar {
    // ... setup code ...

    public String generateString() {
        var word = new StringBuilder();
        var currentSymbol = startSymbol;
        var rand = new Random();

        // Loop until we no longer have a Non-Terminal symbol
        while (vn.contains(currentSymbol)) {
            var rules = productions.get(currentSymbol);
            var rule = rules.get(rand.nextInt(rules.size())); // Pick random rule

            for (char c : rule.toCharArray()) {
                var s = String.valueOf(c);
                if (vt.contains(s)) {
                    word.append(s);
                } else if (vn.contains(s)) {
                    currentSymbol = s; // Move to next state
                }
            }
            // Logic to handle terminal-only rules (like A -> b)
            // ...
        }
        return word.toString();
    }
}

```

2. `FiniteAutomaton.java`
This class represents the state machine. The transitions are stored in a nested map structure: `Map<String, Map<Character, String>>`. This allows us to look up the current state, apply an input character, and find the destination state in  time.

```java
public class FiniteAutomaton {
    // ... setup code ...

    public boolean stringBelongToLanguage(String inputString) {
        var currentState = startState;
        
        for (char c : inputString.toCharArray()) {
            var stateTransitions = transitions.get(currentState);
            
            // Check if valid transition exists
            if (stateTransitions != null && stateTransitions.containsKey(c)) {
                currentState = stateTransitions.get(c);
            } else {
                return false; // Invalid transition found
            }
        }
        
        // Check if we ended in a final tate
        return finalStates.contains(currentState);
    }
}

```

3. Conversion logic
The conversion happens in the `Grammar` class. We iterate through every production rule and map it to a graph edge.
* A rule like $S \to dA$ becomes a transition $\delta(S, d) = A$.
* A rule like $A \to b$ becomes a transition $\delta(A, b) = FinalState$.

## Challenges and difficulties

During implementation I encountered several issues that required extra attention. The random generation of strings from the grammar could risk non-termination when productions cycled or when non-terminals repeatedly expanded without producing terminals; I addressed this by enforcing a limit on derivation steps and preferring productions that yield terminals. Converting the grammar to an automaton revealed edge cases as well: terminal-only productions need an explicit final state, and multiple productions for the same non-terminal and terminal could introduce nondeterminism that must be handled or disambiguated.

Practical debugging problems appeared too: missing keys in the production map caused NullPointerExceptions, and mixing `char` vs `String` representations led to subtle mismatches in transition lookups. Ensuring reproducible tests required controlling the `Random` seed during development. Finally, minor environment issues (classpath and Java preview flags) occasionally interrupted test runs.

These challenges were valuable: they highlighted the need for defensive checks (null/empty rules), clear symbol handling, and safeguards against infinite derivations, all of which made the final implementation more robust.

## Conclusions and results

In this laboratory work, I successfully modeled a Regular Grammar and converted it into a Finite Automaton.

Results:
I ran the client to generate 5 random words using the Grammar, and then passed those words to the Automaton to verify they were accepted. I also tested with an invalid string to ensure the validator works correctly.

Console Output:

```text
1. Generating 5 valid strings
Generated: dacba
Generated: db
Generated: dacb
Generated: bbbdacba
Generated: bdb

2. Checking strings with Automaton
Word 'dacba' is accepted? true
Word 'aaabbb' is accepted? false

```

## References

1. Course Notes: Formal Languages & Finite Automata.
2. Hopcroft, J. E., Motwani, R., & Ullman, J. D. (2006). *Introduction to Automata Theory, Languages, and Computation*.
