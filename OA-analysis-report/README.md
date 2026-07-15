# OA Analysis Report

Analysis and visualization tool for Overriding Assignment (OA) static analysis results. Processes conflict data, benchmarks performance, and compares predictions against ground truth.

## Setup

```bash
pip install -r requirements.txt
```

**Dependencies**: `pandas`, `matplotlib`, `seaborn`, `scikit-learn`

## Usage

All analysis is run through `oaAnalysis.py` using `key=value` arguments.

### Single dataset

```bash
python oaAnalysis.py out.json=analysisReport/depth20MergeDatasetSPARK/conflictsData/out.json plot=true
```

### Compare two datasets

```bash
python oaAnalysis.py \
  out.json=analysisReport/depth10MergeDatasetSPARK/conflictsData/out.json \
  out.json2=analysisReport/depth20MergeDatasetSPARK/conflictsData/out.json \
  labels="Depth 10,Depth 20" \
  plot=true
```

### Compare three or more datasets

Pass multiple `out.json` arguments — the tool automatically switches to multi-compare mode:

```bash
python oaAnalysis.py \
  out.json=path/a/out.json \
  out.json2=path/b/out.json \
  out.json3=path/c/out.json \
  labels="A,B,C" \
  plot=true
```

### Performance analysis

```bash
python oaAnalysis.py \
  performancedata=analysisReport/depth20MergeDatasetSPARK/perfomanceData \
  plot=true
```

Aggregates 10 replicate runs from `results1/` – `results10/` using mean and majority voting, then writes aggregated CSVs/JSON to `perfomanceReport/`.

### Ground truth comparison

```bash
python oaAnalysis.py \
  performancedata=analysisReport/depth20MergeDatasetSPARK/perfomanceData \
  mdsgroundtruth=loi.csv \
  plot=true
```

Alternatively, point directly to the aggregated soot results CSV with `perf_soot_path=`:

```bash
python oaAnalysis.py \
  perf_soot_path=analysisReport/depth20MergeDatasetSPARK/perfomanceData/perfomanceReport/performance_soot_results_stats.csv \
  mdsgroundtruth=loi.csv \
  plot=true
```

Matches soot results against `loi.csv` (LOI column) and reports precision, recall, F1, and accuracy using three parity rules. Source resolution priority: `perf_soot_path` > `sootresults` > `performancedata`.

### Subset non-timeout comparison

```bash
python oaAnalysis.py \
  subsetofnontimeouts=path/a/perfomanceData,path/b/perfomanceData \
  plot=true
```

Compares only scenarios that did not time out across all listed datasets.

## Arguments

| Argument | Description |
|---|---|
| `out.json=` | Input conflict JSON file |
| `out.json2=`, `out.json3=`, … | Additional JSONs for multi-dataset comparison |
| `labels=` | Comma-separated display labels for comparison datasets |
| `plot=` | `true` to generate PNG visualizations |
| `performancedata=` | Path to performance data directory |
| `mdsgroundtruth=` | Path to ground truth CSV (`loi.csv`) |
| `perf_soot_path=` | Direct path to `performance_soot_results_stats.csv` for ground truth matching |
| `sootresults=` | Path to a specific `soot-results.csv` for ground truth matching |
| `subsetofnontimeouts=` | Comma-separated paths for non-timeout subset comparison |

## Analysis modules

### Conflict analysis (`analyzers/conflict_analyzer.py`)

Processes `out.json` and computes per-conflict metrics:

- Stack trace lengths (left and right), depth, and diff
- Whether conflicting methods share the same class or method
- Conflict type classification (A1, B1, B2/C2, D3, F2, Other) based on depth and class/method overlap
- Distribution across scenarios and JARs
- Depth loss analysis: how many conflicts/scenarios are cut at depth thresholds 5–14

**Outputs**: `conflict_stacktrace_stats.csv`, `scenariojar_stats.csv`, console summary

### Scenario analysis (`analyzers/scenario_analyzer.py`)

Aggregates conflict metrics at the scenario (JAR) level:

- Conflicts per scenario / scenarios per JAR
- Median and max depth per scenario grouped by depth ranges (0–2, 2–5, 5–10, 10–20)
- Percentile thresholds: fraction of scenarios meeting various depth/conflict-count criteria

### Performance analysis (`analyzers/performance_analyzer.py`)

Reads 10 replicate benchmark runs and aggregates them:

- Execution time (mean, median, percentiles, timeout rate)
- Peak memory (GB) and peak CPU (%) via resource usage timeseries
- Majority-vote aggregation of boolean OA-Inter predictions across replicates
- Timeout detection and per-scenario statistics

**Outputs**: `performance_soot_results_stats.csv`, `performance_summary_stats.json`, `performance_resource_stats.csv`

### Ground truth comparison (`analyzers/ground_truth_comparator.py`)

Validates OA predictions against manually-labeled LOI ground truth:

- Matches on (project, class, method, merge commit)
- Applies three parity rules: strict, timeout-as-false, timeout-as-true
- Computes confusion matrix, precision, recall, F1, and accuracy

**Output**: `ground_truth_comparison_summary.txt`

## Visualizations

When `plot=true`, PNG files are saved alongside the input data. Generated plots include:

- **Histograms**: stack depth, stacktrace diff, execution time, memory, CPU
- **Pie charts**: conflict type distribution, same-class rate, same-method rate
- **Line plots**: conflicts vs. depth threshold, depth loss curves
- **Bar charts**: conflicts/scenarios per JAR, depth metrics per scenario, timeout rates
- **Comparison plots**: side-by-side versions of all the above for multi-dataset runs
- **Performance timeseries**: raw and spline-smoothed CPU/memory over time

## Project structure

```
OA-analysis-report/
├── oaAnalysis.py               # Entry point: argument parsing and pipeline orchestration
├── constants.py                # Column names, thresholds, time groups, resource buckets
├── loi.csv                     # Ground truth LOI labels
├── requirements.txt
│
├── utils/
│   └── label_utils.py          # Shared path-to-label extraction helper
│
├── processors/
│   ├── conflict_processor.py   # Parses out.json → conflict_stacktrace_stats.csv
│   └── performance_aggregator.py  # Aggregates 10 runs → perfomanceReport/
│
├── analyzers/
│   ├── conflict_analyzer.py    # Conflict statistics and plots
│   ├── scenario_analyzer.py    # Scenario-level statistics and plots
│   ├── performance_analyzer.py # Performance benchmarking and plots
│   └── ground_truth_comparator.py  # LOI prediction evaluation
│
├── visualization/
│   ├── __init__.py             # Visualizer class (composes all mixins)
│   ├── bar_charts.py           # Bar chart methods (single, grouped, comparison, horizontal)
│   ├── histograms.py           # Histogram and distribution comparison methods
│   ├── pie_charts.py           # Pie chart methods
│   ├── timeseries.py           # Timeseries, smoothed curve, and grouped timeseries methods
│   └── line_charts.py          # Depth line plots and JAR metric charts
│
└── analysisReport/             # Results organised by depth + dataset + call graph
    ├── depth5MergeDatasetSPARK/
    ├── depth10MergeDatasetSPARK/
    ├── depth20MergeDatasetSPARK/
    ├── depth20MergeDatasetCHA/
    ├── depth20MergeDatasetCHA&SPARK/
    └── …
        ├── conflictsData/      # out.json, CSVs, PNGs
        └── perfomanceData/
            ├── results1/ … results10/
            └── perfomanceReport/
```
