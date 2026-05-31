import json
import os
from collections import Counter

import pandas as pd

from constants import (
    PERFORMANCE_REPORT_DIR,
    PERFORMANCE_SOOT_STATS_CSV,
    PERFORMANCE_SUMMARY_STATS_JSON,
    PERFORMANCE_RESOURCE_STATS_CSV,
)


class PerformanceAggregator:
    def __init__(self, performance_data_path):
        self.performance_data_path = performance_data_path
        self.report_dir = os.path.join(performance_data_path, PERFORMANCE_REPORT_DIR)
        os.makedirs(self.report_dir, exist_ok=True)

    def aggregate(self) -> str:
        """Aggregate all results from individual runs."""
        self._aggregate_soot_results()
        self._aggregate_performance_summary()
        self._aggregate_resource_usage()
        return self.report_dir

    def _validate_oa_inter_consistency(self, df, groupby_cols):
        """Raise if any group has conflicting True/False OA Inter values across runs."""
        grouped = df.groupby(groupby_cols)["OA Inter"].unique()
        conflicts = []
        for group_key, unique_values in grouped.items():
            unique_vals_set = set(unique_values)
            if True in unique_vals_set and False in unique_vals_set:
                conflicts.append({
                    "group": (
                        dict(zip(groupby_cols, group_key))
                        if isinstance(group_key, tuple)
                        else {groupby_cols[0]: group_key}
                    ),
                    "conflicting_values": list(unique_vals_set),
                })

        if conflicts:
            error_msg = "OA Inter consistency validation failed. Found conflicting values:\n"
            for i, conflict in enumerate(conflicts, 1):
                error_msg += f"\n{i}. {conflict['group']}\n"
                error_msg += f"   Conflicting values: {conflict['conflicting_values']}\n"
                error_msg += "   Boolean conflict: OA Inter has both True and False values\n"
            raise Exception(error_msg)

    def _aggregate_soot_results(self):
        """Aggregate soot-results.csv: mean Time, majority-vote OA Inter."""
        all_data = []
        for i in range(1, 11):
            soot_file = os.path.join(self.performance_data_path, f"results{i}", "soot-results.csv")
            if os.path.exists(soot_file):
                df = pd.read_csv(soot_file, sep=";")
                df["result_num"] = i
                all_data.append(df)

        if not all_data:
            print(f"Warning: No soot-results.csv files found in {self.performance_data_path}")
            return

        combined_df = pd.concat(all_data, ignore_index=True)
        groupby_cols = ["project", "class", "method", "merge commit"]

        self._validate_oa_inter_consistency(combined_df, groupby_cols)

        aggregated = combined_df.groupby(groupby_cols).agg({"Time": "mean"}).reset_index()
        oa_inter_groups = (
            combined_df.groupby(groupby_cols)["OA Inter"]
            .apply(lambda x: Counter(x).most_common(1)[0][0])
            .reset_index(name="OA Inter")
        )
        result_df = aggregated.merge(oa_inter_groups, on=groupby_cols)
        result_df = result_df[groupby_cols + ["OA Inter", "Time"]]

        output_path = os.path.join(self.report_dir, PERFORMANCE_SOOT_STATS_CSV)
        result_df.to_csv(output_path, sep=";", index=False)
        print(f"Saved aggregated soot results to {output_path}")

    def _aggregate_performance_summary(self):
        """Aggregate performance_summary.json: mean of numeric fields."""
        all_summaries = []
        for i in range(1, 11):
            summary_file = os.path.join(
                self.performance_data_path, f"results{i}", "performance_summary.json"
            )
            if os.path.exists(summary_file):
                with open(summary_file, "r") as f:
                    all_summaries.append(json.load(f))

        if not all_summaries:
            print(f"Warning: No performance_summary.json files found in {self.performance_data_path}")
            return

        df = pd.json_normalize(all_summaries)
        numeric_cols = ["duration_seconds", "peak_memory_gb", "peak_cpu_percent"]
        aggregated = {col: df[col].mean() for col in numeric_cols if col in df.columns}
        for key in ["mode", "callgraph", "status"]:
            if key in all_summaries[0]:
                aggregated[key] = all_summaries[0][key]

        output_path = os.path.join(self.report_dir, PERFORMANCE_SUMMARY_STATS_JSON)
        with open(output_path, "w") as f:
            json.dump(aggregated, f, indent=2)
        print(f"Saved aggregated performance summary to {output_path}")

    def _aggregate_resource_usage(self):
        """Aggregate resource_usage_series.csv: mean per time second."""
        all_data = []
        for i in range(1, 11):
            resource_file = os.path.join(
                self.performance_data_path, f"results{i}", "resource_usage_series.csv"
            )
            if os.path.exists(resource_file):
                df = pd.read_csv(resource_file)
                if df.empty:
                    continue
                if "Time_Sec" in df.columns:
                    df["Time_Sec"] = (
                        pd.to_numeric(df["Time_Sec"], errors="coerce")
                        .round(0)
                        .astype(int)
                    )
                df["result_num"] = i
                all_data.append(df)

        if not all_data:
            print(f"Warning: No resource_usage_series.csv files with data found in {self.performance_data_path}")
            return

        max_time_sec = max(df["Time_Sec"].max() for df in all_data)
        aggregated_data = []
        for time_sec in range(int(max_time_sec) + 1):
            row_data: dict = {"Time_Sec": time_sec}
            cpu_values, memory_values = [], []
            for df in all_data:
                time_rows = df[df["Time_Sec"] == time_sec]
                if not time_rows.empty:
                    if "CPU_Percent_Total" in df.columns:
                        cpu_values.append(time_rows["CPU_Percent_Total"].iloc[0])
                    if "Memory_GB" in df.columns:
                        memory_values.append(time_rows["Memory_GB"].iloc[0])
            if cpu_values:
                row_data["CPU_Percent_Total"] = sum(cpu_values) / len(cpu_values)
            if memory_values:
                row_data["Memory_GB"] = sum(memory_values) / len(memory_values)
            aggregated_data.append(row_data)

        result_df = pd.DataFrame(aggregated_data)
        result_df = result_df.dropna(subset=["CPU_Percent_Total", "Memory_GB"])
        result_df = result_df[
            ~((result_df["CPU_Percent_Total"] == 0) & (result_df["Memory_GB"] == 0))
        ]

        output_path = os.path.join(self.report_dir, PERFORMANCE_RESOURCE_STATS_CSV)
        result_df.to_csv(output_path, index=False)
        print(f"Saved aggregated resource usage to {output_path}")
