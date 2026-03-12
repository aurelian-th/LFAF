"""
lexer.py — Lexer (Finite Automaton) for the Car Control DSL.

The lexer implements a Deterministic Finite Automaton (DFA) that reads
characters one at a time and transitions between states to produce tokens.

DFA States
──────────
  START       → Initial state; dispatch based on current character
  IN_WORD     → Accumulating alphabetic / underscore characters (keyword)
  IN_NUMBER   → Accumulating digit characters (integer part)
  IN_DECIMAL  → Accumulating digits after a '.' (fractional part)
  IN_COMMENT  → Inside a line comment (# …); skip until newline
  DONE        → Token emitted; return to START for next token

Transition Table (simplified)
─────────────────────────────
  State        Input          Next State      Action
  ─────        ─────          ──────────      ──────
  START        letter/A-Z_    IN_WORD         begin accumulating
  START        digit          IN_NUMBER       begin accumulating
  START        #              IN_COMMENT      skip
  START        {              DONE            emit LBRACE
  START        }              DONE            emit RBRACE
  START        \\n             DONE            emit NEWLINE
  START        whitespace     START           skip
  IN_WORD      letter/A-Z_   IN_WORD         accumulate
  IN_WORD      other          DONE            emit keyword / error
  IN_NUMBER    digit          IN_NUMBER       accumulate
  IN_NUMBER    .              IN_DECIMAL      accumulate
  IN_NUMBER    other          DONE            emit NUMBER
  IN_DECIMAL   digit          IN_DECIMAL      accumulate
  IN_DECIMAL   other          DONE            emit NUMBER
  IN_COMMENT   \\n             START           skip (don't emit)
  IN_COMMENT   other          IN_COMMENT      skip
"""

from tokens import Token, TokenType, KEYWORDS


# ── DFA State Enumeration ────────────────────────────────────────────────────
class _State:
    START = "START"
    IN_WORD = "IN_WORD"
    IN_NUMBER = "IN_NUMBER"
    IN_DECIMAL = "IN_DECIMAL"
    IN_COMMENT = "IN_COMMENT"


class LexerError(Exception):
    """Raised when the lexer encounters an invalid character."""
    def __init__(self, char: str, line: int, column: int):
        super().__init__(f"Unexpected character {char!r} at line {line}, column {column}")
        self.char = char
        self.line = line
        self.column = column


class Lexer:
    """
    Finite-automaton-based lexer for CarDSL.

    Usage:
        lexer = Lexer(source_code)
        tokens = lexer.tokenize()
    """

    def __init__(self, source: str):
        self.source = source
        self.pos = 0                 # current position in source
        self.line = 1                # current line number
        self.column = 1              # current column number
        self.tokens: list[Token] = []

    # ── Helper methods ────────────────────────────────────────────────────

    def _peek(self) -> str | None:
        """Return current character without consuming it, or None at EOF."""
        if self.pos < len(self.source):
            return self.source[self.pos]
        return None

    def _advance(self) -> str:
        """Consume and return the current character; update line/col."""
        ch = self.source[self.pos]
        self.pos += 1
        if ch == "\n":
            self.line += 1
            self.column = 1
        else:
            self.column += 1
        return ch

    def _emit(self, type_: TokenType, value=None, line: int = 0, col: int = 0):
        """Append a token to the output list."""
        self.tokens.append(Token(type_, value, line or self.line, col or self.column))

    # ── Core DFA tokenizer ────────────────────────────────────────────────

    def tokenize(self) -> list[Token]:
        """
        Run the DFA over the entire source string and return a list of tokens.
        """
        state = _State.START
        buffer: list[str] = []
        token_line = self.line
        token_col = self.column

        while True:
            ch = self._peek()

            # ── STATE: START ──────────────────────────────────────────────
            if state == _State.START:
                if ch is None:
                    # End of input
                    break

                token_line = self.line
                token_col = self.column

                if ch == "#":
                    # Transition → IN_COMMENT
                    self._advance()
                    state = _State.IN_COMMENT

                elif ch in (" ", "\t", "\r"):
                    # Stay in START — skip whitespace
                    self._advance()

                elif ch == "\n":
                    self._advance()
                    # Only emit NEWLINE if the last token isn't already a NEWLINE
                    if not self.tokens or self.tokens[-1].type != TokenType.NEWLINE:
                        self._emit(TokenType.NEWLINE, line=token_line, col=token_col)

                elif ch == "{":
                    self._advance()
                    self._emit(TokenType.LBRACE, line=token_line, col=token_col)

                elif ch == "}":
                    self._advance()
                    self._emit(TokenType.RBRACE, line=token_line, col=token_col)

                elif ch.isalpha() or ch == "_":
                    # Transition → IN_WORD
                    buffer = [self._advance()]
                    state = _State.IN_WORD

                elif ch.isdigit():
                    # Transition → IN_NUMBER
                    buffer = [self._advance()]
                    state = _State.IN_NUMBER

                elif ch == "-":
                    # Negative number
                    buffer = [self._advance()]
                    state = _State.IN_NUMBER

                else:
                    raise LexerError(ch, self.line, self.column)

            # ── STATE: IN_WORD ────────────────────────────────────────────
            elif state == _State.IN_WORD:
                if ch is not None and (ch.isalpha() or ch == "_" or ch.isdigit()):
                    buffer.append(self._advance())
                else:
                    # Transition → START (emit keyword token)
                    word = "".join(buffer).upper()
                    if word in KEYWORDS:
                        self._emit(KEYWORDS[word], line=token_line, col=token_col)
                    else:
                        raise LexerError(
                            "".join(buffer), token_line, token_col
                        )
                    buffer.clear()
                    state = _State.START

            # ── STATE: IN_NUMBER ──────────────────────────────────────────
            elif state == _State.IN_NUMBER:
                if ch is not None and ch.isdigit():
                    buffer.append(self._advance())
                elif ch == ".":
                    buffer.append(self._advance())
                    state = _State.IN_DECIMAL
                else:
                    # Transition → START (emit NUMBER)
                    self._emit(TokenType.NUMBER, int("".join(buffer)),
                               line=token_line, col=token_col)
                    buffer.clear()
                    state = _State.START

            # ── STATE: IN_DECIMAL ─────────────────────────────────────────
            elif state == _State.IN_DECIMAL:
                if ch is not None and ch.isdigit():
                    buffer.append(self._advance())
                else:
                    # Transition → START (emit NUMBER as float)
                    self._emit(TokenType.NUMBER, float("".join(buffer)),
                               line=token_line, col=token_col)
                    buffer.clear()
                    state = _State.START

            # ── STATE: IN_COMMENT ─────────────────────────────────────────
            elif state == _State.IN_COMMENT:
                if ch is None or ch == "\n":
                    state = _State.START
                else:
                    self._advance()

        # Flush remaining buffer (edge case: file ends mid-token)
        if state == _State.IN_WORD:
            word = "".join(buffer).upper()
            if word in KEYWORDS:
                self._emit(KEYWORDS[word], line=token_line, col=token_col)
            else:
                raise LexerError("".join(buffer), token_line, token_col)
        elif state in (_State.IN_NUMBER, _State.IN_DECIMAL):
            val = "".join(buffer)
            num = float(val) if "." in val else int(val)
            self._emit(TokenType.NUMBER, num, line=token_line, col=token_col)

        # Always append EOF
        self._emit(TokenType.EOF, line=self.line, col=self.column)
        return self.tokens
