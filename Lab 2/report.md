# Laboratory Work 2: Determinism in Finite Automata. Conversion from NDFA to DFA. Chomsky Hierarchy.

### Course: Formal Languages & Finite Automata

### Author: Tihon Aurelian-Mihai

### Group: FAF-241

---

## Theory

A Finite Automaton (FA) is a mathematical model of computation that describes a system through a finite number of states, transitions between those states, and actions. It is widely used in systems theory to represent processes that have a clear beginning and ending.

When discussing automata, a crucial characteristic is **determinism**, which defines how predictable the system is.

* **Deterministic Finite Automaton (DFA):** For any given state and input symbol, there is exactly one strictly defined transition to a next state. The execution path is absolute.
* **Non-Deterministic Finite Automaton (NDFA):** The system introduces ambiguity. A single input symbol might lead to multiple possible states from the current state, or the system might change states without any input at all ($\epsilon$-transitions).

Because computational hardware and software ultimately require predictable, step-by-step instructions, NDFAs are often theoretical tools that must be converted to DFAs for practical application. This is achieved using the **Subset Construction Algorithm** (or Powerset Construction), which mathematically groups ambiguous branching paths into new, singular "macro-states," thereby eliminating non-determinism.

Additionally, formal languages are categorized by the **Chomsky Hierarchy**, which classifies grammars based on the strictness of their production rules, ranging from Type 0 (Unrestricted) to Type 3 (Regular).

## Objectives

1. Understand what an automaton is and its practical applications.
2. Extend the existing `Grammar` class to include a function that classifies the grammar based on the Chomsky Hierarchy.
3. Implement a method to convert a Finite Automaton to a Regular Grammar.
4. Create a function to evaluate whether a given FA is deterministic or non-deterministic.
5. Implement the subset construction algorithm to convert an NDFA to a DFA.
6. **Bonus:** Represent the finite automaton graphically using an automated API request.

## Implementation description

The project extends the Java classes established in the first laboratory work, utilizing modern Java features (like the `var` keyword and the `java.net.http.HttpClient` API) for clean and maintainable code.

**1. Chomsky classification (`Grammar.java`)**
I implemented a `classifyChomsky()` method that iterates through the production map. It checks the structure of both the Left-Hand Side (LHS) and Right-Hand Side (RHS) of every rule to determine the strictest applicable grammar type. It enforces checks for terminal and non-terminal placements to identify Type 3 grammars accurately.

**2. FA to Grammar conversion (`FiniteAutomaton.java`)**
The `toRegularGrammar()` method dynamically generates production rules from the automaton's transition matrix. It loops over all mapped transitions; for each $\delta(q_a, input) = q_b$, it creates a Grammar rule $q_a \to input \cdot q_b$. If $q_b$ happens to be a recognized final state, it also appends a terminal rule $q_a \to input$.

```java
public Grammar toRegularGrammar() {
    var grammar = new Grammar(); 
    grammar.clearRules(); // Ensure a clean slate

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

```

**3. NDFA to DFA conversion (`FiniteAutomaton.java`)**
The `toDFA()` method implements the Subset Construction algorithm. I utilized a `Queue<Set<String>>` to process newly discovered "macro-states" iteratively. For every symbol in the alphabet, the algorithm calculates the union of all reachable states from the current macro-state, creating a new deterministic path.

**4. Bonus visualization (`Main.java`)**
Rather than manually copying graph syntax, I implemented an automated HTTP client. It packages the DFA transitions into Graphviz DOT syntax, sends a `GET` request to the QuickChart API, and writes the returned byte array directly to a local `.png` file.

```java
var client = HttpClient.newHttpClient();
var request = HttpRequest.newBuilder()
        .uri(URI.create(apiUrl))
        .GET()
        .build();
            
var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
if (response.statusCode() == 200) {
    Files.write(Path.of(filename), response.body());
}

```

## Challenges and difficulties

During the implementation of this laboratory work, several technical challenges arose that required careful debugging and structural refactoring:

