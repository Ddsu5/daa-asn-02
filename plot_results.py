import csv
from collections import defaultdict
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parent
with (ROOT / "results/tables/benchmark.csv").open(newline="") as stream:
    rows = list(csv.DictReader(stream))

workloads = [
    ("random_access", "Random access"),
    ("search", "Search"),
    ("insert_front", "Insert at front"),
    ("remove_front", "Remove at front"),
    ("insert_middle", "Insert at middle"),
    ("remove_middle", "Remove at middle"),
    ("priority_insert", "Heap insert"),
    ("priority_extract", "Heap extract"),
]
colors = {"DynamicArray": "#1b72aa", "LinkedList": "#e17a16", "MinHeap": "#39915a"}

for field, name, ylabel in [
    ("average_ms", "execution_time_vs_n.png", "Mean time (ms)"),
    ("average_metric", "operations_vs_n.png", "Count + 1 (see series label)"),
]:
    fig, axes = plt.subplots(4, 2, figsize=(15, 17), constrained_layout=True)
    for ax, (workload, title) in zip(axes.flat, workloads):
        grouped = defaultdict(list)
        for row in rows:
            if row["workload"] == workload:
                grouped[row["structure"]].append(row)
        for structure, values in grouped.items():
            values.sort(key=lambda row: int(row["n"]))
            label = structure if field == "average_ms" else f"{structure}: {values[0]['metric']}"
            ax.plot([int(row["n"]) for row in values],
                    [float(row[field]) + (1 if field == "average_metric" else 0) for row in values],
                    "o-", label=label, color=colors[structure], linewidth=2)
        ax.set_title(title)
        ax.set_xlabel("Initial n")
        ax.set_ylabel(ylabel)
        ax.set_xscale("log")
        ax.set_yscale("log")
        ax.grid(True, which="both", alpha=0.25)
        ax.legend(fontsize=8)
    fig.suptitle("Assignment 2: " + ("execution time" if field == "average_ms" else "measured operations"), fontsize=17)
    output = ROOT / "results/plots" / name
    output.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(output, dpi=160)
    plt.close(fig)
    print(output)
