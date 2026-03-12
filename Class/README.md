# CarDSL — A Domain-Specific Language for Car Control

## Overview
CarDSL is a simple domain-specific language for controlling a virtual car.  
It features a **finite-state machine** for car states, a **finite automaton** (lexer) for tokenization, and a recursive-descent **parser** that builds an AST.

## 32 Instructions

| # | Instruction | Category | Description |
|---|-------------|----------|-------------|
| 1 | `ACCELERATE <value>` | Acceleration | Increase acceleration by value |
| 2 | `DECELERATE <value>` | Acceleration | Decrease acceleration by value |
| 3 | `SET_ACCEL <value>` | Acceleration | Set acceleration to exact value |
| 4 | `RESET_ACCEL` | Acceleration | Reset acceleration to 0 |
| 5 | `SPEED_UP <value>` | Velocity | Increase velocity by value |
| 6 | `SLOW_DOWN <value>` | Velocity | Decrease velocity by value |
| 7 | `SET_SPEED <value>` | Velocity | Set velocity to exact value |
| 8 | `STOP` | Velocity | Set velocity to 0 |
| 9 | `CRUISE` | Velocity | Lock current speed (set accel to 0) |
| 10 | `MAX_SPEED` | Velocity | Set velocity to maximum |
| 11 | `TURN_LEFT <degrees>` | Rotation | Turn left by degrees |
| 12 | `TURN_RIGHT <degrees>` | Rotation | Turn right by degrees |
| 13 | `SET_ANGLE <degrees>` | Rotation | Set heading to exact angle |
| 14 | `U_TURN` | Rotation | Rotate 180 degrees |
| 15 | `RESET_ANGLE` | Rotation | Reset heading to 0 |
| 16 | `SPIN <degrees>` | Rotation | Perform a spin (full rotation) |
| 17 | `START_ENGINE` | State | Transition car to IDLE state |
| 18 | `STOP_ENGINE` | State | Transition car to OFF state |
| 19 | `PARK` | State | Transition car to PARKED state |
| 20 | `DRIVE` | State | Transition car to MOVING state |
| 21 | `REVERSE` | State | Transition car to REVERSING state |
| 22 | `EMERGENCY_STOP` | State | Immediate stop + PARKED state |
| 23 | `HONK` | Action | Sound the horn |
| 24 | `TOGGLE_LIGHTS` | Action | Toggle headlights on/off |
| 25 | `SIGNAL_LEFT` | Action | Activate left turn signal |
| 26 | `SIGNAL_RIGHT` | Action | Activate right turn signal |
| 27 | `SIGNAL_OFF` | Action | Deactivate turn signals |
| 28 | `WAIT <seconds>` | Control | Wait for specified seconds |
| 29 | `REPEAT <n> { ... }` | Control | Repeat a block n times |
| 30 | `IF_SPEED_GT <val> { ... }` | Control | Execute block if speed > val |
| 31 | `IF_SPEED_LT <val> { ... }` | Control | Execute block if speed < val |
| 32 | `PRINT_STATUS` | Debug | Print current car state |

## Components

| File | Component | Description |
|------|-----------|-------------|
| `tokens.py` | Token definitions | Token types and Token class |
| `lexer.py` | Lexer (Finite Automaton) | Character-by-character tokenization using DFA |
| `parser.py` | Parser | Recursive-descent parser producing AST nodes |
| `fsm.py` | Finite State Machine | Car state transitions (OFF→IDLE→MOVING→...) |
| `interpreter.py` | Interpreter | Walks AST and executes instructions |
| `main.py` | Entry point | CLI interface to run `.car` scripts |

## Usage

```bash
python main.py example.car
```

## Example Script (`example.car`)

```
START_ENGINE
DRIVE
ACCELERATE 10
SPEED_UP 30
TURN_LEFT 45
REPEAT 3 {
    ACCELERATE 5
    PRINT_STATUS
}
IF_SPEED_GT 50 {
    SLOW_DOWN 20
}
HONK
STOP
PARK
STOP_ENGINE
```

## FSM Diagram

```
        START_ENGINE         DRIVE            
  [OFF] ──────────► [IDLE] ────────► [MOVING]
    ▲                  │                │  ▲
    │    STOP_ENGINE   │  PARK          │  │
    ◄──────────────────┘   │            │  │
    ▲                      ▼            │  │
    │               [PARKED]            │  │
    │                  ▲                │  │
    │  EMERGENCY_STOP  │      REVERSE   │  │
    │  ◄───────────────┤◄───────────────┘  │
    │                  │                   │
    │                  ▼                   │
    │              [REVERSING] ───DRIVE───►│
    └──────────────────┘
```
