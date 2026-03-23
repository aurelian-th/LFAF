# Laboratory Work 4: Regular expressions

### Course: Formal Languages & Finite Automata
### Author: Tihon Aurelian-Mihai
### Group: FAF-241

----

## Theory
A Regular Expression (RegEx) is a sequence of characters that specifies a search pattern. In formal language theory, regular expressions strictly define **Regular Languages** (Type 3 in the Chomsky Hierarchy). They are mathematically equivalent to Finite Automata, meaning any pattern defined by a regex can be processed and verified by an FA.

Regular expressions rely on three primary operators:
1. **Concatenation:** Implied sequencing of symbols (e.g., $AB$ means $A$ followed immediately by $B$).
2. **Alternation ($|$):** The boolean OR operator, representing a branching choice between expressions.
3. **Kleene Closure ($^*$):** Represents zero or more repetitions of the preceding element. 

Modern regex engines also include syntactic sugar like the Plus operator ($+$) for one or more repetitions, the Optional operator ($?$) for zero or one occurrence, and explicit bounded repetitions (e.g., $\{n\}$ or $^n$).

## Objectives
1. Write and cover what regular expressions are and their theoretical use cases.
2. Implement a dynamic interpreter that builds valid strings based on given regular expressions rather than hardcoding generation sequences.
3. Impose a hard limit of 5 maximum iterations for undefined repetitions ($*$, $+$) to prevent infinite or overly bloated string generation.
4. **Bonus:** Implement a trace function to explicitly show the sequence of processing during dynamic generation.

## Implementation description
To dynamically interpret the regex rules, I designed the system as a mini-compiler utilizing an **Abstract Syntax Tree (AST)**. The program is divided into two major phases:

**1. The recursive descent parser**
The `Parser` class processes the raw regex string character by character and builds a hierarchical tree of nodes. I used standard operator precedence to determine the tree depth:
* `parseExpr()` handles alternations (`|`).
* `parseTerm()` handles concatenation.
* `parseFactor()` handles modifiers (`*`, `+`, `?`, `^n`).
* `parseBase()` handles raw literals and nested parentheses.

**2. The generation engine**
Once the AST is built, I traverse the tree by calling the `.generate()` method on the root node. Each node type handles its logic dynamically:
* An `AltNode` utilizes `java.util.Random` to pick one of its child branches.
* A `RepeatNode` generates a random loop bound. As required, undefined limits like `*` are bounded between `0` and `5`, and `+` is bounded between `1` and `5`. The custom $^n$ operator sets both the minimum and maximum boundary strictly to $n$.

**Challenges and difficulties**
The primary challenge was handling operator precedence dynamically without relying on external libraries like `java.util.regex.Pattern`. When parsing strings like `P(Q|R|S)T`, the parser initially struggled to differentiate where the concatenation ended and the alternation began. I resolved this by enforcing strict grammar rules in the recursive descent parser.

Furthermore, the handwritten Variant 1 included non-standard mathematical superscripts like `^+` and `^*`, alongside custom explicit bounds like `^5`. While adding custom logic to parse these bounds, I encountered a critical parsing collision with whitespace. Initially, I used a global `replaceAll("\\s+", "")` to strip all spaces from the input. However, for the expression `1(0|1)^*2(3|4)^5 36`, this aggressively merged the `5` and the `36` into `^536`. As a result, the AST's `RepeatNode` attempted to loop 536 times, causing massive string generation bloat. 

To overcome this, I removed the global string mutation and instead implemented a dynamic `skipWhitespace()` helper method within the parser. This allowed the parser to read the raw string, use the space as a natural delimiter to know exactly when the `^5` exponent ended, and safely skip over the space without adding it to the final generated string—which perfectly aligns with the expected lab output examples.

**Conclusions and results**
The implementation successfully handles dynamic regex interpretation. By utilizing an AST, the code natively understands deeply nested parentheses and accurately distributes repetitions without hardcoded workarounds. The dynamic whitespace handling ensures robust parsing of complex explicit bounds.

**Console output (Excerpt demonstrating the Execution Trace):**

```text
Processing Regex: (a|b)(c|d)E^+G?

[Generation #1] Result: acEEE

Execution trace
Concatenation:
  Alternation (Chose branch 1):
    Literal: 'a'
  Alternation (Chose branch 1):
    Literal: 'c'
  Repetition '+' (Looping 3 times):
    Literal: 'E'
    Literal: 'E'
    Literal: 'E'
  Repetition '?' (Looping 0 times):


[Generation #2] Result: bdE

[Generation #3] Result: bcEEE

```
The tree-based execution trace proves the engine evaluates the expression dynamically, processing alternations and correctly bounding the exact 5-loop repetition before concatenating the final literals.