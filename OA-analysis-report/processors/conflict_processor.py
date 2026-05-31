import csv
import os
from collections import defaultdict

from constants import (
    CONFLICT_STATS_CSV,
    SCENARIO_STATS_CSV,
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
    COL_NUM_CONFLICTS,
    COL_NUM_SCENARIOS,
)


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
            l_len, r_len, diff, max_len,
            same_class, same_method, same_start_class, same_start_method,
            scenario_jar, start_idx,
        )

    def _store_metrics(
        self, l_len, r_len, diff, max_len,
        same_class, same_method, same_start_class, same_start_method,
        scenario_jar, start_idx,
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

        data["rows"].append([
            data["conflict_idx"],
            l_len, r_len, max_len, diff,
            same_class, same_method, same_start_class, same_start_method,
            scenario_jar, start_idx,
        ])
        data["conflict_idx"] += 1

    def save_results(self):
        conflict_stats_path = os.path.join(self.output_dir, CONFLICT_STATS_CSV)
        with open(conflict_stats_path, "w", newline="") as csvfile:
            writer = csv.writer(csvfile)
            writer.writerow([
                COL_CONFLICT_INDEX,
                COL_LEFT_LENGTH, COL_RIGHT_LENGTH, COL_DEPTH, COL_DIFF,
                COL_SAME_CLASS, COL_SAME_METHOD,
                COL_SAME_START_CLASS, COL_SAME_START_METHOD,
                COL_SCENARIO_JAR, COL_SCENARIO_INDEX,
            ])
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
                writer.writerow([jar, jar_conflict_counts[jar], jar_scenario_counts[jar]])
