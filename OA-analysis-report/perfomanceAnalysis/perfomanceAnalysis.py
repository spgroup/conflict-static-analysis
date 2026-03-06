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
            print(f"  Average CPU (%): {self.perf_resource['CPU_Percent'].mean():.2f}")
            print(f"  Max CPU (%): {self.perf_resource['CPU_Percent'].max():.2f}")
            print(f"  Min CPU (%): {self.perf_resource['CPU_Percent'].min():.2f}")
            print(
                f"  Average Memory (GB): {self.perf_resource['Memory_GB'].mean():.4f}"
            )
            print(f"  Max Memory (GB): {self.perf_resource['Memory_GB'].max():.4f}")
            print(f"  Min Memory (GB): {self.perf_resource['Memory_GB'].min():.4f}")

        print("\n" + "=" * 60 + "\n")

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

    def analyze_subset_non_timeouts(self, paths):
        """
        Given a list of performance report directory paths, find the subset of
        scenarios (identified by project, class, method, merge commit) that had
        NO timeout in ANY of the provided paths, then plot _plot_time_distribution_bar
        for each path using only those scenarios.

        Each path should point to a directory that contains
        performance_soot_results_stats.csv.
        """
        SOOT_CSV = PERFORMANCE_SOOT_STATS_CSV
        ID_COLS = ["project", "class", "method", "merge commit"]

        print("\n" + "=" * 60)
        print("Subset Non-Timeouts Analysis")
        print("=" * 60)

        # --- 1. Load all CSVs and resolve common non-timeout scenario keys ---
        path_dfs = {}
        for path in paths:
            csv_path = os.path.join(path, SOOT_CSV)
            if not os.path.exists(csv_path):
                print(f"  Warning: File not found, skipping path: {csv_path}")
                continue
            df = pd.read_csv(csv_path, sep=";")
            # Verify required columns exist
            missing = [c for c in ID_COLS + ["OA Inter", "Time"] if c not in df.columns]
            if missing:
                print(f"  Warning: Missing columns {missing} in {csv_path}, skipping.")
                continue
            path_dfs[path] = df
            total = len(df)
            timeouts = (df["OA Inter"] == "timeout").sum()
            print(f"  Loaded {csv_path}: {total} scenarios, {timeouts} timeouts")

        if len(path_dfs) < 1:
            print("  No valid paths found. Aborting subset analysis.")
            return

        # Build sets of non-timeout scenario keys for each path
        non_timeout_key_sets = []
        for path, df in path_dfs.items():
            non_timeout_df = df[df["OA Inter"] != "timeout"]
            keys = set(zip(*[non_timeout_df[c].astype(str) for c in ID_COLS]))
            non_timeout_key_sets.append(keys)

        # Intersection: scenarios with no timeout in ALL paths
        common_non_timeout_keys = non_timeout_key_sets[0]
        for s in non_timeout_key_sets[1:]:
            common_non_timeout_keys = common_non_timeout_keys & s

        print(
            f"\n  Common non-timeout scenarios across all paths: {len(common_non_timeout_keys)}"
        )

        if len(common_non_timeout_keys) == 0:
            print("  No common non-timeout scenarios found. Nothing to plot.")
            return

        # --- 2. For each path, filter to subset and plot time distribution bar ---
        previous_avg_time = None
        previous_worst_10_avg_time = None
        for path, df in path_dfs.items():
            # Build a boolean mask for rows that belong to the common subset
            key_col = list(zip(*[df[c].astype(str) for c in ID_COLS]))
            mask = pd.Series(
                [k in common_non_timeout_keys for k in key_col], index=df.index
            )
            subset_df = df[mask].copy()

            # Calculate average time for the subset
            subset_df_non_timeout = subset_df[subset_df["OA Inter"] != "timeout"]
            avg_time = subset_df_non_timeout["Time"].mean() if len(subset_df_non_timeout) > 0 else 0
            
            # Calculate worst 10% average time
            worst_10_threshold = subset_df_non_timeout["Time"].quantile(0.9) if len(subset_df_non_timeout) > 0 else 0
            worst_10_df = subset_df_non_timeout[subset_df_non_timeout["Time"] >= worst_10_threshold]
            worst_10_avg_time = worst_10_df["Time"].mean() if len(worst_10_df) > 0 else 0

            print(f"\n  Plotting for path: {path}")
            print(f"    Subset size: {len(subset_df)} scenarios")
            print(f"    Average time: {avg_time:.2f} seconds", end="")
            
            # Calculate and print percentage difference from previous dataset
            if previous_avg_time is not None:
                pct_diff = ((avg_time - previous_avg_time) / previous_avg_time) * 100
                sign = "+" if pct_diff >= 0 else ""
                print(f" ({sign}{pct_diff:.1f}%)")
            else:
                print()
            
            print(f"    Worst 10% average time: {worst_10_avg_time:.2f} seconds", end="")
            
            # Calculate and print percentage difference from previous dataset's worst 10%
            if previous_worst_10_avg_time is not None:
                pct_diff_worst = ((worst_10_avg_time - previous_worst_10_avg_time) / previous_worst_10_avg_time) * 100
                sign = "+" if pct_diff_worst >= 0 else ""
                print(f" ({sign}{pct_diff_worst:.1f}%)")
            else:
                print()

            # Use a sanitized folder name derived from the path for output filenames
            safe_name = path.replace("/", "_").replace("\\", "_").strip("_")

            # Temporarily override output_dir and perf_soot, then call the plot method
            original_output_dir = self.output_dir
            original_perf_soot = self.perf_soot

            self.output_dir = path
            self.perf_soot = subset_df

            self._plot_time_distribution_bar_subset(safe_name)

            # Restore
            self.output_dir = original_output_dir
            self.perf_soot = original_perf_soot
            
            # Update previous average times for next iteration
            previous_avg_time = avg_time
            previous_worst_10_avg_time = worst_10_avg_time

        print("\n" + "=" * 60 + "\n")

    def _plot_time_distribution_bar_subset(self, label):
        """
        Plot bar chart of time distribution for the current perf_soot (already
        filtered to non-timeout subset). Output filename includes the given label.
        """
        if self.perf_soot is None or len(self.perf_soot) == 0:
            return

        # All rows are already non-timeout (subset was pre-filtered)
        non_timeout_df = self.perf_soot[self.perf_soot["OA Inter"] != "timeout"]

        if len(non_timeout_df) == 0:
            return

        time_groups = {
            "0-5": (0, 5),
            "5-10": (5, 10),
            "10-30": (10, 30),
            "30-60": (30, 60),
            "60-120": (60, 120),
            "120+": (120, float("inf")),
        }

        group_counts = {}
        for group_name, (min_time, max_time) in time_groups.items():
            count = len(
                non_timeout_df[
                    (non_timeout_df["Time"] >= min_time)
                    & (non_timeout_df["Time"] < max_time)
                ]
            )
            group_counts[group_name] = count

        total = sum(group_counts.values())
        if total == 0:
            return
        x_labels = list(group_counts.keys())
        y_percentages = [count / total * 100 for count in group_counts.values()]

        filename = os.path.join(
            self.output_dir,
            f"subset_non_timeouts_time_distribution_{label}.png",
        )

        self.visualizer.plot_bar_chart(
            x=x_labels,
            y=y_percentages,
            title="Scenario Time Distribution – Common Non-Timeout Subset",
            xlabel="Execution Time Range (seconds)",
            ylabel="Percentage (%)",
            filename=filename,
        )
        print(f"    Saved plot: {filename}")

    def _plot_resource_timeseries(self):
        """Plot separate timeseries of CPU and Memory usage over time"""
        # Plot CPU usage over time
        self.visualizer.plot_timeseries(
            df=self.perf_resource,
            x_col="Time_Sec",
            y_cols=["CPU_Percent"],
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
            y_col="CPU_Percent",
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
            y_col="CPU_Percent",
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
