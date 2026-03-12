"""
visualizer.py -- Top-down map visualization of the car's movement history.

Uses matplotlib to draw:
  - The car's path as a colored line (color = velocity)
  - Direction arrows along the path
  - Key event labels (START_ENGINE, STOP, TURN_LEFT, etc.)
  - Start / End markers
"""

from __future__ import annotations

import math
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
from matplotlib.collections import LineCollection
import numpy as np


# Events worth labeling on the map
LABEL_EVENTS = {
    "START_ENGINE", "STOP_ENGINE", "STOP", "PARK", "DRIVE", "REVERSE",
    "EMERGENCY_STOP", "TURN_LEFT", "TURN_RIGHT", "U_TURN", "SET_ANGLE",
}


def plot_car_map(history: list[dict], title: str = "CarDSL - Top-Down Map"):
    """
    Draw a top-down 2D map of the car's path.

    Parameters
    ----------
    history : list[dict]
        Each entry has keys: x, y, angle, velocity, acceleration, state, label
    title : str
        Window / figure title
    """
    if len(history) < 2:
        print("Not enough movement data to plot (need at least 2 points).")
        return

    xs = [p["x"] for p in history]
    ys = [p["y"] for p in history]
    vels = [p["velocity"] for p in history]

    fig, ax = plt.subplots(figsize=(10, 8))
    fig.patch.set_facecolor("#1e1e2e")
    ax.set_facecolor("#1e1e2e")

    # ── Colored path (velocity heat-map) ──────────────────────────────────
    points = np.array(list(zip(xs, ys))).reshape(-1, 1, 2)
    segments = np.concatenate([points[:-1], points[1:]], axis=1)

    max_vel = max(vels) if max(vels) > 0 else 1
    norm = plt.Normalize(0, max_vel)
    cmap = plt.cm.plasma

    lc = LineCollection(segments, cmap=cmap, norm=norm, linewidth=2.5, alpha=0.9)
    lc.set_array(np.array(vels[:-1]))
    ax.add_collection(lc)

    cbar = fig.colorbar(lc, ax=ax, shrink=0.7, pad=0.02)
    cbar.set_label("Velocity (km/h)", color="white", fontsize=10)
    cbar.ax.yaxis.set_tick_params(color="white")
    plt.setp(cbar.ax.yaxis.get_ticklabels(), color="white")

    # ── Direction arrows every N steps ────────────────────────────────────
    arrow_step = max(1, len(history) // 20)
    for i in range(0, len(history), arrow_step):
        p = history[i]
        if p["velocity"] < 0.1:
            continue
        rad = math.radians(p["angle"])
        dx = math.sin(rad) * 0.8
        dy = math.cos(rad) * 0.8
        ax.annotate(
            "",
            xy=(p["x"] + dx, p["y"] + dy),
            xytext=(p["x"], p["y"]),
            arrowprops=dict(arrowstyle="->", color="#a0a0a0", lw=1.2),
        )

    # ── Event labels ──────────────────────────────────────────────────────
    labeled_positions = set()
    for i, p in enumerate(history):
        if p["label"] in LABEL_EVENTS:
            # Avoid overlapping labels: skip if too close to previous label
            grid_key = (round(p["x"], 1), round(p["y"], 1))
            if grid_key in labeled_positions:
                continue
            labeled_positions.add(grid_key)

            ax.annotate(
                p["label"],
                (p["x"], p["y"]),
                textcoords="offset points",
                xytext=(8, 8),
                fontsize=7,
                color="#f0c040",
                fontweight="bold",
                alpha=0.85,
                bbox=dict(boxstyle="round,pad=0.2", fc="#2a2a3e", ec="#555", alpha=0.7),
            )

    # ── Start & End markers ───────────────────────────────────────────────
    ax.plot(xs[0], ys[0], "o", color="#00ff88", markersize=12, zorder=5,
            label="Start")
    ax.plot(xs[-1], ys[-1], "s", color="#ff4466", markersize=12, zorder=5,
            label="End")

    # Small dot for every recorded position
    ax.scatter(xs, ys, c="white", s=3, alpha=0.3, zorder=3)

    # ── Axes styling ──────────────────────────────────────────────────────
    ax.set_xlabel("X position", color="white", fontsize=11)
    ax.set_ylabel("Y position", color="white", fontsize=11)
    ax.set_title(title, color="white", fontsize=14, fontweight="bold")
    ax.tick_params(colors="white")
    for spine in ax.spines.values():
        spine.set_color("#555")
    ax.set_aspect("equal", adjustable="datalim")
    ax.grid(True, color="#333", linestyle="--", linewidth=0.5, alpha=0.5)
    ax.legend(loc="upper left", facecolor="#2a2a3e", edgecolor="#555",
              labelcolor="white", fontsize=9)

    # ── Padding ───────────────────────────────────────────────────────────
    margin = max(
        (max(xs) - min(xs)) * 0.1,
        (max(ys) - min(ys)) * 0.1,
        2.0,
    )
    ax.set_xlim(min(xs) - margin, max(xs) + margin)
    ax.set_ylim(min(ys) - margin, max(ys) + margin)

    plt.tight_layout()
    plt.show()
