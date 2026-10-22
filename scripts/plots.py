import csv
from pathlib import Path
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

root = Path(__file__).resolve().parent.parent
with (root / 'results/tables/averages.csv').open(newline='') as file:
    rows = list(csv.DictReader(file))

series = {}
for row in rows:
    label = row['algorithm'] + ' (' + row['graph'] + ')'
    series.setdefault(label, []).append(row)

output = root / 'results/plots'
output.mkdir(parents=True, exist_ok=True)
for field, axis, filename in [('V', 'Number of vertices (V)', 'time-vs-vertices.png'),
                              ('E', 'Number of edges (E)', 'time-vs-edges.png')]:
    fig, ax = plt.subplots(figsize=(10, 6))
    for label, points in series.items():
        points.sort(key=lambda row: int(row[field]))
        ax.plot([int(row[field]) for row in points],
                [float(row['average_ns']) / 1_000_000 for row in points],
                marker='o', linewidth=1.6, label=label)
    ax.set_xscale('log')
    ax.set_yscale('log')
    ticks = sorted({int(row[field]) for row in rows})
    ax.set_xticks(ticks, labels=[str(value) for value in ticks])
    ax.set_xlabel(axis)
    ax.set_ylabel('Average execution time (ms, log scale)')
    ax.set_title('Smart city graph algorithms: average of 5 runs')
    ax.grid(True, which='both', alpha=0.25)
    ax.legend(fontsize=8, loc='upper left')
    fig.text(0.5, 0.015, 'Seed 2503; V and E change together; instrumentation included.', ha='center', fontsize=9)
    fig.tight_layout(rect=(0, 0.04, 1, 1))
    fig.savefig(output / filename, dpi=160)
    plt.close(fig)
    print(output / filename)
