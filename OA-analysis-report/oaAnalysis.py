import json
import csv
import sys
import os
import pandas as pd
from collections import defaultdict, Counter
from constants import *
from conflictAnalysis.conflictAnalysis import ConflictAnalyzer
from scenarioAnalysis.scenarioAnalysis import ScenarioAnalyzer
from perfomanceAnalysis.perfomanceAnalysis import PerformanceAnalyzer


class ConflictProcessor:
    def __init__(self, output_dir="."):
        self.output_dir = output_dir
        self.conflict_data = {
            "l_lengths": defaultdict(list),
            "r_lengths": defaultdict(list),
            "same_class": defaultdict(list),
            "same_method": defaultdict(list),
            "start_same_class": defaultdict(list),
            "start_same_method": defaultdict(list),
            "diffs": defaultdict(list),
            "depth": defaultdict(list),
            "jar_map": defaultdict(set),
            "rows": [],
            "conflict_idx": 0,
            "same_depth_count": 0,
        }

    def process_conflicts(self, conflicts, start_idx=0):
        for entry in conflicts:
            if (
                not isinstance(entry, dict)
                or "body" not in entry
                or "interference" not in entry["body"]
            ):
                raise Exception("Invalid entry format", entry)

            scenario_jar = entry.get("ScenarioJAR")
            self.conflict_data["jar_map"][start_idx].add(scenario_jar)

            interference = entry["body"]["interference"]
            if not isinstance(interference, list) or len(interference) < 2:
                raise Exception("Invalid interference format", entry)

            self._process_interference(interference, scenario_jar, start_idx)

    def _process_interference(self, interference, scenario_jar, start_idx):
        l_stack = interference[0].get("stackTrace", [])
        r_stack = interference[1].get("stackTrace", [])
        l_len, r_len = len(l_stack), len(r_stack)
        diff = abs(l_len - r_len)
        max_len = max(l_len, r_len)

        l_class = interference[0].get("location", {}).get("class")
        r_class = interference[1].get("location", {}).get("class")
        l_method = interference[0].get("location", {}).get("method")
        r_method = interference[1].get("location", {}).get("method")

        same_class = l_class == r_class and l_class is not None
        same_method = same_class and l_method == r_method and l_method is not None

        l_start_class = l_stack[0].get("class") if l_stack else None
        r_start_class = r_stack[0].get("class") if r_stack else None
        same_start_class = l_start_class == r_start_class and l_start_class is not None

        l_start_method = l_stack[0].get("method") if l_stack else None
        r_start_method = r_stack[0].get("method") if r_stack else None
        same_start_method = (
            l_start_method == r_start_method and l_start_method is not None
        )

        if l_len == r_len:
            self.conflict_data["same_depth_count"] += 1

        self._store_metrics(
            l_len,
            r_len,
            diff,
            max_len,
            same_class,
            same_method,
            same_start_class,
            same_start_method,
            scenario_jar,
            start_idx,
        )

    def _store_metrics(
        self,
        l_len,
        r_len,
        diff,
        max_len,
        same_class,
        same_method,
        same_start_class,
        same_start_method,
        scenario_jar,
        start_idx,
    ):
        data = self.conflict_data
        data["l_lengths"][start_idx].append(l_len)
        data["r_lengths"][start_idx].append(r_len)
        data["diffs"][start_idx].append(diff)
        data["depth"][start_idx].append(max_len)
        data["same_class"][start_idx].append(same_class)
        data["same_method"][start_idx].append(same_method)
        data["start_same_class"][start_idx].append(same_start_class)
        data["start_same_method"][start_idx].append(same_start_method)

        data["rows"].append(
            [
                data["conflict_idx"],
                l_len,
                r_len,
                max_len,
                diff,
                same_class,
                same_method,
                same_start_class,
                same_start_method,
                scenario_jar,
                start_idx,
            ]
        )
        data["conflict_idx"] += 1

    def save_results(self):
        conflict_stats_path = os.path.join(self.output_dir, CONFLICT_STATS_CSV)
        with open(conflict_stats_path, "w", newline="") as csvfile:
            writer = csv.writer(csvfile)
            writer.writerow(
                [
                    COL_CONFLICT_INDEX,
                    COL_LEFT_LENGTH,
                    COL_RIGHT_LENGTH,
                    COL_DEPTH,
                    COL_DIFF,
                    COL_SAME_CLASS,
                    COL_SAME_METHOD,
                    COL_SAME_START_CLASS,
                    COL_SAME_START_METHOD,
                    COL_SCENARIO_JAR,
                    COL_SCENARIO_INDEX,
                ]
            )
            writer.writerows(self.conflict_data["rows"])

        scenario_jar_list = {
            idx: list(self.conflict_data["jar_map"][idx])[0]
            for idx in self.conflict_data["jar_map"]
        }
        jar_conflict_counts = defaultdict(int)
        jar_scenario_counts = defaultdict(int)

        for idx, jar in scenario_jar_list.items():
            jar_conflict_counts[jar] += len(self.conflict_data["depth"][idx])
            jar_scenario_counts[jar] += 1

        scenario_stats_path = os.path.join(self.output_dir, SCENARIO_STATS_CSV)
        with open(scenario_stats_path, "w", newline="") as csvfile:
            writer = csv.writer(csvfile)
            writer.writerow([COL_SCENARIO_JAR, COL_NUM_CONFLICTS, COL_NUM_SCENARIOS])
            for jar in jar_conflict_counts:
                writer.writerow(
                    [jar, jar_conflict_counts[jar], jar_scenario_counts[jar]]
                )


