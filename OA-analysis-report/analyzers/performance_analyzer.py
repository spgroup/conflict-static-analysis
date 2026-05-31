import json
import os
import re

import numpy as np
import pandas as pd

from visualization import Visualizer
from constants import (
    PERFORMANCE_SOOT_STATS_CSV,
    PERFORMANCE_SUMMARY_STATS_JSON,
    PERFORMANCE_RESOURCE_STATS_CSV,
    TIME_GROUPS,
    TIME_GROUPS_WITH_TIMEOUTS,
    CPU_BUCKETS,
    MEM_BUCKETS,
)


class PerformanceAnalyzer:
    def __init__(self):
        self.visualizer = Visualizer()
        self.output_dir = "."
        self.perf_summary = {}
        self.perf_soot = None
        self.perf_resource = None

    def analyze(self, plot=True, output_dir="."):
        self.output_dir = output_dir
        try:
            self._load_performance_data()
            self._print_performance_stats()
            if plot:
                self._create_plots()
        except Exception as e:
            print(f"Error analyzing performance data: {e}")

    # ------------------------------------------------------------------ data loading

    def _load_performance_data(self):
        summary_path = os.path.join(self.output_dir, PERFORMANCE_SUMMARY_STATS_JSON)
        if os.path.exists(summary_path):
            with open(summary_path, "r") as f:
                self.perf_summary = json.load(f)

        soot_path = os.path.join(self.output_dir, PERFORMANCE_SOOT_STATS_CSV)
        if os.path.exists(soot_path):
            self.perf_soot = pd.read_csv(soot_path, sep=";")

        resource_path = os.path.join(self.output_dir, PERFORMANCE_RESOURCE_STATS_CSV)
        if os.path.exists(resource_path):
            self.perf_resource = pd.read_csv(resource_path)

    # ------------------------------------------------------------------ stats printing

    def _print_performance_stats(self):
        print("\n" + "=" * 60)
        print("Performance Analysis Results")
        print("=" * 60)

        if self.perf_summary:
            print("\nPerformance Summary (Aggregated):")
            print(f"  Mode: {self.perf_summary.get('mode', 'N/A')}")
            print(f"  Callgraph: {self.perf_summary.get('callgraph', 'N/A')}")
            print(f"  Status: {self.perf_summary.get('status', 'N/A')}")
            print(f"  Duration (seconds): {self.perf_summary.get('duration_seconds', 'N/A'):.2f}")
            print(f"  Peak Memory (GB): {self.perf_summary.get('peak_memory_gb', 'N/A'):.4f}")
            print(f"  Peak CPU (%): {self.perf_summary.get('peak_cpu_percent', 'N/A'):.2f}")

        if self.perf_soot is not None:
            timeout_count = (self.perf_soot["OA Inter"] == "timeout").sum()
            non_timeout_df = self.perf_soot[self.perf_soot["OA Inter"] != "timeout"]

            print("\nSoot Results:")
            print(f"  Total scenarios: {len(self.perf_soot)}")
            print(f"  Timeouts: {timeout_count}")
            print(f"  Non-timeout scenarios: {len(non_timeout_df)}")
            print(f"  Timeouts (%) : {(timeout_count / len(self.perf_soot) * 100):.2f}%")

            if len(non_timeout_df) > 0:
                t = non_timeout_df["Time"]
                print("\n  Time Statistics (seconds):")
                print(f"  Average:         {t.mean():.2f}")
                print(f"  Median (50th):   {t.median():.2f}")
                for p in [75, 90, 95, 99]:
                    print(f"  {p}th percentile: {t.quantile(p / 100):.2f}")
                print(f"  Max:             {t.max():.2f}")
                print(f"  Min:             {t.min():.2f}")

                false_count = (self.perf_soot["OA Inter"] == "false").sum()
                false_pct = (false_count / len(self.perf_soot) * 100) if len(self.perf_soot) > 0 else 0
                print(f"\n  False results (%) : {false_pct:.2f}%")
                print(f"  True results (%)  : {100 - false_pct:.2f}%")

        if self.perf_resource is not None and len(self.perf_resource) > 0:
            print("\nResource Usage (Aggregated):")
            print(f"  Total records: {len(self.perf_resource)}")
            cpu = self.perf_resource["CPU_Percent_Total"]
            mem = self.perf_resource["Memory_GB"]
            print(f"  Average CPU (%): {cpu.mean():.2f}")
            print(f"  Median CPU (%): {cpu.median():.2f}")
            for p in [75, 90, 95, 99]:
                print(f"  {p}th percentile CPU (%): {cpu.quantile(p / 100):.2f}")
            print(f"  Max CPU (%): {cpu.max():.2f}")
            print(f"  Min CPU (%): {cpu.min():.2f}")
            print(f"  Average Memory (GB): {mem.mean():.4f}")
            print(f"  Median Memory (GB): {mem.median():.4f}")
            for p in [75, 90, 95, 99]:
                print(f"  {p}th percentile Memory (GB): {mem.quantile(p / 100):.4f}")
            print(f"  Max Memory (GB): {mem.max():.4f}")
            print(f"  Min Memory (GB): {mem.min():.4f}")
            dist = self._compute_resource_time_distribution(self.perf_resource)
            self._print_resource_time_distribution(dist, indent="  ")

        self._print_individual_run_stats()
        print("\n" + "=" * 60 + "\n")

    def _print_individual_run_stats(self):
        print("\nStatistics from 10 Individual Runs:")
        print("-" * 60)
        parent_dir = os.path.dirname(self.output_dir)
        results_folders = [
            (i, os.path.join(parent_dir, f"results{i}"))
            for i in range(1, 11)
            if os.path.isdir(os.path.join(parent_dir, f"results{i}"))
        ]

        if not results_folders:
            print("  No individual result folders found (results1-results10)")
            return

        print(f"  Found {len(results_folders)} result folders\n")
        self._analyze_results_file(results_folders, "soot-results.csv", sep=";", file_type="Soot Results")
        self._analyze_results_file(results_folders, "resource_usage_series.csv", sep=",", file_type="Resource Usage")

    def _analyze_results_file(self, results_folders, filename, sep=",", file_type=""):
        print(f"\n  {file_type} Analysis ({filename}):")
        print("  " + "-" * 56)
        dfs, valid_folders = [], []
        for run_num, results_path in results_folders:
            file_path = os.path.join(results_path, filename)
            if os.path.exists(file_path):
                try:
                    df = pd.read_csv(file_path, sep=sep)
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

        first_df = dfs[0]
        numeric_cols = [
            col for col in first_df.columns
            if pd.to_numeric(first_df[col], errors="coerce").notna().any()
        ]

        if not numeric_cols:
            print(f"  No numeric columns found in {filename}")
            return

        for col in numeric_cols:
            values = []
            for df in dfs:
                if col in df.columns:
                    df_filtered = df
                    if "soot-results.csv" in filename and "OA Inter" in df.columns:
                        df_filtered = df[df["OA Inter"] != "timeout"]
                    values.extend(pd.to_numeric(df_filtered[col], errors="coerce").dropna().tolist())

            if values:
                vs = pd.Series(values)
                mean = vs.mean()
                std_dev = vs.std()
                cv = (std_dev / mean * 100) if mean != 0 else 0
                print(f"    {col}:")
                print(f"      Mean: {mean:.6f}")
                print(f"      Std Dev: {std_dev:.6f}")
                print(f"      Variability (CV %): {cv:.2f}%")
            else:
                print(f"    {col}: No numeric data available")

    # ------------------------------------------------------------------ plots

    def _create_plots(self):
        if self.perf_soot is not None:
            self._plot_time_histogram()
            self._plot_time_histogram_including_timeouts()
            self._plot_time_distribution_bar()
            self._plot_time_distribution_bar_including_timeouts()

        if self.perf_resource is not None and len(self.perf_resource) > 0:
            self._plot_resource_timeseries()
            dist = self._compute_resource_time_distribution(self.perf_resource)
            self._plot_resource_usage_distribution(dist, self.output_dir)

    def _plot_time_histogram(self):
        if self.perf_soot is None:
            raise Exception("Soot performance data not loaded")
        non_timeout_df = self.perf_soot[self.perf_soot["OA Inter"] != "timeout"]
        if len(non_timeout_df) > 0:
            self.visualizer.plot_histogram(
                data=non_timeout_df["Time"], bins=30,
                title="Test Execution Time Distribution (Excluding Timeouts)",
                xlabel="Time (seconds)",
                filename=os.path.join(self.output_dir, "performance_time_histogram.png"),
            )

    def _plot_time_histogram_including_timeouts(self):
        if self.perf_soot is None:
            raise Exception("Soot performance data not loaded")
        non_timeout_df = self.perf_soot[self.perf_soot["OA Inter"] != "timeout"]
        timeout_count = (self.perf_soot["OA Inter"] == "timeout").sum()
        time_data = non_timeout_df["Time"].tolist() if len(non_timeout_df) > 0 else []
        if timeout_count > 0:
            timeout_value = max(max(time_data, default=0) + 50, 400)
            time_data.extend([timeout_value] * timeout_count)
        if time_data:
            self.visualizer.plot_histogram(
                data=time_data, bins=35,
                title="Test Execution Time Distribution (Including Timeouts)",
                xlabel="Time (seconds)",
                filename=os.path.join(self.output_dir, "performance_time_histogram_with_timeouts.png"),
            )

    def _plot_time_distribution_bar(self):
        if self.perf_soot is None:
            raise Exception("Soot performance data not loaded")
        non_timeout_df = self.perf_soot[self.perf_soot["OA Inter"] != "timeout"]
        if len(non_timeout_df) == 0:
            return
        group_counts = {
            name: len(non_timeout_df[(non_timeout_df["Time"] >= lo) & (non_timeout_df["Time"] < hi)])
            for name, (lo, hi) in TIME_GROUPS.items()
            if len(non_timeout_df[(non_timeout_df["Time"] >= lo) & (non_timeout_df["Time"] < hi)]) > 0
        }
        if group_counts:
            total = sum(group_counts.values())
            self.visualizer.plot_bar_chart(
                x=list(group_counts.keys()),
                y=[c / total * 100 for c in group_counts.values()],
                title="Scenario Distribution by Execution Time (Excluding Timeouts)",
                xlabel="Execution Time Range (seconds)",
                ylabel="Percentage (%)",
                filename=os.path.join(self.output_dir, "performance_time_distribution_bar.png"),
            )

    def _plot_time_distribution_bar_including_timeouts(self):
        if self.perf_soot is None:
            raise Exception("Soot performance data not loaded")
        non_timeout_df = self.perf_soot[self.perf_soot["OA Inter"] != "timeout"]
        timeout_count = (self.perf_soot["OA Inter"] == "timeout").sum()
        group_counts = {}
        for name, bounds in TIME_GROUPS_WITH_TIMEOUTS.items():
            if bounds is None:
                count = int(timeout_count)
            else:
                lo, hi = bounds
                count = len(non_timeout_df[(non_timeout_df["Time"] >= lo) & (non_timeout_df["Time"] < hi)])
            if count > 0:
                group_counts[name] = count
        if group_counts:
            total = sum(group_counts.values())
            print("Total groups counted (including timeouts):", total)
            self.visualizer.plot_bar_chart(
                x=list(group_counts.keys()),
                y=[c / total * 100 for c in group_counts.values()],
                title="Scenario Distribution by Execution Time (Including Timeouts)",
                xlabel="Execution Time Range (seconds)",
                ylabel="Percentage (%)",
                filename=os.path.join(self.output_dir, "performance_time_distribution_bar_with_timeouts.png"),
            )

    def _plot_resource_timeseries(self):
        for col, ylabel, title_suffix, filename_suffix in [
            ("CPU_Percent_Total", "CPU Usage (%)",     "CPU Usage Over Time",    "cpu"),
            ("Memory_GB",         "Memory Usage (GB)", "Memory Usage Over Time", "memory"),
        ]:
            self.visualizer.plot_timeseries(
                df=self.perf_resource, x_col="Time_Sec", y_cols=[col],
                title=title_suffix, xlabel="Time (seconds)", ylabel=ylabel,
                filename=os.path.join(self.output_dir, f"performance_{filename_suffix}_timeseries.png"),
            )
            self.visualizer.plot_normalized_smoothed_timeseries(
                df=self.perf_resource, x_col="Time_Sec", y_col=col,
                title=f"{title_suffix} (with Smoothed Curve)",
                xlabel="Time (seconds)", ylabel=ylabel,
                filename=os.path.join(self.output_dir, f"performance_{filename_suffix}_normalized_smoothed.png"),
            )
            self.visualizer.plot_smoothed_only_timeseries(
                df=self.perf_resource, x_col="Time_Sec", y_col=col,
                title=f"{title_suffix} (Smoothed)",
                xlabel="Time (seconds)", ylabel=ylabel,
                filename=os.path.join(self.output_dir, f"performance_{filename_suffix}_smoothed_only.png"),
            )

    def _plot_resource_usage_distribution(self, dist, output_dir):
        for key, title, xlabel, filename_suffix in [
            ("cpu",    "CPU Usage Distribution (% of Total Execution Time)",    "CPU Usage Range",    "cpu_usage_distribution"),
            ("memory", "Memory Usage Distribution (% of Total Execution Time)", "Memory Usage Range", "memory_usage_distribution"),
        ]:
            bucket_dist = dist.get(key, {})
            if bucket_dist:
                filename = os.path.join(output_dir, f"performance_{filename_suffix}.png")
                self.visualizer.plot_bar_chart(
                    x=list(bucket_dist.keys()), y=list(bucket_dist.values()),
                    title=title, xlabel=xlabel, ylabel="Percentage of Time (%)",
                    filename=filename,
                )
                print(f"  Saved plot: {filename}")

    def _plot_grouped_resource_usage_distribution(self, distributions, labels, output_path):
        valid_labels = [l for l in labels if l in distributions]
        if not valid_labels:
            return
        first = next(iter(distributions.values()))
        for key, filename_part, title, xlabel in [
            ("cpu",    "grouped_cpu_usage_distribution.png",    "CPU Usage Distribution (% of Total Execution Time)",    "CPU Usage Range"),
            ("memory", "grouped_memory_usage_distribution.png", "Memory Usage Distribution (% of Total Execution Time)", "Memory Usage Range"),
        ]:
            buckets = list(first[key].keys())
            datasets = [[distributions[l][key].get(b, 0) for b in buckets] for l in valid_labels]
            outfile = os.path.join(output_path, filename_part)
            self.visualizer.plot_grouped_bar_chart(
                x=buckets, datasets=datasets, labels=valid_labels,
                title=title, xlabel=xlabel, ylabel="Percentage of Time (%)",
                filename=outfile,
            )
            print(f"  Saved grouped plot: {outfile}")

    # ------------------------------------------------------------------ resource distribution

    @staticmethod
    def _compute_resource_time_distribution(res_df):
        """Return CPU and memory time-bucket percentages."""
        total = len(res_df)
        if total == 0:
            return {"cpu": {}, "memory": {}}

        cpu_dist = {
            name: ((res_df["CPU_Percent_Total"] >= lo) & (res_df["CPU_Percent_Total"] < hi)).sum() / total * 100
            for name, (lo, hi) in CPU_BUCKETS
        }
        mem_dist = {
            name: ((res_df["Memory_GB"] >= lo) & (res_df["Memory_GB"] < hi)).sum() / total * 100
            for name, (lo, hi) in MEM_BUCKETS
        }
        return {"cpu": cpu_dist, "memory": mem_dist}

    @staticmethod
    def _print_resource_time_distribution(dist, indent="  "):
        print(f"\n{indent}CPU usage distribution (% of total execution time):")
        for bucket, pct in dist.get("cpu", {}).items():
            print(f"{indent}  {bucket}: {pct:.1f}%")
        print(f"\n{indent}Memory usage distribution (% of total execution time):")
        for bucket, pct in dist.get("memory", {}).items():
            print(f"{indent}  {bucket}: {pct:.1f}%")

    # ------------------------------------------------------------------ path helpers

    @staticmethod
    def _label_from_path(path):
        """Extract human-readable label, e.g. 'depth5' from 'analysisReport/depth5CHA/...'."""
        parts = path.replace("\\", "/").split("/")
        for part in parts:
            m = re.match(r"(depth\d+)([a-zA-Z]+)", part, re.IGNORECASE)
            if m:
                return m.group(1)
        return next((p for p in reversed(parts) if p), path)

    # ------------------------------------------------------------------ subset analysis

    def analyze_subset_non_timeouts(self, paths):
        """Find scenarios with no timeout across all paths and produce grouped plots."""
        ID_COLS = ["project", "class", "method", "merge commit"]

        print("\n" + "=" * 60)
        print("Subset Non-Timeouts Analysis")
        print("=" * 60)

        path_dfs = {}
        for path in paths:
            csv_path = os.path.join(path, PERFORMANCE_SOOT_STATS_CSV)
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

        resource_series = {}
        for path in path_dfs:
            label = self._label_from_path(path)
            res_path = os.path.join(path, PERFORMANCE_RESOURCE_STATS_CSV)
            if os.path.exists(res_path):
                res_df = pd.read_csv(res_path)
                if not res_df.empty and "Time_Sec" in res_df.columns:
                    resource_series[label] = res_df

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
        labels = []
        subset_dfs, all_dfs = {}, {}
        previous_avg = previous_worst10 = previous_total_time = None
        previous_mean_cpu = previous_mean_memory = None
        total_times, mean_times, worst_10_times = {}, {}, {}
        resource_distributions = {}

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
            w10_avg = nt[nt["Time"] >= w10_threshold]["Time"].mean() if len(nt) > 0 else 0

            print(f"\n  Dataset: {label}  (path: {path})")
            print(f"    Subset size: {len(subset_df)} scenarios")
            print(f"    Average time: {avg:.2f}s", end="")
            if previous_avg is not None:
                pct = (avg - previous_avg) / previous_avg * 100
                print(f"  ({'+' if pct >= 0 else ''}{pct:.1f}%)")
            else:
                print()
            if len(nt) > 0:
                t = nt["Time"]
                print(f"    Median time (50th): {t.median():.2f}s")
                for p in [75, 90, 95, 99]:
                    print(f"    {p}th percentile:    {t.quantile(p / 100):.2f}s")
            print(f"    Worst 10% avg time: {w10_avg:.2f}s", end="")
            if previous_worst10 is not None:
                pct = (w10_avg - previous_worst10) / previous_worst10 * 100
                print(f"  ({'+' if pct >= 0 else ''}{pct:.1f}%)")
            else:
                print()
            worst_10_times[label] = w10_avg

            if label in resource_series:
                res_df = resource_series[label]
                if not res_df.empty:
                    total_time = res_df["Time_Sec"].max() if "Time_Sec" in res_df.columns else 0
                    cpu_series = res_df["CPU_Percent_Total"] if "CPU_Percent_Total" in res_df.columns else pd.Series(dtype=float)
                    mem_series = res_df["Memory_GB"] if "Memory_GB" in res_df.columns else pd.Series(dtype=float)
                    mean_cpu = cpu_series.mean() if not cpu_series.empty else 0
                    mean_memory = mem_series.mean() if not mem_series.empty else 0

                    print(f"    Total execution time: {total_time:.2f}s", end="")
                    if previous_total_time is not None:
                        pct = (total_time - previous_total_time) / previous_total_time * 100
                        print(f"  ({'+' if pct >= 0 else ''}{pct:.1f}%)")
                    else:
                        print()
                    print(f"    Mean execution time: {avg:.2f}s")

                    print(f"    Mean CPU: {mean_cpu:.2f}%", end="")
                    if previous_mean_cpu is not None:
                        pct = (mean_cpu - previous_mean_cpu) / previous_mean_cpu * 100
                        print(f"  ({'+' if pct >= 0 else ''}{pct:.1f}%)")
                    else:
                        print()
                    if not cpu_series.empty:
                        print(f"    Median CPU: {cpu_series.median():.2f}%")
                        for p in [75, 90, 95, 99]:
                            print(f"    {p}th percentile CPU: {cpu_series.quantile(p / 100):.2f}%")

                    print(f"    Mean Memory: {mean_memory:.4f}GB", end="")
                    if previous_mean_memory is not None:
                        pct = (mean_memory - previous_mean_memory) / previous_mean_memory * 100
                        print(f"  ({'+' if pct >= 0 else ''}{pct:.1f}%)")
                    else:
                        print()
                    if not mem_series.empty:
                        print(f"    Median Memory: {mem_series.median():.4f}GB")
                        for p in [75, 90, 95, 99]:
                            print(f"    {p}th percentile Memory: {mem_series.quantile(p / 100):.4f}GB")
                    dist = self._compute_resource_time_distribution(res_df)
                    self._print_resource_time_distribution(dist, indent="    ")
                    resource_distributions[label] = dist
                    total_times[label] = total_time
                    mean_times[label] = avg
                    previous_total_time = total_time
                    previous_mean_cpu = mean_cpu
                    previous_mean_memory = mean_memory

            previous_avg = avg
            previous_worst10 = w10_avg

        self._plot_grouped_time_distribution(subset_dfs, labels, output_path)
        self._plot_grouped_time_distribution_with_timeouts(all_dfs, labels, output_path)
        if resource_series:
            self._plot_grouped_resource_timeseries(resource_series, labels, output_path)
        if total_times and labels:
            self._plot_horizontal_times("total_duration_horizontal.png", total_times, labels,
                                        "Dataset Execution Time by Depth", output_path)
        if mean_times and labels:
            self._plot_horizontal_times("mean_execution_time_horizontal.png", mean_times, labels,
                                        "Mean Scenario Execution Time by Depth", output_path)
        if worst_10_times and labels:
            self._plot_horizontal_times("worst_10_percent_time_horizontal.png", worst_10_times, labels,
                                        "Worst 10% Scenario Execution Time by Depth", output_path)
        if resource_distributions:
            self._plot_grouped_resource_usage_distribution(resource_distributions, labels, output_path)

        print("\n" + "=" * 60 + "\n")

    def _plot_grouped_time_distribution(self, subset_dfs, labels, output_path):
        x_labels = list(TIME_GROUPS.keys())
        datasets = []
        for label in labels:
            nt = subset_dfs[label][subset_dfs[label]["OA Inter"] != "timeout"]
            counts = [
                len(nt[(nt["Time"] >= lo) & (nt["Time"] < hi)])
                for lo, hi in TIME_GROUPS.values()
            ]
            total = sum(counts)
            datasets.append([c / total * 100 if total > 0 else 0 for c in counts])
        filename = os.path.join(output_path, "subset_non_timeouts_time_distribution_grouped.png")
        self.visualizer.plot_grouped_bar_chart(
            x=x_labels, datasets=datasets, labels=labels,
            title="Scenario Time Distribution – Common Non-Timeout Subset",
            xlabel="Execution Time Range (seconds)", ylabel="Percentage (%)",
            filename=filename,
        )
        print(f"\n  Saved grouped plot: {filename}")

    def _plot_grouped_time_distribution_with_timeouts(self, all_dfs, labels, output_path):
        x_labels = list(TIME_GROUPS_WITH_TIMEOUTS.keys())
        datasets = []
        for label in labels:
            df = all_dfs[label]
            nt = df[df["OA Inter"] != "timeout"]
            timeout_count = (df["OA Inter"] == "timeout").sum()
            counts = []
            for group_name, bounds in TIME_GROUPS_WITH_TIMEOUTS.items():
                if bounds is None:
                    counts.append(int(timeout_count))
                else:
                    lo, hi = bounds
                    counts.append(len(nt[(nt["Time"] >= lo) & (nt["Time"] < hi)]))
            total = sum(counts)
            datasets.append([c / total * 100 if total > 0 else 0 for c in counts])
        filename = os.path.join(output_path, "all_scenarios_time_distribution_with_timeouts_grouped.png")
        self.visualizer.plot_grouped_bar_chart(
            x=x_labels, datasets=datasets, labels=labels,
            title="Scenario Distribution by Execution Time (Including Timeouts) – All Scenarios",
            xlabel="Execution Time Range (seconds)", ylabel="Percentage (%)",
            filename=filename,
        )
        print(f"  Saved grouped plot: {filename}")

    def _plot_grouped_resource_timeseries(self, resource_series, labels, output_path):
        def _series(col):
            return [
                (resource_series[lbl]["Time_Sec"].values, resource_series[lbl][col].values)
                if lbl in resource_series and col in resource_series[lbl].columns
                else (np.array([]), np.array([]))
                for lbl in labels
            ]

        configs = [
            ("CPU_Percent_Total", "CPU Usage (%)",     "CPU Usage Over Time",    "cpu"),
            ("Memory_GB",         "Memory Usage (GB)", "Memory Usage Over Time", "memory"),
        ]
        for col, ylabel, title, key in configs:
            series_data = _series(col)
            for variant, include_raw, suffix in [
                ("raw",          None,  f"grouped_{key}_timeseries.png"),
                ("smoothed_raw", True,  f"grouped_{key}_normalized_smoothed.png"),
                ("smoothed",     False, f"grouped_{key}_smoothed_only.png"),
            ]:
                outfile = os.path.join(output_path, suffix)
                if variant == "raw":
                    self.visualizer.plot_grouped_timeseries(
                        series_list=series_data, labels=labels,
                        title=title, xlabel="Time (seconds)", ylabel=ylabel,
                        filename=outfile,
                    )
                else:
                    title_variant = f"{title} ({'with Smoothed Curve' if include_raw else 'Smoothed'})"
                    self.visualizer.plot_grouped_smoothed_timeseries(
                        series_list=series_data, labels=labels,
                        title=title_variant, xlabel="Time (seconds)", ylabel=ylabel,
                        filename=outfile, include_raw=include_raw,
                    )
                print(f"  Saved grouped plot: {outfile}")

    def _plot_horizontal_times(self, filename_part, times_dict, labels, title, output_path):
        sorted_labels = [l for l in labels if l in times_dict]
        sorted_values = [times_dict[l] for l in sorted_labels]
        filename = os.path.join(output_path, filename_part)
        self.visualizer.plot_horizontal_bar_chart(
            labels=sorted_labels, values=sorted_values,
            title=title, xlabel="Time (seconds)", ylabel="Depth",
            filename=filename,
        )
        print(f"  Saved horizontal bar plot: {filename}")
