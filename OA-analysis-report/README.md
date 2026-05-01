# OA Analysis Report

Analysis and visualization tool for Overriding Assignment (OA) static analysis results. Processes conflict data, benchmarks performance, and compares predictions against ground truth.

## Setup

```bash
pip install -r requirements.txt
```

**Dependencies**: `pandas`, `matplotlib`, `seaborn`, `scikit-learn`

## Usage

All analysis is run through `oaAnalysis.py` using key=value arguments.

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

Matches soot results against `loi.csv` (LOI column) and reports precision, recall, F1, and accuracy using AND/OR/minority-voting parity rules. Source resolution priority: `perf_soot_path` > `sootresults` > `performancedata`.

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

### Conflict analysis (`conflictAnalysis/`)

Processes `out.json` and computes per-conflict metrics:

- Stack trace lengths (left and right), depth, and diff
- Whether conflicting methods share the same class or method
- Conflict type classification (A1, B1, B2/C2, D3, F2, Other) based on depth and class/method overlap
- Distribution across scenarios and JARs
- Depth loss analysis: how many conflicts/scenarios are cut at depth thresholds 5, 10, 14

**Outputs**: `conflict_stacktrace_stats.csv`, `scenariojar_stats.csv`, console summary

### Scenario analysis (`scenarioAnalysis/`)

Aggregates conflict metrics at the scenario (JAR) level:

- Conflicts per scenario / scenarios per JAR
- Median and max depth per scenario
- Percentile thresholds: fraction of scenarios meeting various depth/conflict-count criteria

### Performance analysis (`perfomanceAnalysis/`)

Reads 10 replicate benchmark runs and aggregates them:

- Execution time (mean, median, percentiles, timeout rate)
- Peak memory (GB) and peak CPU (%) via resource usage timeseries
- Majority-vote aggregation of boolean OA-Inter predictions across replicates
- Timeout detection and per-scenario statistics

**Outputs**: `performance_soot_results_stats.csv`, `performance_summary_stats.json`, `performance_resource_stats.csv`

### Ground truth comparison (`groundTruthAnalysis/`)

Validates OA predictions against manually-labeled LOI ground truth:

- Matches on (project, class, method, merge commit)
- Applies three parity rules: AND, OR, minority voting
- Computes confusion matrix, precision, recall, F1, and accuracy
- Supports two OA-Inter interpretations for sensitivity analysis

## Visualizations

When `plot=true`, PNG files are saved alongside the input data. Generated plots include:

- **Histograms**: stack depth, stacktrace diff, execution time, memory, CPU
- **Pie charts**: conflict type distribution, same-class rate, same-method rate
- **Line plots**: conflicts vs. depth threshold, depth loss curves
- **Bar charts**: conflicts/scenarios per JAR, depth metrics per scenario, timeout rates
- **Comparison plots**: side-by-side versions of all the above for multi-dataset runs
- **Performance timeseries**: raw and spline-smoothed CPU/memory over time
- **Ground truth**: confusion matrix heatmaps, precision/recall/F1 bar charts

## Project structure

```
OA-analysis-report/
├── oaAnalysis.py               # Entry point and orchestration
├── constants.py                # Column names, thresholds, plot config
├── visualization.py            # All matplotlib/seaborn plot functions
├── loi.csv                     # Ground truth LOI labels
├── requirements.txt
├── conflictAnalysis/
│   └── conflictAnalysis.py
├── scenarioAnalysis/
│   └── scenarioAnalysis.py
├── perfomanceAnalysis/
│   └── perfomanceAnalysis.py
├── groundTruthAnalysis/
│   └── groundTruthComparison.py
└── analysisReport/             # Results organized by depth + dataset + call graph
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
