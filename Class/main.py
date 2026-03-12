"""
main.py -- Entry point for the Car Control DSL.

Usage:
    python main.py <script.car>          Run a .car script file
    python main.py --demo                Run the built-in demo
    python main.py --map <script.car>    Run and show top-down movement map
    python main.py --map --demo          Demo with map
    python main.py --fsm                 Print the FSM transition table
    python main.py --tokens <script.car> Show lexer output only
    python main.py --ast <script.car>    Show parser AST only
"""

import sys
import os

# Ensure the project directory is on the path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from lexer import Lexer, LexerError
from parser import Parser, ParseError
from interpreter import Interpreter, Car
from fsm import CarFSM
from visualizer import plot_car_map


DEMO_SCRIPT = """\
# ── CarDSL Demo Script ──

START_ENGINE
PRINT_STATUS

# Get moving
DRIVE
TOGGLE_LIGHTS
ACCELERATE 10
SPEED_UP 30
PRINT_STATUS

# Navigate a corner
SIGNAL_LEFT
TURN_LEFT 90
SIGNAL_OFF
PRINT_STATUS

# Repeat acceleration bursts
REPEAT 3 {
    ACCELERATE 5
    SPEED_UP 10
    PRINT_STATUS
}

# Conditional slowdown
IF_SPEED_GT 50 {
    SLOW_DOWN 20
    DECELERATE 10
    PRINT_STATUS
}

# U-turn maneuver
SIGNAL_RIGHT
U_TURN
SIGNAL_OFF

# Cruise
CRUISE
PRINT_STATUS

# Check low speed
IF_SPEED_LT 100 {
    SPEED_UP 15
}

# Set exact values
SET_SPEED 80
SET_ACCEL 0
SET_ANGLE 270
PRINT_STATUS

# Spin!
SPIN 360

# Reset everything
RESET_ACCEL
RESET_ANGLE
PRINT_STATUS

# Max speed then emergency stop
MAX_SPEED
PRINT_STATUS
EMERGENCY_STOP
PRINT_STATUS

# Reverse
DRIVE
SPEED_UP 20
REVERSE
PRINT_STATUS

# Final park
STOP
HONK
STOP_ENGINE
PRINT_STATUS
"""


def run_source(source: str, verbose: bool = True, show_map: bool = False):
    """Lex -> Parse -> Interpret a source string."""
    # 1. Lexing (Finite Automaton)
    if verbose:
        print("=== LEXER (Finite Automaton) ===")
    lexer = Lexer(source)
    tokens = lexer.tokenize()
    if verbose:
        for tok in tokens:
            print(f"  {tok}")
        print()

    # 2. Parsing (Recursive Descent)
    if verbose:
        print("=== PARSER (AST) ===")
    parser = Parser(tokens)
    ast = parser.parse()
    if verbose:
        _print_ast(ast)
        print()

    # 3. Interpretation
    if verbose:
        print("=== INTERPRETER ===")
    interp = Interpreter(verbose=verbose)
    interp.run(ast)
    if verbose:
        print()
        print("=== FINAL STATE ===")
        print(f"  {interp.car.status()}")

    # 4. Show top-down map if requested
    if show_map:
        print("\n=== SHOWING MAP ===")
        plot_car_map(interp.car.history)

    return interp.car


def _print_ast(node, indent: int = 0):
    """Pretty-print an AST tree."""
    prefix = "  " * indent
    from parser import Program, SimpleInstruction, RepeatBlock, IfSpeedGT, IfSpeedLT

    if isinstance(node, Program):
        print(f"{prefix}Program ({len(node.statements)} statements)")
        for s in node.statements:
            _print_ast(s, indent + 1)
    elif isinstance(node, SimpleInstruction):
        arg = f" {node.argument}" if node.argument is not None else ""
        print(f"{prefix}{node.name}{arg}")
    elif isinstance(node, RepeatBlock):
        print(f"{prefix}REPEAT {node.count}")
        for s in node.body:
            _print_ast(s, indent + 1)
    elif isinstance(node, IfSpeedGT):
        print(f"{prefix}IF_SPEED_GT {node.threshold}")
        for s in node.body:
            _print_ast(s, indent + 1)
    elif isinstance(node, IfSpeedLT):
        print(f"{prefix}IF_SPEED_LT {node.threshold}")
        for s in node.body:
            _print_ast(s, indent + 1)


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)

    # Check for --map flag
    show_map = "--map" in sys.argv
    args = [a for a in sys.argv[1:] if a != "--map"]
    if not args and show_map:
        # --map alone => treat as --demo --map
        args = ["--demo"]
    arg = args[0] if args else "--demo"

    if arg == "--demo":
        print("Running built-in demo...\n")
        run_source(DEMO_SCRIPT, verbose=True, show_map=show_map)

    elif arg == "--fsm":
        print("Car FSM Transition Table:\n")
        CarFSM.print_transition_table()

    elif arg == "--tokens":
        if len(args) < 2:
            print("Usage: python main.py --tokens <file.car>")
            sys.exit(1)
        with open(args[1], "r") as f:
            source = f.read()
        lexer = Lexer(source)
        for tok in lexer.tokenize():
            print(tok)

    elif arg == "--ast":
        if len(args) < 2:
            print("Usage: python main.py --ast <file.car>")
            sys.exit(1)
        with open(args[1], "r") as f:
            source = f.read()
        lexer = Lexer(source)
        parser = Parser(lexer.tokenize())
        _print_ast(parser.parse())

    else:
        # Treat as file path
        filepath = arg
        if not os.path.isfile(filepath):
            print(f"Error: file not found: {filepath}")
            sys.exit(1)
        with open(filepath, "r") as f:
            source = f.read()
        print(f"Running {filepath}...\n")
        run_source(source, verbose=True, show_map=show_map)


if __name__ == "__main__":
    main()
