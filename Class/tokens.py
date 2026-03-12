"""
tokens.py — Token type definitions for the Car Control DSL.

Each token has a type (from the TokenType enum) and an optional value.
"""

from enum import Enum, auto


class TokenType(Enum):
    """All token types recognized by the CarDSL lexer (finite automaton)."""

    # ── Acceleration instructions ──
    ACCELERATE = auto()
    DECELERATE = auto()
    SET_ACCEL = auto()
    RESET_ACCEL = auto()

    # ── Velocity instructions ──
    SPEED_UP = auto()
    SLOW_DOWN = auto()
    SET_SPEED = auto()
    STOP = auto()
    CRUISE = auto()
    MAX_SPEED = auto()

    # ── Rotation instructions ──
    TURN_LEFT = auto()
    TURN_RIGHT = auto()
    SET_ANGLE = auto()
    U_TURN = auto()
    RESET_ANGLE = auto()
    SPIN = auto()

    # ── State / FSM instructions ──
    START_ENGINE = auto()
    STOP_ENGINE = auto()
    PARK = auto()
    DRIVE = auto()
    REVERSE = auto()
    EMERGENCY_STOP = auto()

    # ── Action instructions ──
    HONK = auto()
    TOGGLE_LIGHTS = auto()
    SIGNAL_LEFT = auto()
    SIGNAL_RIGHT = auto()
    SIGNAL_OFF = auto()

    # ── Control-flow instructions ──
    WAIT = auto()
    REPEAT = auto()
    IF_SPEED_GT = auto()
    IF_SPEED_LT = auto()

    # ── Debug ──
    PRINT_STATUS = auto()

    # ── Literals / Syntax ──
    NUMBER = auto()       # integer or float literal
    LBRACE = auto()       # {
    RBRACE = auto()       # }
    NEWLINE = auto()      # \n (statement separator)
    EOF = auto()          # end of input


# ── Keyword lookup table ─────────────────────────────────────────────────────
# Maps uppercase keyword strings to their TokenType.
KEYWORDS: dict[str, TokenType] = {
    "ACCELERATE":     TokenType.ACCELERATE,
    "DECELERATE":     TokenType.DECELERATE,
    "SET_ACCEL":      TokenType.SET_ACCEL,
    "RESET_ACCEL":    TokenType.RESET_ACCEL,
    "SPEED_UP":       TokenType.SPEED_UP,
    "SLOW_DOWN":      TokenType.SLOW_DOWN,
    "SET_SPEED":      TokenType.SET_SPEED,
    "STOP":           TokenType.STOP,
    "CRUISE":         TokenType.CRUISE,
    "MAX_SPEED":      TokenType.MAX_SPEED,
    "TURN_LEFT":      TokenType.TURN_LEFT,
    "TURN_RIGHT":     TokenType.TURN_RIGHT,
    "SET_ANGLE":      TokenType.SET_ANGLE,
    "U_TURN":         TokenType.U_TURN,
    "RESET_ANGLE":    TokenType.RESET_ANGLE,
    "SPIN":           TokenType.SPIN,
    "START_ENGINE":   TokenType.START_ENGINE,
    "STOP_ENGINE":    TokenType.STOP_ENGINE,
    "PARK":           TokenType.PARK,
    "DRIVE":          TokenType.DRIVE,
    "REVERSE":        TokenType.REVERSE,
    "EMERGENCY_STOP": TokenType.EMERGENCY_STOP,
    "HONK":           TokenType.HONK,
    "TOGGLE_LIGHTS":  TokenType.TOGGLE_LIGHTS,
    "SIGNAL_LEFT":    TokenType.SIGNAL_LEFT,
    "SIGNAL_RIGHT":   TokenType.SIGNAL_RIGHT,
    "SIGNAL_OFF":     TokenType.SIGNAL_OFF,
    "WAIT":           TokenType.WAIT,
    "REPEAT":         TokenType.REPEAT,
    "IF_SPEED_GT":    TokenType.IF_SPEED_GT,
    "IF_SPEED_LT":    TokenType.IF_SPEED_LT,
    "PRINT_STATUS":   TokenType.PRINT_STATUS,
}


class Token:
    """A single token produced by the lexer."""

    __slots__ = ("type", "value", "line", "column")

    def __init__(self, type_: TokenType, value=None, line: int = 0, column: int = 0):
        self.type = type_
        self.value = value
        self.line = line
        self.column = column

    def __repr__(self) -> str:
        if self.value is not None:
            return f"Token({self.type.name}, {self.value!r}, L{self.line}:{self.column})"
        return f"Token({self.type.name}, L{self.line}:{self.column})"
