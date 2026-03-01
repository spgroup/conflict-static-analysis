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
            print(f"  Timeouts (%) : {(timeout_count / len(self.perf_soot) * 100):.2f}%")

            if len(non_timeout_df) > 0:
                print("\n  Time Statistics (seconds):")
                print(f"  Average time (seconds): {non_timeout_df['Time'].mean():.2f}")
                print(f"  Median time (seconds): {non_timeout_df['Time'].median():.2f}")
                print(f"  Max time (seconds): {non_timeout_df['Time'].max():.2f}")
                print(f"  Min time (seconds): {non_timeout_df['Time'].min():.2f}")
                false_count = (self.perf_soot['OA Inter'] == 'false').sum()
                false_percentage = (false_count / len(self.perf_soot) * 100) if len(self.perf_soot) > 0 else 0

                print(f"  False results (%) : {false_percentage:.2f}%")
                print(f"  True results (%) : {100 - false_percentage:.2f}%")
                
                # Print percentiles
                print("\n  Time Percentiles:")
                percentiles = [50, 75, 90, 95, 99]
                for p in percentiles:
                    value = non_timeout_df['Time'].quantile(p / 100)
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
                filename=os.path.join(self.output_dir, "performance_time_histogram.png")
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
            timeout_value = max(max_time + 50, 400)  # Add 50 seconds above max or use 400
            time_data.extend([timeout_value] * timeout_count)
        
        
        if len(time_data) > 0:
            self.visualizer.plot_histogram(
                data=time_data,
                bins=35,
                title="Test Execution Time Distribution (Including Timeouts)",
                xlabel="Time (seconds)",
                filename=os.path.join(self.output_dir, "performance_time_histogram_with_timeouts.png")
            )

    def _plot_time_distribution_bar(self):
        """Plot bar chart showing distribution of scenarios across time groups"""
        if self.perf_soot is None:
            raise Exception("Soot performance data not loaded")
        
        non_timeout_df = self.perf_soot[self.perf_soot["OA Inter"] != "timeout"]
        
        if len(non_timeout_df) > 0:
            # Define time groups (in seconds)
            time_groups = {
                '1-5': (1, 5),
                '5-10': (5, 10),
                '10-30': (10, 30),
                '30-60': (30, 60),
                '60-120': (60, 120),
                '120+': (120, float('inf'))
            }
            
            # Count scenarios in each group
            group_counts = {}
            for group_name, (min_time, max_time) in time_groups.items():
                count = len(non_timeout_df[(non_timeout_df['Time'] >= min_time) & (non_timeout_df['Time'] < max_time)])
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
                    filename=os.path.join(self.output_dir, "performance_time_distribution_bar.png")
                )

    def _plot_time_distribution_bar_including_timeouts(self):
        """Plot bar chart showing distribution of scenarios across time groups including timeouts"""
        if self.perf_soot is None:
            raise Exception("Soot performance data not loaded")
        
        # Define time groups (in seconds) with updated 120-300 and timeout(>300)
        time_groups = {
            '0-5': (0, 5),
            '5-10': (5, 10),
            '10-30': (10, 30),
            '30-60': (30, 60),
            '60-120': (60, 120),
            '120-300': (120, 300),
            'timeouts': (300, float('inf'))
        }
        
        # Count scenarios in each group
        group_counts = {}
        non_timeout_df = self.perf_soot[self.perf_soot["OA Inter"] != "timeout"]
        timeout_df = self.perf_soot[self.perf_soot["OA Inter"] == "timeout"]
        for group_name, (min_time, max_time) in time_groups.items():
            if group_name == 'timeouts':
                # For timeouts, count the timeout entries marked as "timeout"
                count = len(timeout_df)
            else:
                count = len(non_timeout_df[(non_timeout_df['Time'] >= min_time) & (non_timeout_df['Time'] < max_time)])
            
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
                filename=os.path.join(self.output_dir, "performance_time_distribution_bar_with_timeouts.png")
            )


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
            filename=os.path.join(self.output_dir, "performance_cpu_timeseries.png")
        )
        
        # Plot Memory usage over time
        self.visualizer.plot_timeseries(
            df=self.perf_resource,
            x_col="Time_Sec",
            y_cols=["Memory_GB"],
            title="Memory Usage Over Time",
            xlabel="Time (seconds)",
            ylabel="Memory Usage (GB)",
            filename=os.path.join(self.output_dir, "performance_memory_timeseries.png")
        )
        
        # Plot smoothed CPU usage
        self.visualizer.plot_normalized_smoothed_timeseries(
            df=self.perf_resource,
            x_col="Time_Sec",
            y_col="CPU_Percent",
            title="CPU Usage Over Time (with Smoothed Curve)",
            xlabel="Time (seconds)",
            ylabel="CPU Usage (%)",
            filename=os.path.join(self.output_dir, "performance_cpu_normalized_smoothed.png")
        )
        
        # Plot smoothed Memory usage
        self.visualizer.plot_normalized_smoothed_timeseries(
            df=self.perf_resource,
            x_col="Time_Sec",
            y_col="Memory_GB",
            title="Memory Usage Over Time (with Smoothed Curve)",
            xlabel="Time (seconds)",
            ylabel="Memory Usage (GB)",
            filename=os.path.join(self.output_dir, "performance_memory_normalized_smoothed.png")
        )
        
        # Plot smoothed CPU only (without original data)
        self.visualizer.plot_smoothed_only_timeseries(
            df=self.perf_resource,
            x_col="Time_Sec",
            y_col="CPU_Percent",
            title="CPU Usage Over Time (Smoothed Only)",
            xlabel="Time (seconds)",
            ylabel="CPU Usage (%)",
            filename=os.path.join(self.output_dir, "performance_cpu_smoothed_only.png")
        )
        
        # Plot smoothed Memory only (without original data)
        self.visualizer.plot_smoothed_only_timeseries(
            df=self.perf_resource,
            x_col="Time_Sec",
            y_col="Memory_GB",
            title="Memory Usage Over Time (Smoothed Only)",
            xlabel="Time (seconds)",
            ylabel="Memory Usage (GB)",
            filename=os.path.join(self.output_dir, "performance_memory_smoothed_only.png")
        )


