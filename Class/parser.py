"""
parser.py — Recursive-descent parser for the Car Control DSL.

Grammar (EBNF)
──────────────
  program       ::= { statement NEWLINE }
  statement     ::= simple_stmt | block_stmt
  simple_stmt   ::= ACCELERATE NUMBER
                   | DECELERATE NUMBER
                   | SET_ACCEL NUMBER
                   | RESET_ACCEL
                   | SPEED_UP NUMBER
                   | SLOW_DOWN NUMBER
                   | SET_SPEED NUMBER
                   | STOP
                   | CRUISE
                   | MAX_SPEED
                   | TURN_LEFT NUMBER
                   | TURN_RIGHT NUMBER
                   | SET_ANGLE NUMBER
                   | U_TURN
                   | RESET_ANGLE
                   | SPIN NUMBER
                   | START_ENGINE
                   | STOP_ENGINE
                   | PARK
                   | DRIVE
                   | REVERSE
                   | EMERGENCY_STOP
                   | HONK
                   | TOGGLE_LIGHTS
                   | SIGNAL_LEFT
                   | SIGNAL_RIGHT
                   | SIGNAL_OFF
                   | WAIT NUMBER
                   | PRINT_STATUS
  block_stmt    ::= REPEAT NUMBER block
                   | IF_SPEED_GT NUMBER block
                   | IF_SPEED_LT NUMBER block
  block         ::= LBRACE { statement NEWLINE } RBRACE
"""

from __future__ import annotations
from dataclasses import dataclass, field
from tokens import Token, TokenType


# ═══════════════════════════════════════════════════════════════════════════════
#  AST Node definitions
# ═══════════════════════════════════════════════════════════════════════════════

@dataclass
class ASTNode:
    """Base class for all AST nodes."""
    line: int = 0
    column: int = 0


@dataclass
class Program(ASTNode):
    """Root node — a list of statements."""
    statements: list[ASTNode] = field(default_factory=list)


@dataclass
class SimpleInstruction(ASTNode):
    """An instruction with an optional numeric argument."""
    name: str = ""
    argument: float | int | None = None


@dataclass
class RepeatBlock(ASTNode):
    """REPEAT <n> { body }"""
    count: int = 0
    body: list[ASTNode] = field(default_factory=list)


@dataclass
class IfSpeedGT(ASTNode):
    """IF_SPEED_GT <val> { body }"""
    threshold: float | int = 0
    body: list[ASTNode] = field(default_factory=list)


@dataclass
class IfSpeedLT(ASTNode):
    """IF_SPEED_LT <val> { body }"""
    threshold: float | int = 0
    body: list[ASTNode] = field(default_factory=list)


# ═══════════════════════════════════════════════════════════════════════════════
#  Instructions requiring a numeric argument
# ═══════════════════════════════════════════════════════════════════════════════

INSTRUCTIONS_WITH_ARG: set[TokenType] = {
    TokenType.ACCELERATE,
    TokenType.DECELERATE,
    TokenType.SET_ACCEL,
    TokenType.SPEED_UP,
    TokenType.SLOW_DOWN,
    TokenType.SET_SPEED,
    TokenType.TURN_LEFT,
    TokenType.TURN_RIGHT,
    TokenType.SET_ANGLE,
    TokenType.SPIN,
    TokenType.WAIT,
}

INSTRUCTIONS_NO_ARG: set[TokenType] = {
    TokenType.RESET_ACCEL,
    TokenType.STOP,
    TokenType.CRUISE,
    TokenType.MAX_SPEED,
    TokenType.U_TURN,
    TokenType.RESET_ANGLE,
    TokenType.START_ENGINE,
    TokenType.STOP_ENGINE,
    TokenType.PARK,
    TokenType.DRIVE,
    TokenType.REVERSE,
    TokenType.EMERGENCY_STOP,
    TokenType.HONK,
    TokenType.TOGGLE_LIGHTS,
    TokenType.SIGNAL_LEFT,
    TokenType.SIGNAL_RIGHT,
    TokenType.SIGNAL_OFF,
    TokenType.PRINT_STATUS,
}


# ═══════════════════════════════════════════════════════════════════════════════
#  Parser
# ═══════════════════════════════════════════════════════════════════════════════

