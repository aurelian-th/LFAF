# Laboratory Work 5: Chomsky Normal Form

### Course: Formal Languages & Finite Automata
### Author: Tihon Aurelian-Mihai
### Group: FAF-241

----

## Theory
Chomsky Normal Form (CNF) is a strict, standardized structure for context-free grammars. Normalizing a grammar into CNF simplifies parsing algorithms and theoretical proofs because it guarantees a predictable, binary derivation tree structure. 

A grammar is in Chomsky Normal Form if all of its production rules are strictly in one of two forms:
1.  **Two Non-Terminals:** $A \to BC$
2.  **One Terminal:** $A \to a$

To convert an arbitrary context-free grammar into CNF, a specific sequence of five transformations must be applied mathematically and programmatically:
1.  **Eliminate $\epsilon$-productions:** Remove rules that produce the empty string and compensate by generating all permutation branches where that nullable variable is omitted.
2.  **Eliminate Unit Productions (Renaming):** Remove rules mapping one non-terminal directly to another ($A \to B$) by substituting the target variable's right-hand side.
3.  **Eliminate Inaccessible Symbols:** Remove variables that can never be reached from the start symbol ($S$).
4.  **Eliminate Non-Productive Symbols:** Remove variables that cannot eventually resolve entirely into terminal symbols.
5.  **Convert to CNF (Cascading):** Extract terminals into standalone variables (e.g., $X_a \to a$) and cascade right-hand sides longer than two symbols into pairs using intermediate variables (e.g., $Y_1, Y_2$).

## Objectives
1. Learn about Chomsky Normal Form (CNF).
2. Get familiar with the approaches to normalizing a grammar.
3. Implement a method for normalizing an input grammar according to the rules of CNF.
    * Encapsulate the implementation in an appropriate class.
    * Execute and test the implemented functionality.
    * **Bonus Point:** Ensure the algorithm can accept and process *any* grammar dynamically, not just hardcoded for the specific variant.

## Implementation Description
To fulfill the requirements and achieve the bonus point, I built a dynamic, object-oriented `CNFConverter` class in Java. 

To support dynamic grammar sizes and multi-character variable names (like $X_a$ or $Y_{12}$), I avoided standard string manipulation and instead used a nested collection structure: `Map<String, Set<List<String>>>`. This mapped every non-terminal to a set of its right-hand side derivations, broken down into discrete symbol tokens.

The conversion logic was broken into five discrete methods called sequentially:
* `step1EliminateEpsilon()`: Scans for variables deriving `e` (epsilon). Uses a recursive permutation generator to rebuild all other rules by simulating both the inclusion and omission of the nullable variable.
* `step2EliminateUnit()`: Iterates through the rule set. When it finds a single-item list containing a non-terminal, it deletes the rule and directly inherits all productions of the target variable.
* `step3EliminateInaccessible()`: Uses a simulated reachability graph (breadth-first discovery) starting from the Start Symbol $S$. Variables outside the final reachability set are discarded.
* `step4EliminateNonProductive()`: Iterates from the bottom up. Variables that explicitly map to terminals are marked productive. The algorithm recursively marks variables productive if their right-hand side contains only other productive variables.
* `step5ConvertToStrictCNF()`: Replaces all terminals in mixed strings with generic $X_n$ variables. Then, using a `while` loop, it pops the last two items off right-hand sides longer than 2, creates a new $Y_n$ paired variable, and pushes it back until the string length is exactly 2.

## Challenges and Difficulties
While building the algorithm for Step 5, I encountered a major `ConcurrentModificationException`. Because the cascade logic generates new $X$ and $Y$ variables on the fly, adding them directly to the `nonTerminals` set while simultaneously iterating over that same set crashed the Java runtime. I resolved this by taking a static snapshot of the keys (`new ArrayList<>(nonTerminals)`) before initiating the loop.

A secondary logical bug occurred when merging the newly cascaded CNF pairs back into the main grammar map. The algorithm successfully generated the CNF-compliant rules but appended them alongside the old, un-cascaded strings (e.g., leaving $A \to baCa$ right next to $A \to X_bY_4$). I fixed this by implementing a `.clear()` instruction on the target map entry before injecting the final generated sets, ensuring the strict binary form was preserved without old rule bleed-over.

## Conclusions and Results
The application successfully parses, processes, and normalizes formal context-free grammars. The step-by-step console printouts confirm that Variant 25 perfectly matches the mathematical expectations.

**Final Output for Variant 25:**
```text
=== LAB: CHOMSKY NORMAL FORM (Variant 25) ===
--- INITIAL GRAMMAR ---
A -> a | aS | bCaCa
B -> A | bS | bCAa
S -> bA | BC
C -> AB | e
D -> AB

[... Intermediate steps omitted for brevity ...]

--- STEP 5: Chomsky Normal Form (Final) ---
A -> a | X_aS | X_bY_3 | X_bY_1 | X_bY_4 | X_bY_5
B -> a | X_bS | X_aS | X_bY_3 | X_bY_1 | X_bY_6 | X_bY_7 | X_bY_4 | X_bY_5
C -> AB
Y_1 -> X_aX_a
Y_3 -> X_aY_2
Y_2 -> CX_a
Y_5 -> CY_1
Y_4 -> CY_3
Y_7 -> CY_6
Y_6 -> AX_a
S -> a | X_bS | BC | X_aS | X_bY_3 | X_bY_1 | X_bY_6 | X_bY_7 | X_bA | X_bY_4 | X_bY_5
X_b -> b
X_a -> a
```