class PerformanceAggregator:
    def __init__(self, performance_data_path):
        self.performance_data_path = performance_data_path
        self.report_dir = os.path.join(performance_data_path, PERFORMANCE_REPORT_DIR)
        os.makedirs(self.report_dir, exist_ok=True)

    def aggregate(self) -> str:
        """Aggregate all results from individual runs"""
        self._aggregate_soot_results()
        self._aggregate_performance_summary()
        self._aggregate_resource_usage()

        return self.report_dir

    def _aggregate_soot_results(self):
        """Aggregate soot-results.csv files with mean for Time and majority vote for OA Inter"""
        all_data = []

        # Read all soot-results.csv files from results1 to results10
        for i in range(1, 11):
            results_dir = os.path.join(self.performance_data_path, f"results{i}")
            soot_file = os.path.join(results_dir, "soot-results.csv")

            if os.path.exists(soot_file):
                df = pd.read_csv(soot_file, sep=";")
                df["result_num"] = i
                all_data.append(df)

        if not all_data:
            print(
                f"Warning: No soot-results.csv files found in {self.performance_data_path}"
            )
            return

        combined_df = pd.concat(all_data, ignore_index=True)

        # Group by project, class, method, and merge commit
        groupby_cols = ["project", "class", "method", "merge commit"]

        # Aggregate Time by mean
        agg_dict = {"Time": "mean"}
        aggregated = combined_df.groupby(groupby_cols).agg(agg_dict).reset_index()

        # For OA Inter, take majority vote
        oa_inter_groups = (
            combined_df.groupby(groupby_cols)["OA Inter"]
            .apply(lambda x: Counter(x).most_common(1)[0][0])
            .reset_index(name="OA Inter")
        )

        # Merge the results
        result_df = aggregated.merge(oa_inter_groups, on=groupby_cols)

        # Reorder columns to match original format
        result_df = result_df[groupby_cols + ["OA Inter", "Time"]]

        # Save to CSV with semicolon separator
        output_path = os.path.join(self.report_dir, PERFORMANCE_SOOT_STATS_CSV)
        result_df.to_csv(output_path, sep=";", index=False)
        print(f"Saved aggregated soot results to {output_path}")

    def _aggregate_performance_summary(self):
        """Aggregate performance_summary.json files by mean of numeric fields"""
        all_summaries = []

        # Read all performance_summary.json files from results1 to results10
        for i in range(1, 11):
            results_dir = os.path.join(self.performance_data_path, f"results{i}")
            summary_file = os.path.join(results_dir, "performance_summary.json")

            if os.path.exists(summary_file):
                with open(summary_file, "r") as f:
                    data = json.load(f)
                    all_summaries.append(data)

        if not all_summaries:
            print(
                f"Warning: No performance_summary.json files found in {self.performance_data_path}"
            )
            return

        # Convert to DataFrame for easier aggregation
        df = pd.json_normalize(all_summaries)

        # Aggregate numeric fields by mean
        numeric_cols = ["duration_seconds", "peak_memory_gb", "peak_cpu_percent"]
        aggregated = {}

        for col in numeric_cols:
            if col in df.columns:
                aggregated[col] = df[col].mean()

        # Keep non-numeric fields from the first record
        for key in ["mode", "callgraph", "status"]:
            if key in all_summaries[0]:
                aggregated[key] = all_summaries[0][key]

        # Save to JSON
        output_path = os.path.join(self.report_dir, PERFORMANCE_SUMMARY_STATS_JSON)
        with open(output_path, "w") as f:
            json.dump(aggregated, f, indent=2)
        print(f"Saved aggregated performance summary to {output_path}")

    def _aggregate_resource_usage(self):
        """Aggregate resource_usage_series.csv files by mean per time second"""
        all_data = []

        # Read all resource_usage_series.csv files from results1 to results10
        for i in range(1, 11):
            results_dir = os.path.join(self.performance_data_path, f"results{i}")
            resource_file = os.path.join(results_dir, "resource_usage_series.csv")

            if os.path.exists(resource_file):
                df = pd.read_csv(resource_file)

                # Skip empty DataFrames (only headers, no data)
                if df.empty:
                    continue

                # Convert Time_Sec to numeric and then round to integer for alignment across results
                if "Time_Sec" in df.columns:
                    df["Time_Sec"] = (
                        pd.to_numeric(df["Time_Sec"], errors="coerce")
                        .round(0)
                        .astype(int)
                    )

                df["result_num"] = i
                all_data.append(df)

        if not all_data:
            print(
                f"Warning: No resource_usage_series.csv files with data found in {self.performance_data_path}"
            )
            return

        # Find the maximum time_sec (length of longest series)
        max_time_sec = max(df["Time_Sec"].max() for df in all_data)

        # Aggregate by time_sec: for each second, compute mean of CPU and Memory across all results
        aggregated_data = []
        for time_sec in range(int(max_time_sec) + 1):
            row_data: dict = {"Time_Sec": time_sec}

            # Collect values for this time_sec from all results
            cpu_values = []
            memory_values = []

            for df in all_data:
                time_rows = df[df["Time_Sec"] == time_sec]
                if not time_rows.empty:
                    if "CPU_Percent" in df.columns:
                        cpu_values.append(time_rows["CPU_Percent"].iloc[0])
                    if "Memory_GB" in df.columns:
                        memory_values.append(time_rows["Memory_GB"].iloc[0])

            # Compute mean for this time_sec
            if cpu_values:
                row_data["CPU_Percent"] = sum(cpu_values) / len(cpu_values)
            if memory_values:
                row_data["Memory_GB"] = sum(memory_values) / len(memory_values)

            aggregated_data.append(row_data)

        # Convert to DataFrame and save
        result_df = pd.DataFrame(aggregated_data)

        # Save to CSV
        output_path = os.path.join(self.report_dir, PERFORMANCE_RESOURCE_STATS_CSV)
        result_df.to_csv(output_path, index=False)
        print(f"Saved aggregated resource usage to {output_path}")


