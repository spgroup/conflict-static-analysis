import os
import json
import pandas as pd
from visualization import Visualizer
from constants import *


class PerformanceAnalyzer:
    def __init__(self):
        self.visualizer = Visualizer()
        self.output_dir = "."
        self.perf_summary = {}
        self.perf_soot = None
        self.perf_resource = None

    def analyze(self, plot=True, output_dir="."):
        """Analyze performance data from aggregated files"""
        self.output_dir = output_dir

        try:
            # Read the aggregated files
            self._load_performance_data()

            # Print performance statistics
            self._print_performance_stats()

            # Create plots if requested
            if plot:
                self._create_plots()

        except Exception as e:
            print(f"Error analyzing performance data: {e}")

    def _load_performance_data(self):
        """Load the three aggregated performance files"""
        # Load performance summary stats (JSON)
        summary_path = os.path.join(self.output_dir, PERFORMANCE_SUMMARY_STATS_JSON)
        if os.path.exists(summary_path):
            with open(summary_path, "r") as f:
                self.perf_summary = json.load(f)

        # Load performance soot results stats (CSV)
        soot_path = os.path.join(self.output_dir, PERFORMANCE_SOOT_STATS_CSV)
        if os.path.exists(soot_path):
            self.perf_soot = pd.read_csv(soot_path, sep=";")

        # Load performance resource stats (CSV)
        resource_path = os.path.join(self.output_dir, PERFORMANCE_RESOURCE_STATS_CSV)
        if os.path.exists(resource_path):
            self.perf_resource = pd.read_csv(resource_path)

    def _print_performance_stats(self):
        """Print performance statistics"""
        print("\n" + "=" * 60)
        print("Performance Analysis Results")
        print("=" * 60)

        # Print summary statistics
        if self.perf_summary:
            print("\nPerformance Summary (Aggregated):")
            print(f"  Mode: {self.perf_summary.get('mode', 'N/A')}")
            print(f"  Callgraph: {self.perf_summary.get('callgraph', 'N/A')}")
            print(f"  Status: {self.perf_summary.get('status', 'N/A')}")
            print(
                f"  Duration (seconds): {self.perf_summary.get('duration_seconds', 'N/A'):.2f}"
            )
            print(
                f"  Peak Memory (GB): {self.perf_summary.get('peak_memory_gb', 'N/A'):.4f}"
            )
            print(
                f"  Peak CPU (%): {self.perf_summary.get('peak_cpu_percent', 'N/A'):.2f}"
            )

        # Print soot results statistics (excluding timeouts)
        if self.perf_soot is not None:
            # Identify timeouts (OA Inter = true)
            timeout_count = (self.perf_soot["OA Inter"] == "timeout").sum()
            non_timeout_df = self.perf_soot[self.perf_soot["OA Inter"] != "timeout"]

            print("\nSoot Results:")
            print(f"  Total scenarios: {len(self.perf_soot)}")
            print(f"  Timeouts: {timeout_count}")
            print(f"  Non-timeout scenarios: {len(non_timeout_df)}")
            print(
                f"  Timeouts (%) : {(timeout_count / len(self.perf_soot) * 100):.2f}%"
            )

            if len(non_timeout_df) > 0:
                print("\n  Time Statistics (seconds):")
                print(f"  Average time (seconds): {non_timeout_df['Time'].mean():.2f}")
                print(f"  Median time (seconds): {non_timeout_df['Time'].median():.2f}")
                print(f"  Max time (seconds): {non_timeout_df['Time'].max():.2f}")
                print(f"  Min time (seconds): {non_timeout_df['Time'].min():.2f}")
                false_count = (self.perf_soot["OA Inter"] == "false").sum()
                false_percentage = (
                    (false_count / len(self.perf_soot) * 100)
                    if len(self.perf_soot) > 0
                    else 0
                )

                print(f"  False results (%) : {false_percentage:.2f}%")
                print(f"  True results (%) : {100 - false_percentage:.2f}%")

                # Print percentiles
                print("\n  Time Percentiles:")
                percentiles = [50, 75, 90, 95, 99]
                for p in percentiles:
                    value = non_timeout_df["Time"].quantile(p / 100)
                    print(f"    {p}th percentile: {value:.2f} seconds")

        # Print resource usage statistics
        if self.perf_resource is not None and len(self.perf_resource) > 0:
            print("\nResource Usage (Aggregated):")
            print(f"  Total records: {len(self.perf_resource)}")
            print(f"  Average CPU (%): {self.perf_resource['CPU_Percent_Total'].mean():.2f}")
            print(f"  Max CPU (%): {self.perf_resource['CPU_Percent_Total'].max():.2f}")
            print(f"  Min CPU (%): {self.perf_resource['CPU_Percent_Total'].min():.2f}")
            print(
                f"  Average Memory (GB): {self.perf_resource['Memory_GB'].mean():.4f}"
            )
            print(f"  Max Memory (GB): {self.perf_resource['Memory_GB'].max():.4f}")
            print(f"  Min Memory (GB): {self.perf_resource['Memory_GB'].min():.4f}")

        # Print statistics from 10 individual runs
        self._print_individual_run_stats()

        print("\n" + "=" * 60 + "\n")

    def _print_individual_run_stats(self):
        """Calculate and print statistics for each column from 10 individual runs"""
        print("\nStatistics from 10 Individual Runs:")
        print("-" * 60)

        # Find the results directories (results1 through results10)
        results_folders = []
        parent_dir = os.path.dirname(self.output_dir)

        for i in range(1, 11):
            results_path = os.path.join(parent_dir, f"results{i}")
            if os.path.isdir(results_path):
                results_folders.append((i, results_path))

        if not results_folders:
            print("  No individual result folders found (results1-results10)")
            return

        print(f"  Found {len(results_folders)} result folders\n")

        # Load and analyze Soot results statistics
        self._analyze_results_file(
            results_folders, "soot-results.csv", sep=";", file_type="Soot Results"
        )

        # Load and analyze Resource usage statistics
        self._analyze_results_file(
            results_folders,
            "resource_usage_series.csv",
            sep=",",
            file_type="Resource Usage",
        )

    def _analyze_results_file(self, results_folders, filename, sep=",", file_type=""):
        """Analyze a specific results file across all result folders"""
        print(f"\n  {file_type} Analysis ({filename}):")
        print("  " + "-" * 56)

        # Load data from all results folders
        dfs = []
        valid_folders = []

        for run_num, results_path in results_folders:
            file_path = os.path.join(results_path, filename)
            if os.path.exists(file_path):
                try:
                    df = pd.read_csv(file_path, sep=sep)
                    # Only add non-empty DataFrames
                    if len(df) > 0:
                        dfs.append(df)
                        valid_folders.append(run_num)
                except Exception as e:
                    print(f"    Warning: Could not read {file_path}: {e}")
            else:
                print(f"    Warning: File not found: {file_path}")

        if not dfs:
            print(f"  No valid {filename} files found with data")
            return

        # Get numeric columns - try to convert all columns to numeric
        # Use the first dataframe with data to determine numeric columns
        first_df = dfs[0]
        numeric_cols = []
        
        for col in first_df.columns:
            # Try to convert column to numeric
            converted = pd.to_numeric(first_df[col], errors="coerce")
            if converted.notna().any():  # If at least some values were converted
                numeric_cols.append(col)

        if not numeric_cols:
            print(f"  No numeric columns found in {filename}")
            return

        # Calculate statistics for each numeric column
        for col in numeric_cols:
            values = []
            for df in dfs:
                if col in df.columns:
                    # For soot results, exclude timeouts
                    df_filtered = df
                    if "soot-results.csv" in filename and "OA Inter" in df.columns:
                        df_filtered = df[df["OA Inter"] != "timeout"]
                    
                    # Extract numeric values, ignoring non-numeric entries
                    col_values = pd.to_numeric(df_filtered[col], errors="coerce").dropna()
                    values.extend(col_values.tolist())

            if values:
                values_series = pd.Series(values)
                mean = values_series.mean()
                std_dev = values_series.std()
                # Coefficient of variation (variability as percentage)
                cv = (std_dev / mean * 100) if mean != 0 else 0

                print(f"    {col}:")
                print(f"      Mean: {mean:.6f}")
                print(f"      Std Dev: {std_dev:.6f}")
                print(f"      Variability (CV %): {cv:.2f}%")
            else:
                print(f"    {col}: No numeric data available")

    def _create_plots(self):
        """Create performance visualization plots"""
        if self.perf_soot is not None:
            self._plot_time_histogram()
            self._plot_time_histogram_including_timeouts()
            self._plot_time_distribution_bar()
            self._plot_time_distribution_bar_including_timeouts()

        if self.perf_resource is not None and len(self.perf_resource) > 0:
            self._plot_resource_timeseries()

    def _plot_time_histogram(self):
        """Plot histogram of time field excluding timeouts"""
        if self.perf_soot is None:
            raise Exception("Soot performance data not loaded")

        non_timeout_df = self.perf_soot[self.perf_soot["OA Inter"] != "timeout"]

        if len(non_timeout_df) > 0:
            self.visualizer.plot_histogram(
                data=non_timeout_df["Time"],
                bins=30,
                title="Test Execution Time Distribution (Excluding Timeouts)",
                xlabel="Time (seconds)",
                filename=os.path.join(
                    self.output_dir, "performance_time_histogram.png"
                ),
            )

    def _plot_time_histogram_including_timeouts(self):
        """Plot histogram of time field including timeouts (shown as max value)"""
        if self.perf_soot is None:
            raise Exception("Soot performance data not loaded")

        # For timeouts, use a large value (e.g., 400 seconds as representative)
        time_data = []
        non_timeout_df = self.perf_soot[self.perf_soot["OA Inter"] != "timeout"]
        timeout_count = (self.perf_soot["OA Inter"] == "timeout").sum()

        if len(non_timeout_df) > 0:
            time_data = non_timeout_df["Time"].tolist()

        # Add timeout values (represented as 400 seconds for visualization)
        if timeout_count > 0:
            max_time = max(time_data) if time_data else 0
            timeout_value = max(
                max_time + 50, 400
            )  # Add 50 seconds above max or use 400
            time_data.extend([timeout_value] * timeout_count)

        if len(time_data) > 0:
            self.visualizer.plot_histogram(
                data=time_data,
                bins=35,
                title="Test Execution Time Distribution (Including Timeouts)",
                xlabel="Time (seconds)",
                filename=os.path.join(
                    self.output_dir, "performance_time_histogram_with_timeouts.png"
                ),
            )

    def _plot_time_distribution_bar(self):
        """Plot bar chart showing distribution of scenarios across time groups"""
        if self.perf_soot is None:
            raise Exception("Soot performance data not loaded")

        non_timeout_df = self.perf_soot[self.perf_soot["OA Inter"] != "timeout"]

        if len(non_timeout_df) > 0:
            # Define time groups (in seconds)
            time_groups = {
                "1-5": (1, 5),
                "5-10": (5, 10),
                "10-30": (10, 30),
                "30-60": (30, 60),
                "60-120": (60, 120),
                "120+": (120, float("inf")),
            }

            # Count scenarios in each group
            group_counts = {}
            for group_name, (min_time, max_time) in time_groups.items():
                count = len(
                    non_timeout_df[
                        (non_timeout_df["Time"] >= min_time)
                        & (non_timeout_df["Time"] < max_time)
                    ]
                )
                if count > 0:  # Only include groups with at least one scenario
                    group_counts[group_name] = count

            # Create bar chart data with percentages
            if group_counts:
                total = sum(group_counts.values())
                x_labels = list(group_counts.keys())
                y_percentages = [count / total * 100 for count in group_counts.values()]

                self.visualizer.plot_bar_chart(
                    x=x_labels,
                    y=y_percentages,
                    title="Scenario Distribution by Execution Time (Excluding Timeouts)",
                    xlabel="Execution Time Range (seconds)",
                    ylabel="Percentage (%)",
                    filename=os.path.join(
                        self.output_dir, "performance_time_distribution_bar.png"
                    ),
                )

    def _plot_time_distribution_bar_including_timeouts(self):
        """Plot bar chart showing distribution of scenarios across time groups including timeouts"""
        if self.perf_soot is None:
            raise Exception("Soot performance data not loaded")

        # Define time groups (in seconds) with updated 120-300 and timeout(>300)
        time_groups = {
            "0-5": (0, 5),
            "5-10": (5, 10),
            "10-30": (10, 30),
            "30-60": (30, 60),
            "60-120": (60, 120),
            "120-300": (120, 300),
            "timeouts": (300, float("inf")),
        }

        # Count scenarios in each group
        group_counts = {}
        non_timeout_df = self.perf_soot[self.perf_soot["OA Inter"] != "timeout"]
        timeout_df = self.perf_soot[self.perf_soot["OA Inter"] == "timeout"]
        for group_name, (min_time, max_time) in time_groups.items():
            if group_name == "timeouts":
                # For timeouts, count the timeout entries marked as "timeout"
                count = len(timeout_df)
            else:
                count = len(
                    non_timeout_df[
                        (non_timeout_df["Time"] >= min_time)
                        & (non_timeout_df["Time"] < max_time)
                    ]
                )

            if count > 0:  # Only include groups with at least one scenario
                group_counts[group_name] = count

        # Create bar chart data with percentages
        if group_counts:
            total = sum(group_counts.values())
            print("Total groups counted (including timeouts):", total)
            x_labels = list(group_counts.keys())
            y_percentages = [count / total * 100 for count in group_counts.values()]

            self.visualizer.plot_bar_chart(
                x=x_labels,
                y=y_percentages,
                title="Scenario Distribution by Execution Time (Including Timeouts)",
                xlabel="Execution Time Range (seconds)",
                ylabel="Percentage (%)",
                filename=os.path.join(
                    self.output_dir,
                    "performance_time_distribution_bar_with_timeouts.png",
                ),
            )

    @staticmethod
    def _label_from_path(path):
        """Extract a human-readable label from a path.

        E.g. 'analysisReport/depth5CHA/perfomanceData/perfomanceReport'
             -> 'depth5 CHA'
        Looks for a path component matching depthN<ALGO> (case-insensitive).
        Falls back to the last meaningful path component.
        """
        import re

        parts = path.replace("\\", "/").split("/")
        for part in parts:
            m = re.match(r"(depth\d+)([a-zA-Z]+)", part, re.IGNORECASE)
            if m:
                return f"{m.group(1)} {m.group(2).upper()}"
        return next((p for p in reversed(parts) if p), path)

    def analyze_subset_non_timeouts(self, paths):
        """
        Given a list of performance report directory paths, find the subset of
        scenarios (identified by project, class, method, merge commit) that had
        NO timeout in ANY of the provided paths, then produce grouped plots
        saved in the first valid path:
          - time distribution bar (subset, no timeouts)
          - time distribution bar with timeouts (all scenarios, no subset filter)
          - CPU / Memory timeseries and smoothed variants
        """
        SOOT_CSV = PERFORMANCE_SOOT_STATS_CSV
        RESOURCE_CSV = PERFORMANCE_RESOURCE_STATS_CSV
        ID_COLS = ["project", "class", "method", "merge commit"]

        print("\n" + "=" * 60)
        print("Subset Non-Timeouts Analysis")
        print("=" * 60)

        # --- 1. Load all soot CSVs ---
        path_dfs = {}
        for path in paths:
            csv_path = os.path.join(path, SOOT_CSV)
            if not os.path.exists(csv_path):
                print(f"  Warning: File not found, skipping path: {csv_path}")
                continue
            df = pd.read_csv(csv_path, sep=";")
            missing = [c for c in ID_COLS + ["OA Inter", "Time"] if c not in df.columns]
            if missing:
                print(f"  Warning: Missing columns {missing} in {csv_path}, skipping.")
                continue
            path_dfs[path] = df
            total = len(df)
            timeouts = (df["OA Inter"] == "timeout").sum()
            print(f"  Loaded {csv_path}: {total} scenarios, {timeouts} timeouts")

        if not path_dfs:
            print("  No valid paths found. Aborting subset analysis.")
            return

        # --- 2. Load resource CSVs (best-effort, keyed by label) ---
        resource_series = {}  # label -> DataFrame
        for path in path_dfs:
            label = self._label_from_path(path)
            res_path = os.path.join(path, RESOURCE_CSV)
            if os.path.exists(res_path):
                res_df = pd.read_csv(res_path)
                if not res_df.empty and "Time_Sec" in res_df.columns:
                    resource_series[label] = res_df

        # --- 3. Find common non-timeout scenario keys ---
        non_timeout_key_sets = []
        for df in path_dfs.values():
            nt = df[df["OA Inter"] != "timeout"]
            non_timeout_key_sets.append(set(zip(*[nt[c].astype(str) for c in ID_COLS])))

        common_keys = non_timeout_key_sets[0]
        for s in non_timeout_key_sets[1:]:
            common_keys &= s

        print(f"\n  Common non-timeout scenarios across all paths: {len(common_keys)}")

        if not common_keys:
            print("  No common non-timeout scenarios found. Nothing to plot.")
            return

        output_path = next(iter(path_dfs))

        TIME_GROUPS = {
            "0-5": (0, 5),
            "5-10": (5, 10),
            "10-30": (10, 30),
            "30-60": (30, 60),
            "60-120": (60, 120),
            "120+": (120, float("inf")),
        }
        TIME_GROUPS_WITH_TIMEOUTS = {
            "0-5": (0, 5),
            "5-10": (5, 10),
            "10-30": (10, 30),
            "30-60": (30, 60),
            "60-120": (60, 120),
            "120-300": (120, 300),
            "timeouts": None,  # sentinel – counted separately
        }

        # --- 4. Build per-path subsets, print stats, collect data ---
        labels = []
        subset_dfs = {}
        all_dfs = {}  # full (unfiltered) soot DFs per label
        previous_avg = None
        previous_worst10 = None

        for path, df in path_dfs.items():
            key_col = list(zip(*[df[c].astype(str) for c in ID_COLS]))
            mask = pd.Series([k in common_keys for k in key_col], index=df.index)
            subset_df = df[mask].copy()
            label = self._label_from_path(path)
            labels.append(label)
            subset_dfs[label] = subset_df
            all_dfs[label] = df

            nt = subset_df[subset_df["OA Inter"] != "timeout"]
            avg = nt["Time"].mean() if len(nt) > 0 else 0
            w10_threshold = nt["Time"].quantile(0.9) if len(nt) > 0 else 0
            w10_avg = (
                nt[nt["Time"] >= w10_threshold]["Time"].mean() if len(nt) > 0 else 0
            )

            print(f"\n  Dataset: {label}  (path: {path})")
            print(f"    Subset size: {len(subset_df)} scenarios")
            print(f"    Average time: {avg:.2f}s", end="")
            if previous_avg is not None:
                pct = (avg - previous_avg) / previous_avg * 100
                print(f"  ({'+' if pct >= 0 else ''}{pct:.1f}%)")
            else:
                print()
            print(f"    Worst 10% avg time: {w10_avg:.2f}s", end="")
            if previous_worst10 is not None:
                pct = (w10_avg - previous_worst10) / previous_worst10 * 100
                print(f"  ({'+' if pct >= 0 else ''}{pct:.1f}%)")
            else:
                print()

            previous_avg = avg
            previous_worst10 = w10_avg

        # --- 5. Grouped plots ---
        self._plot_grouped_time_distribution(
            subset_dfs, labels, TIME_GROUPS, output_path
        )

        self._plot_grouped_time_distribution_with_timeouts(
            all_dfs, labels, TIME_GROUPS_WITH_TIMEOUTS, output_path
        )

        if resource_series:
            self._plot_grouped_resource_timeseries(resource_series, labels, output_path)

        print("\n" + "=" * 60 + "\n")

    def _plot_grouped_time_distribution(
        self, subset_dfs, labels, time_groups, output_path
    ):
        """Single grouped bar chart comparing time distributions across all datasets."""
        x_labels = list(time_groups.keys())
        datasets = []

        for label in labels:
            nt = subset_dfs[label][subset_dfs[label]["OA Inter"] != "timeout"]
            counts = [
                len(nt[(nt["Time"] >= min_t) & (nt["Time"] < max_t)])
                for min_t, max_t in time_groups.values()
            ]
            total = sum(counts)
            datasets.append([c / total * 100 if total > 0 else 0 for c in counts])

        filename = os.path.join(
            output_path,
            "subset_non_timeouts_time_distribution_grouped.png",
        )

        self.visualizer.plot_grouped_bar_chart(
            x=x_labels,
            datasets=datasets,
            labels=labels,
            title="Scenario Time Distribution – Common Non-Timeout Subset",
            xlabel="Execution Time Range (seconds)",
            ylabel="Percentage (%)",
            filename=filename,
        )
        print(f"\n  Saved grouped plot: {filename}")

    def _plot_grouped_time_distribution_with_timeouts(
        self, all_dfs, labels, time_groups, output_path
    ):
        """Grouped bar chart of time distribution including timeouts, all scenarios."""
        x_labels = list(time_groups.keys())
        datasets = []

        for label in labels:
            df = all_dfs[label]
            nt = df[df["OA Inter"] != "timeout"]
            timeout_count = (df["OA Inter"] == "timeout").sum()
            counts = []
            for group_name, bounds in time_groups.items():
                if bounds is None:  # sentinel for "timeouts" bucket
                    counts.append(int(timeout_count))
                else:
                    min_t, max_t = bounds
                    counts.append(len(nt[(nt["Time"] >= min_t) & (nt["Time"] < max_t)]))
            total = sum(counts)
            datasets.append([c / total * 100 if total > 0 else 0 for c in counts])

        filename = os.path.join(
            output_path,
            "all_scenarios_time_distribution_with_timeouts_grouped.png",
        )

        self.visualizer.plot_grouped_bar_chart(
            x=x_labels,
            datasets=datasets,
            labels=labels,
            title="Scenario Distribution by Execution Time (Including Timeouts) – All Scenarios",
            xlabel="Execution Time Range (seconds)",
            ylabel="Percentage (%)",
            filename=filename,
        )
        print(f"  Saved grouped plot: {filename}")

    def _plot_grouped_resource_timeseries(self, resource_series, labels, output_path):
        """Four grouped resource plots: raw CPU, raw Memory, smoothed+raw, smoothed-only."""
        import numpy as np

        def _series(res_dfs, col):
            """Return list of (x_arr, y_arr) for the given column, in label order."""
            result = []
            for lbl in labels:
                if lbl in res_dfs and col in res_dfs[lbl].columns:
                    df = res_dfs[lbl]
                    result.append((df["Time_Sec"].values, df[col].values))
                else:
                    result.append((np.array([]), np.array([])))
            return result

        valid_labels = [l for l in labels if l in resource_series]

        # Raw CPU
        self.visualizer.plot_grouped_timeseries(
            series_list=_series(resource_series, "CPU_Percent_Total"),
            labels=labels,
            title="CPU Usage Over Time",
            xlabel="Time (seconds)",
            ylabel="CPU Usage (%)",
            filename=os.path.join(output_path, "grouped_cpu_timeseries.png"),
        )
        print(
            f"  Saved grouped plot: {os.path.join(output_path, 'grouped_cpu_timeseries.png')}"
        )

        # Raw Memory
        self.visualizer.plot_grouped_timeseries(
            series_list=_series(resource_series, "Memory_GB"),
            labels=labels,
            title="Memory Usage Over Time",
            xlabel="Time (seconds)",
            ylabel="Memory Usage (GB)",
            filename=os.path.join(output_path, "grouped_memory_timeseries.png"),
        )
        print(
            f"  Saved grouped plot: {os.path.join(output_path, 'grouped_memory_timeseries.png')}"
        )

        # Smoothed + raw CPU
        self.visualizer.plot_grouped_smoothed_timeseries(
            series_list=_series(resource_series, "CPU_Percent_Total"),
            labels=labels,
            title="CPU Usage Over Time (with Smoothed Curve)",
            xlabel="Time (seconds)",
            ylabel="CPU Usage (%)",
            filename=os.path.join(output_path, "grouped_cpu_normalized_smoothed.png"),
            include_raw=True,
        )
        print(
            f"  Saved grouped plot: {os.path.join(output_path, 'grouped_cpu_normalized_smoothed.png')}"
        )

        # Smoothed + raw Memory
        self.visualizer.plot_grouped_smoothed_timeseries(
            series_list=_series(resource_series, "Memory_GB"),
            labels=labels,
            title="Memory Usage Over Time (with Smoothed Curve)",
            xlabel="Time (seconds)",
            ylabel="Memory Usage (GB)",
            filename=os.path.join(
                output_path, "grouped_memory_normalized_smoothed.png"
            ),
            include_raw=True,
        )
        print(
            f"  Saved grouped plot: {os.path.join(output_path, 'grouped_memory_normalized_smoothed.png')}"
        )

        # Smoothed-only CPU
        self.visualizer.plot_grouped_smoothed_timeseries(
            series_list=_series(resource_series, "CPU_Percent_Total"),
            labels=labels,
            title="CPU Usage Over Time (Smoothed Only)",
            xlabel="Time (seconds)",
            ylabel="CPU Usage (%)",
            filename=os.path.join(output_path, "grouped_cpu_smoothed_only.png"),
            include_raw=False,
        )
        print(
            f"  Saved grouped plot: {os.path.join(output_path, 'grouped_cpu_smoothed_only.png')}"
        )

        # Smoothed-only Memory
        self.visualizer.plot_grouped_smoothed_timeseries(
            series_list=_series(resource_series, "Memory_GB"),
            labels=labels,
            title="Memory Usage Over Time (Smoothed Only)",
            xlabel="Time (seconds)",
            ylabel="Memory Usage (GB)",
            filename=os.path.join(output_path, "grouped_memory_smoothed_only.png"),
            include_raw=False,
        )
        print(
            f"  Saved grouped plot: {os.path.join(output_path, 'grouped_memory_smoothed_only.png')}"
        )

    def _plot_resource_timeseries(self):
        """Plot separate timeseries of CPU and Memory usage over time"""
        # Plot CPU usage over time
        self.visualizer.plot_timeseries(
            df=self.perf_resource,
            x_col="Time_Sec",
            y_cols=["CPU_Percent_Total"],
            title="CPU Usage Over Time",
            xlabel="Time (seconds)",
            ylabel="CPU Usage (%)",
            filename=os.path.join(self.output_dir, "performance_cpu_timeseries.png"),
        )

        # Plot Memory usage over time
        self.visualizer.plot_timeseries(
            df=self.perf_resource,
            x_col="Time_Sec",
            y_cols=["Memory_GB"],
            title="Memory Usage Over Time",
            xlabel="Time (seconds)",
            ylabel="Memory Usage (GB)",
            filename=os.path.join(self.output_dir, "performance_memory_timeseries.png"),
        )

        # Plot smoothed CPU usage
        self.visualizer.plot_normalized_smoothed_timeseries(
            df=self.perf_resource,
            x_col="Time_Sec",
            y_col="CPU_Percent_Total",
            title="CPU Usage Over Time (with Smoothed Curve)",
            xlabel="Time (seconds)",
            ylabel="CPU Usage (%)",
            filename=os.path.join(
                self.output_dir, "performance_cpu_normalized_smoothed.png"
            ),
        )

        # Plot smoothed Memory usage
        self.visualizer.plot_normalized_smoothed_timeseries(
            df=self.perf_resource,
            x_col="Time_Sec",
            y_col="Memory_GB",
            title="Memory Usage Over Time (with Smoothed Curve)",
            xlabel="Time (seconds)",
            ylabel="Memory Usage (GB)",
            filename=os.path.join(
                self.output_dir, "performance_memory_normalized_smoothed.png"
            ),
        )

        # Plot smoothed CPU only (without original data)
        self.visualizer.plot_smoothed_only_timeseries(
            df=self.perf_resource,
            x_col="Time_Sec",
            y_col="CPU_Percent_Total",
            title="CPU Usage Over Time (Smoothed Only)",
            xlabel="Time (seconds)",
            ylabel="CPU Usage (%)",
            filename=os.path.join(self.output_dir, "performance_cpu_smoothed_only.png"),
        )

        # Plot smoothed Memory only (without original data)
        self.visualizer.plot_smoothed_only_timeseries(
            df=self.perf_resource,
            x_col="Time_Sec",
            y_col="Memory_GB",
            title="Memory Usage Over Time (Smoothed Only)",
            xlabel="Time (seconds)",
            ylabel="Memory Usage (GB)",
            filename=os.path.join(
                self.output_dir, "performance_memory_smoothed_only.png"
            ),
        )
