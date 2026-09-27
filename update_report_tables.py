import csv
from pathlib import Path

root = Path(__file__).resolve().parent
with (root / "results/tables/benchmark.csv").open(newline="") as stream:
    rows = list(csv.DictReader(stream))

sections = [
    ("random_access", "Random access (10,000 gets)"),
    ("search", "Search (1,000 queries)"),
    ("insert_front", "Insertion at index 0 (1,000 inserts)"),
    ("remove_front", "Removal at index 0 (1,000 removals)"),
    ("insert_middle", "Insertion at index n/2 (1,000 inserts)"),
    ("remove_middle", "Removal at index n/2 (1,000 removals)"),
    ("priority_insert", "Heap insertion (n inserts)"),
    ("priority_extract", "Heap extraction (n extracts)"),
]

tables = []
for workload, title in sections:
    selected = [row for row in rows if row["workload"] == workload]
    tables.append(f"### {title}\n")
    if workload.startswith("priority"):
        tables.extend(["| n | Heap ms / comparisons | Theory |", "|---:|---:|---|"])
    else:
        tables.extend(["| n | Dynamic array ms / metric | Linked list ms / metric | Theory: array; list |",
                       "|---:|---:|---:|---|"])
    for n in [100, 1_000, 10_000, 100_000]:
        by_structure = {row["structure"]: row for row in selected if int(row["n"]) == n}
        if workload.startswith("priority"):
            row = by_structure["MinHeap"]
            tables.append(f"| {n:,} | {float(row['average_ms']):.3f} / {float(row['average_metric']):,.0f} | {row['theory']} |")
        else:
            array = by_structure["DynamicArray"]
            linked = by_structure["LinkedList"]
            tables.append(
                f"| {n:,} | {float(array['average_ms']):.3f} / {float(array['average_metric']):,.0f} "
                f"| {float(linked['average_ms']):.3f} / {float(linked['average_metric']):,.0f} "
                f"| {array['theory']}; {linked['theory']} |"
            )
    tables.append("")

readme = root / "README.md"
content = readme.read_text()
start, end = "### Random access (10,000 gets)", "## 6. Discussion and performance analysis"
if start not in content:
    raise ValueError("README has no results tables")
left, rest = content.split(start, 1)
_, right = rest.split(end, 1)
readme.write_text(left + "\n".join(tables) + "\n" + end + right)
print("Updated", readme)