def parse_args():
    plot_enabled = False
    input_json = None
    input_json2 = None
    labels = None
    performance_data_path = None

    for arg in sys.argv[1:]:
        if arg.lower().startswith("plot="):
            plot_value = arg.split("=")[1].lower()
            plot_enabled = plot_value == "true"
        elif arg.lower().startswith("out.json="):
            input_json = arg.split("=", 1)[1]
        elif arg.lower().startswith("out.json2="):
            input_json2 = arg.split("=", 1)[1]
        elif arg.lower().startswith("labels="):
            labels = arg.split("=", 1)[1]
        elif arg.lower().startswith("performancedata="):
            performance_data_path = arg.split("=", 1)[1]

    return plot_enabled, input_json, input_json2, labels, performance_data_path


def main():
    plot_enabled, input_json, input_json2, labels, performance_data_path = parse_args()

    # Validate that at least one input is provided
    if not input_json and not performance_data_path:
        print("Error: Either out.json or performancedata parameter must be provided")
        sys.exit(1)

    # Parse labels if provided
    label1, label2 = None, None
    if labels and "," in labels:
        parts = labels.split(",")
        label1 = parts[0].strip()
        label2 = parts[1].strip() if len(parts) > 1 else None

    output_dir = None
    output_dir2 = ""
    processor1 = None
    processor2 = None

    # Process conflict data if JSON input is provided
    if input_json:
        output_dir = os.path.dirname(os.path.abspath(input_json))
        if not output_dir:
            output_dir = "."

        # Process first JSON file
        processor1 = ConflictProcessor(output_dir)

        with open(input_json) as f:
            data = json.load(f)

        if data and isinstance(data, list):
            for idx, entry in enumerate(data):
                if isinstance(entry, dict) and "conflicts" in entry:
                    conflicts = entry.get("conflicts", [])
                    processor1.process_conflicts(conflicts, start_idx=idx)
                else:
                    raise Exception("Invalid conflict data format")

        for idx in processor1.conflict_data["jar_map"]:
            if len(processor1.conflict_data["jar_map"][idx]) > 1:
                raise Exception(
                    "Multiple scenario jars found",
                    processor1.conflict_data["jar_map"][idx],
                    idx,
                )

        processor1.save_results()

        # Process second JSON file if provided
        if input_json2:
            output_dir2 = os.path.dirname(os.path.abspath(input_json2))
            if not output_dir2:
                output_dir2 = "."

            processor2 = ConflictProcessor(output_dir2)

            with open(input_json2) as f:
                data = json.load(f)

            if data and isinstance(data, list):
                for idx, entry in enumerate(data):
                    if isinstance(entry, dict) and "conflicts" in entry:
                        conflicts = entry.get("conflicts", [])
                        processor2.process_conflicts(conflicts, start_idx=idx)
                    else:
                        raise Exception("Invalid conflict data format")

            for idx in processor2.conflict_data["jar_map"]:
                if len(processor2.conflict_data["jar_map"][idx]) > 1:
                    raise Exception(
                        "Multiple scenario jars found",
                        processor2.conflict_data["jar_map"][idx],
                        idx,
                    )

            processor2.save_results()

    # Aggregate performance data if path is provided
    perfomance_report_dir = None
    if performance_data_path:
        perf_aggregator = PerformanceAggregator(performance_data_path)
        perfomance_report_dir = perf_aggregator.aggregate()

    # Run analyzers if data is available
    if processor1:
        conflict_analyzer = ConflictAnalyzer()
        scenario_analyzer = ScenarioAnalyzer()

        if processor2:
            # Comparison mode
            conflict_analyzer.analyze_compare(
                plot=plot_enabled,
                output_dir=output_dir,
                output_dir2=output_dir2,
                label1=label1,
                label2=label2,
            )
            scenario_analyzer.analyze_compare(
                plot=plot_enabled,
                output_dir=output_dir,
                output_dir2=output_dir2,
                label1=label1,
                label2=label2,
            )
        else:
            # Single file mode
            conflict_analyzer.analyze(plot=plot_enabled, output_dir=output_dir)
            scenario_analyzer.analyze(plot=plot_enabled, output_dir=output_dir)

    if perfomance_report_dir:
        perfomance_analyzer = PerformanceAnalyzer()
        perfomance_analyzer.analyze(plot=plot_enabled, output_dir=perfomance_report_dir)


if __name__ == "__main__":
    main()
