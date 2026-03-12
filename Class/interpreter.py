"""
interpreter.py — AST-walking interpreter for the Car Control DSL.

Walks the AST produced by the parser and executes each instruction,
mutating the Car model and enforcing FSM transitions.
"""

from __future__ import annotations

import math
import time
from dataclasses import dataclass, field

from parser import (
    ASTNode, Program, SimpleInstruction, RepeatBlock, IfSpeedGT, IfSpeedLT,
)
from fsm import CarFSM, CarState, FSMError


# ═══════════════════════════════════════════════════════════════════════════════
#  Car Model
# ═══════════════════════════════════════════════════════════════════════════════

@dataclass
class Car:
    """Runtime model of the car with physics-like properties."""
    # -- Velocity (km/h) --
    velocity: float = 0.0
    max_velocity: float = 200.0

    # -- Acceleration (km/h^2) --
    acceleration: float = 0.0

    # -- Rotation / heading (degrees, 0 = north, clockwise) --
    angle: float = 0.0

    # -- Position (x, y) on a 2D plane --
    x: float = 0.0
    y: float = 0.0

    # -- Peripheral state --
    lights_on: bool = False
    signal: str = "OFF"  # "OFF", "LEFT", "RIGHT"

    # -- FSM --
    fsm: CarFSM = field(default_factory=CarFSM)

    # -- Movement history for visualization --
    history: list = field(default_factory=list)

    def clamp_velocity(self):
        """Keep velocity within [0, max_velocity]."""
        self.velocity = max(0.0, min(self.velocity, self.max_velocity))

    def normalize_angle(self):
        """Keep angle in [0, 360)."""
        self.angle = self.angle % 360

    def record_position(self, label: str = ""):
        """
        Move the car one 'time step' based on current velocity & heading,
        then record the new position in the history.
        Heading: 0=North(+Y), 90=East(+X), 180=South(-Y), 270=West(-X).
        """
        dt = 0.1  # time-step per instruction (arbitrary units)
        rad = math.radians(self.angle)
        self.x += self.velocity * math.sin(rad) * dt
        self.y += self.velocity * math.cos(rad) * dt
        self.history.append({
            "x": self.x,
            "y": self.y,
            "angle": self.angle,
            "velocity": self.velocity,
            "acceleration": self.acceleration,
            "state": self.fsm.get_state().name,
            "label": label,
        })

    def clamp_velocity(self):
        """Keep velocity within [0, max_velocity]."""
        self.velocity = max(0.0, min(self.velocity, self.max_velocity))

    def normalize_angle(self):
        """Keep angle in [0, 360)."""
        self.angle = self.angle % 360

    def status(self) -> str:
        return (
            f"[{self.fsm.get_state().name}] "
            f"vel={self.velocity:.1f} km/h  "
            f"accel={self.acceleration:.1f}  "
            f"heading={self.angle:.1f} deg  "
            f"lights={'ON' if self.lights_on else 'OFF'}  "
            f"signal={self.signal}"
        )


# ═══════════════════════════════════════════════════════════════════════════════
#  Interpreter
# ═══════════════════════════════════════════════════════════════════════════════

class RuntimeError(Exception):
    """Raised for errors during interpretation."""
    pass