1. **Immutable Collection Constraints:** When attempting to clear the Lab 1 grammar rules to make way for the Lab 2 FA conversion, I encountered an `UnsupportedOperationException`. This occurred because I had initially instantiated the grammar variables using `Set.of()`, which creates an immutable collection in Java. Attempting to call `.clear()` on these sets caused a runtime crash. I resolved this by wrapping the initializations in `new HashSet<>()`, restoring mutability while keeping the initialization clean.
2. **State Uniqueness in Subset Construction:** Another significant hurdle was ensuring logical uniqueness when naming macro-states during the NDFA to DFA conversion. Because a macro-state is a collection of sub-states, the order of elements could cause logical duplicates (e.g., treating `[q0,q1]` and `[q1,q0]` as two separate states). I mitigated this by enforcing a `TreeSet` when formatting state names, guaranteeing an alphabetically sorted, predictable string representation.
3. **API Data Formatting:** When implementing the bonus automated graph generation, the initial HTTP request returned an unreadable file. I discovered that the QuickChart API defaults to returning SVG vector data, which corrupted the file when forced into a `.png` extension. Appending `&format=png` to the URL parameters successfully instructed the API to render and return the correct binary image data.

These debugging sessions heavily reinforced the importance of understanding underlying data structures and handling external API constraints defensively.

## Conclusions and results

In this laboratory work, I successfully implemented the theoretical algorithms required to evaluate and manipulate Finite Automata and formal grammars.

The console execution verified all constraints. The Lab 1 grammar was correctly classified as a Type 3 (Regular Grammar). The internal FA for Variant 25 was correctly identified as Non-Deterministic due to the branching transition $\delta(q_0, a) \to \{q_0, q_1\}$. The subset construction algorithm successfully mathematical squished these branches into deterministic macro-states, proving the conversion logic works entirely.

**Console output:**

```text
Lab 2: Automata conversion

[Task 1] Lab 1 Grammar classification:
  Result: Type 3 (Regular Grammar)

[Task 2] Original Finite Automaton (v25):
  Start State: q0
  Final States: [q2]
  Transitions:
    ?(q1, a) = [q2]
    ?(q1, b) = [q1]
    ?(q2, a) = [q3]
    ?(q3, a) = [q1]
    ?(q0, a) = [q1, q0]

[Task 3] Converting FA to Regular Grammar:
  q1 -> aq2 | a | bq1
  q2 -> aq3
  q3 -> aq1
  q0 -> aq1 | aq0

[Task 4] Is original FA deterministic? false

[Task 5] Converting NDFA to DFA using subset construction...

Resulting DFA:
  Start State: [q0]
  Final States: [[q0,q1,q2,q3], [q0,q1,q2], [q2]]
  Transitions:
    ?([q0,q1,q2,q3], a) = [[q0,q1,q2,q3]]
    ?([q0,q1,q2,q3], b) = [[q1]]
    ?([q0,q1,q2], a) = [[q0,q1,q2,q3]]
    ?([q0,q1,q2], b) = [[q1]]
    ?([q0], a) = [[q0,q1]]
    ?([q1], a) = [[q2]]
    ?([q1], b) = [[q1]]
    ?([q2], a) = [[q3]]
    ?([q3], a) = [[q1]]
    ?([q0,q1], a) = [[q0,q1,q2]]
    ?([q0,q1], b) = [[q1]]

Is the new Automaton deterministic? true

[Task 6] Generating visual graph.
  Sending Graphviz data to API...
  Success! Graph saved automatically as: dfa_variant25.png

```

**Generated DFA visualization:**

<img width="852" height="281" alt="dfa_variant25" src="https://github.com/user-attachments/assets/880dc3b5-03f3-4361-8543-45e34870aa60" />

## References

1. Course Notes: Formal Languages & Finite Automata.
2. Hopcroft, J. E., Motwani, R., & Ullman, J. D. (2006). *Introduction to Automata Theory, Languages, and Computation*.
