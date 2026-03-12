"""
fsm.py — Finite State Machine for the Car's operational state.

States
──────
  OFF        – Engine is off; no operations possible
  IDLE       – Engine running, car stationary
  MOVING     – Car moving forward
  REVERSING  – Car moving in reverse
  PARKED     – Car is parked (hand-brake on)

Transition Table
────────────────
  Current State   Event (Instruction)    Next State
  ─────────────   ───────────────────    ──────────
  OFF             START_ENGINE           IDLE
  IDLE            DRIVE                  MOVING
  IDLE            REVERSE                REVERSING
  IDLE            PARK                   PARKED
  IDLE            STOP_ENGINE            OFF
  MOVING          STOP / PARK            PARKED
  MOVING          REVERSE                REVERSING
  MOVING          EMERGENCY_STOP         PARKED
  REVERSING       STOP / PARK            PARKED
  REVERSING       DRIVE                  MOVING
  REVERSING       EMERGENCY_STOP         PARKED
  PARKED          DRIVE                  MOVING
  PARKED          REVERSE                REVERSING
  PARKED          STOP_ENGINE            OFF
"""

from __future__ import annotations
from enum import Enum, auto


class CarState(Enum):
    OFF = auto()
    IDLE = auto()
    MOVING = auto()
    REVERSING = auto()
    PARKED = auto()


class FSMError(Exception):
    """Raised when an invalid state transition is attempted."""
    def __init__(self, current: CarState, event: str):
        super().__init__(
            f"Invalid transition: cannot perform '{event}' while in state {current.name}"
        )
        self.current = current
        self.event = event


class CarFSM:
    """
    Deterministic Finite State Machine for the car's operational mode.

    The FSM enforces valid transitions — e.g. you cannot DRIVE while the
    engine is OFF, and you cannot STOP_ENGINE while MOVING.
    """

    # ── Transition table ──────────────────────────────────────────────────
    # Maps (current_state, event) → next_state
    _transitions: dict[tuple[CarState, str], CarState] = {
        # From OFF
        (CarState.OFF, "START_ENGINE"):       CarState.IDLE,

        # From IDLE
        (CarState.IDLE, "DRIVE"):             CarState.MOVING,
        (CarState.IDLE, "REVERSE"):           CarState.REVERSING,
        (CarState.IDLE, "PARK"):              CarState.PARKED,
        (CarState.IDLE, "STOP_ENGINE"):       CarState.OFF,

        # From MOVING
        (CarState.MOVING, "STOP"):            CarState.PARKED,
        (CarState.MOVING, "PARK"):            CarState.PARKED,
        (CarState.MOVING, "REVERSE"):         CarState.REVERSING,
        (CarState.MOVING, "EMERGENCY_STOP"):  CarState.PARKED,

        # From REVERSING
        (CarState.REVERSING, "STOP"):         CarState.PARKED,
        (CarState.REVERSING, "PARK"):         CarState.PARKED,
        (CarState.REVERSING, "DRIVE"):        CarState.MOVING,
        (CarState.REVERSING, "EMERGENCY_STOP"): CarState.PARKED,

        # From PARKED
        (CarState.PARKED, "DRIVE"):           CarState.MOVING,
        (CarState.PARKED, "REVERSE"):         CarState.REVERSING,
        (CarState.PARKED, "STOP_ENGINE"):     CarState.OFF,
    }

    def __init__(self):
        self.state = CarState.OFF

    def transition(self, event: str) -> CarState:
        """
        Attempt a state transition for the given event.
        Returns the new state, or raises FSMError if invalid.
        """
        key = (self.state, event)
        if key not in self._transitions:
            raise FSMError(self.state, event)
        self.state = self._transitions[key]
        return self.state

    def get_state(self) -> CarState:
        return self.state

    def reset(self):
        self.state = CarState.OFF

    # ── Visualization helper ──────────────────────────────────────────────

    @classmethod
    def print_transition_table(cls):
        """Pretty-print the full transition table."""
        print(f"{'Current State':<15} {'Event':<20} {'Next State':<15}")
        print("-" * 50)
        for (src, event), dst in sorted(
            cls._transitions.items(), key=lambda x: (x[0][0].value, x[0][1])
        ):
            print(f"{src.name:<15} {event:<20} {dst.name:<15}")