class Interpreter:
    """
    Walks the AST and executes instructions against a Car instance.

    Usage:
        interp = Interpreter()
        interp.run(ast)
    """

    def __init__(self, car: Car | None = None, verbose: bool = True):
        self.car = car or Car()
        self.verbose = verbose

    def _log(self, msg: str):
        if self.verbose:
            print(f"  >> {msg}")

    # ── Main entry ────────────────────────────────────────────────────────

    def run(self, program: Program):
        for stmt in program.statements:
            self._exec(stmt)

    # ── Dispatcher ────────────────────────────────────────────────────────

    def _exec(self, node: ASTNode):
        if isinstance(node, SimpleInstruction):
            self._exec_simple(node)
        elif isinstance(node, RepeatBlock):
            self._exec_repeat(node)
        elif isinstance(node, IfSpeedGT):
            self._exec_if_speed_gt(node)
        elif isinstance(node, IfSpeedLT):
            self._exec_if_speed_lt(node)
        else:
            raise RuntimeError(f"Unknown AST node: {type(node).__name__}")
        # Record position after every instruction for the map
        label = ""
        if isinstance(node, SimpleInstruction):
            label = node.name
        self.car.record_position(label)

    # ── Simple instructions ───────────────────────────────────────────────

    def _exec_simple(self, instr: SimpleInstruction):
        name = instr.name
        arg = instr.argument
        car = self.car

        # ── Acceleration ──
        if name == "ACCELERATE":
            car.acceleration += arg
            self._log(f"ACCELERATE {arg}  ->  accel={car.acceleration:.1f}")

        elif name == "DECELERATE":
            car.acceleration -= arg
            self._log(f"DECELERATE {arg}  ->  accel={car.acceleration:.1f}")

        elif name == "SET_ACCEL":
            car.acceleration = arg
            self._log(f"SET_ACCEL {arg}")

        elif name == "RESET_ACCEL":
            car.acceleration = 0.0
            self._log("RESET_ACCEL  ->  accel=0")

        # ── Velocity ──
        elif name == "SPEED_UP":
            car.velocity += arg
            car.clamp_velocity()
            self._log(f"SPEED_UP {arg}  ->  vel={car.velocity:.1f}")

        elif name == "SLOW_DOWN":
            car.velocity -= arg
            car.clamp_velocity()
            self._log(f"SLOW_DOWN {arg}  ->  vel={car.velocity:.1f}")

        elif name == "SET_SPEED":
            car.velocity = arg
            car.clamp_velocity()
            self._log(f"SET_SPEED {arg}")

        elif name == "STOP":
            car.velocity = 0.0
            car.acceleration = 0.0
            self._fsm_transition("STOP")
            self._log("STOP  ->  vel=0, accel=0")

        elif name == "CRUISE":
            car.acceleration = 0.0
            self._log(f"CRUISE at {car.velocity:.1f} km/h")

        elif name == "MAX_SPEED":
            car.velocity = car.max_velocity
            self._log(f"MAX_SPEED  ->  vel={car.velocity:.1f}")

        # ── Rotation ──
        elif name == "TURN_LEFT":
            car.angle -= arg
            car.normalize_angle()
            self._log(f"TURN_LEFT {arg} deg  ->  heading={car.angle:.1f} deg")

        elif name == "TURN_RIGHT":
            car.angle += arg
            car.normalize_angle()
            self._log(f"TURN_RIGHT {arg} deg  ->  heading={car.angle:.1f} deg")

        elif name == "SET_ANGLE":
            car.angle = arg % 360
            self._log(f"SET_ANGLE {arg} deg")

        elif name == "U_TURN":
            car.angle = (car.angle + 180) % 360
            self._log(f"U_TURN  ->  heading={car.angle:.1f} deg")

        elif name == "RESET_ANGLE":
            car.angle = 0.0
            self._log("RESET_ANGLE  ->  heading=0 deg")

        elif name == "SPIN":
            car.angle = (car.angle + arg) % 360
            self._log(f"SPIN {arg} deg  ->  heading={car.angle:.1f} deg")

        # ── State / FSM ──
        elif name == "START_ENGINE":
            self._fsm_transition("START_ENGINE")
            self._log("START_ENGINE  ->  engine running")

        elif name == "STOP_ENGINE":
            self._fsm_transition("STOP_ENGINE")
            car.velocity = 0.0
            car.acceleration = 0.0
            self._log("STOP_ENGINE  ->  engine off")

        elif name == "PARK":
            self._fsm_transition("PARK")
            car.velocity = 0.0
            car.acceleration = 0.0
            self._log("PARK")

        elif name == "DRIVE":
            self._fsm_transition("DRIVE")
            self._log("DRIVE  ->  moving forward")

        elif name == "REVERSE":
            self._fsm_transition("REVERSE")
            self._log("REVERSE  ->  moving backward")

        elif name == "EMERGENCY_STOP":
            car.velocity = 0.0
            car.acceleration = 0.0
            self._fsm_transition("EMERGENCY_STOP")
            self._log("!! EMERGENCY_STOP !!")

        # ── Actions ──
        elif name == "HONK":
            self._log("HONK! BEEP BEEP!")

        elif name == "TOGGLE_LIGHTS":
            car.lights_on = not car.lights_on
            self._log(f"TOGGLE_LIGHTS  ->  lights {'ON' if car.lights_on else 'OFF'}")

        elif name == "SIGNAL_LEFT":
            car.signal = "LEFT"
            self._log("SIGNAL_LEFT")

        elif name == "SIGNAL_RIGHT":
            car.signal = "RIGHT"
            self._log("SIGNAL_RIGHT")

        elif name == "SIGNAL_OFF":
            car.signal = "OFF"
            self._log("SIGNAL_OFF")

        # ── Control ──
        elif name == "WAIT":
            self._log(f"WAIT {arg}s ...")
            time.sleep(arg)

        # ── Debug ──
        elif name == "PRINT_STATUS":
            print(f"  STATUS: {car.status()}")

        else:
            raise RuntimeError(f"Unknown instruction: {name}")

    # ── Block instructions ────────────────────────────────────────────────

    def _exec_repeat(self, node: RepeatBlock):
        self._log(f"REPEAT {node.count} times")
        for i in range(node.count):
            self._log(f"  -- iteration {i + 1}/{node.count} --")
            for stmt in node.body:
                self._exec(stmt)

    def _exec_if_speed_gt(self, node: IfSpeedGT):
        if self.car.velocity > node.threshold:
            self._log(f"IF_SPEED_GT {node.threshold}  ->  TRUE (vel={self.car.velocity:.1f})")
            for stmt in node.body:
                self._exec(stmt)
        else:
            self._log(f"IF_SPEED_GT {node.threshold}  ->  FALSE (vel={self.car.velocity:.1f})")

    def _exec_if_speed_lt(self, node: IfSpeedLT):
        if self.car.velocity < node.threshold:
            self._log(f"IF_SPEED_LT {node.threshold}  ->  TRUE (vel={self.car.velocity:.1f})")
            for stmt in node.body:
                self._exec(stmt)
        else:
            self._log(f"IF_SPEED_LT {node.threshold}  ->  FALSE (vel={self.car.velocity:.1f})")

    # ── FSM helper ────────────────────────────────────────────────────────

    def _fsm_transition(self, event: str):
        try:
            new_state = self.car.fsm.transition(event)
            self._log(f"  FSM -> {new_state.name}")
        except FSMError as e:
            raise RuntimeError(str(e))