class ParseError(Exception):
    """Raised on syntax errors."""
    def __init__(self, message: str, token: Token | None = None):
        loc = ""
        if token:
            loc = f" at line {token.line}, column {token.column}"
        super().__init__(f"{message}{loc}")
        self.token = token


class Parser:
    """
    Recursive-descent parser for CarDSL.

    Usage:
        parser = Parser(tokens)
        ast = parser.parse()
    """

    def __init__(self, tokens: list[Token]):
        self.tokens = tokens
        self.pos = 0

    # ── Helpers ───────────────────────────────────────────────────────────

    def _current(self) -> Token:
        return self.tokens[self.pos]

    def _peek_type(self) -> TokenType:
        return self._current().type

    def _advance(self) -> Token:
        tok = self._current()
        self.pos += 1
        return tok

    def _expect(self, type_: TokenType) -> Token:
        tok = self._current()
        if tok.type != type_:
            raise ParseError(f"Expected {type_.name}, got {tok.type.name}", tok)
        return self._advance()

    def _skip_newlines(self):
        while self._peek_type() == TokenType.NEWLINE:
            self._advance()

    # ── Grammar rules ────────────────────────────────────────────────────

    def parse(self) -> Program:
        """program ::= { statement NEWLINE }"""
        prog = Program()
        self._skip_newlines()
        while self._peek_type() != TokenType.EOF:
            stmt = self._parse_statement()
            prog.statements.append(stmt)
            self._skip_newlines()
        return prog

    def _parse_statement(self) -> ASTNode:
        """statement ::= simple_stmt | block_stmt"""
        tt = self._peek_type()

        if tt in INSTRUCTIONS_WITH_ARG:
            return self._parse_simple_with_arg()

        if tt in INSTRUCTIONS_NO_ARG:
            return self._parse_simple_no_arg()

        if tt == TokenType.REPEAT:
            return self._parse_repeat()

        if tt == TokenType.IF_SPEED_GT:
            return self._parse_if_speed_gt()

        if tt == TokenType.IF_SPEED_LT:
            return self._parse_if_speed_lt()

        raise ParseError(f"Unexpected token {tt.name}", self._current())

    def _parse_simple_with_arg(self) -> SimpleInstruction:
        tok = self._advance()
        num_tok = self._expect(TokenType.NUMBER)
        return SimpleInstruction(
            name=tok.type.name,
            argument=num_tok.value,
            line=tok.line,
            column=tok.column,
        )

    def _parse_simple_no_arg(self) -> SimpleInstruction:
        tok = self._advance()
        return SimpleInstruction(
            name=tok.type.name,
            line=tok.line,
            column=tok.column,
        )

    def _parse_block(self) -> list[ASTNode]:
        """block ::= LBRACE { statement NEWLINE } RBRACE"""
        self._expect(TokenType.LBRACE)
        self._skip_newlines()
        body: list[ASTNode] = []
        while self._peek_type() != TokenType.RBRACE:
            body.append(self._parse_statement())
            self._skip_newlines()
        self._expect(TokenType.RBRACE)
        return body

    def _parse_repeat(self) -> RepeatBlock:
        tok = self._advance()  # consume REPEAT
        count_tok = self._expect(TokenType.NUMBER)
        self._skip_newlines()
        body = self._parse_block()
        return RepeatBlock(
            count=int(count_tok.value),
            body=body,
            line=tok.line,
            column=tok.column,
        )

    def _parse_if_speed_gt(self) -> IfSpeedGT:
        tok = self._advance()  # consume IF_SPEED_GT
        val_tok = self._expect(TokenType.NUMBER)
        self._skip_newlines()
        body = self._parse_block()
        return IfSpeedGT(
            threshold=val_tok.value,
            body=body,
            line=tok.line,
            column=tok.column,
        )

    def _parse_if_speed_lt(self) -> IfSpeedLT:
        tok = self._advance()  # consume IF_SPEED_LT
        val_tok = self._expect(TokenType.NUMBER)
        self._skip_newlines()
        body = self._parse_block()
        return IfSpeedLT(
            threshold=val_tok.value,
            body=body,
            line=tok.line,
            column=tok.column,
        )
