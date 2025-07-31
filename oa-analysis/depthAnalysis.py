import json
import csv
import matplotlib.pyplot as plt
from collections import defaultdict
import numpy as np

with open('out.json') as f:
    data = json.load(f)

conflicts_l_lengths = dict()
conflicts_r_lengths = dict()
conflicts_diffs = dict()
conflicts_depth = dict()
conflicts_same_class = dict()
conflicts_same_method = dict()
scenario_jar_map = dict()

rows = []
same_depth_count = 0
conflict_idx = 0

def process_conflicts(conflicts, start_idx=0):
    global same_depth_count
    global conflict_idx

    conflicts_l_lengths[start_idx] = []
    conflicts_r_lengths[start_idx] = []
    conflicts_same_class[start_idx] = []
    conflicts_same_method[start_idx] = []
    conflicts_diffs[start_idx] = []
    conflicts_depth[start_idx] = []
    scenario_jar_map[start_idx] = set()

    for _, entry in enumerate(conflicts):
        if not isinstance(entry, dict):
            raise Exception("Failed to read entry as dict", entry)
        if 'body' not in entry or 'interference' not in entry['body']:
            raise Exception("Failed to read entry with body-interference", entry)

        scenario_jar = entry.get("ScenarioJAR")
        scenario_jar_map[start_idx].add(scenario_jar)

        interference = entry['body']['interference']
        if not isinstance(interference, list) or len(interference) < 2:
            raise Exception("Interference has less than 2 branches", entry)

        l_stack = interference[0].get('stackTrace', [])
        r_stack = interference[1].get('stackTrace', [])

        l_len = len(l_stack)
        r_len = len(r_stack)
        diff = abs(l_len - r_len)
        max_len = max(l_len, r_len)

        l_class = interference[0].get('location', {}).get('class')
        r_class = interference[1].get('location', {}).get('class')
        l_method = interference[0].get('location', {}).get('method')
        r_method = interference[1].get('location', {}).get('method')

        same_class = l_class == r_class and l_class is not None
        same_method = same_class and l_method == r_method and l_method is not None

        # Store per-index metrics
        conflicts_l_lengths[start_idx].append(l_len)
        conflicts_r_lengths[start_idx].append(r_len)
        conflicts_diffs[start_idx].append(diff)
        conflicts_depth[start_idx].append(max_len)
        conflicts_same_class[start_idx].append(same_class)
        conflicts_same_method[start_idx].append(same_method)

        if l_len == r_len:
            same_depth_count += 1

        rows.append([conflict_idx, l_len, r_len, max_len, diff, same_class, same_method, scenario_jar, start_idx])
        conflict_idx += 1

idx = 0
if data and isinstance(data, list):
    for entry in data:
        if isinstance(entry, dict) and 'conflicts' in entry:
            conflicts = entry.get('conflicts', [])
            process_conflicts(conflicts, start_idx=idx)
            idx += 1
        else:
            raise Exception("Conflict outside conflict wrapper")

for idx in scenario_jar_map:
    scenario_jar_set = scenario_jar_map[idx]
    if len(scenario_jar_set) > 1:
        raise Exception("Multiple scenario jars found", scenario_jar_set, idx)

with open('conflict_stacktrace_stats.csv', 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    writer.writerow(['conflict_index', 'left_stacktrace_length', 'right_stacktrace_length',
                     'conflict_depth', 'stacktrace_diff', 'same_class', 'same_method', "scenario_jar",
                     "scenario_index"])
    writer.writerows(rows)

conflict_counts = {idx: len(conflicts_depth[idx]) for idx in conflicts_depth}
scenario_median_depths = {idx: np.median(conflicts_depth[idx]) for idx in conflicts_depth}
scenario_max_depths = {idx: max(conflicts_depth[idx]) for idx in conflicts_depth}
scenario_min_depths = {idx: min(conflicts_depth[idx]) for idx in conflicts_depth}
scenario_median_diffs = {idx: np.median(conflicts_diffs[idx]) for idx in conflicts_diffs}
scenario_max_diffs = {idx: max(conflicts_diffs[idx]) for idx in conflicts_diffs}
scenario_min_diffs = {idx: min(conflicts_diffs[idx]) for idx in conflicts_diffs}

def compute_true_percentage(lst):
    return 100 * sum(lst) / len(lst) if lst else 0

scenario_same_method_pct = {idx: compute_true_percentage(conflicts_same_method[idx]) for idx in conflicts_same_method}
scenario_same_class_pct = {idx: compute_true_percentage(conflicts_same_class[idx]) for idx in conflicts_same_class}

conflicts_depths_hist = []
for idx in conflicts_depth:
    depths = conflicts_depth[idx]
    for depth in depths:
        conflicts_depths_hist.append(depth)

conflicts_diffs_hist = []
for idx in conflicts_diffs:
    diffs = conflicts_diffs[idx]
    for diff in diffs:
        conflicts_diffs_hist.append(diff)

scenario_jar_list = {idx: list(scenario_jar_map[idx])[0] for idx in scenario_jar_map}

def save_hist(data, title, xlabel, filename, bins='auto', xticks=None):
    plt.figure(figsize=(8, 5))
    plt.hist(data, bins=bins, rwidth=0.8)
    plt.title(title)
    plt.xlabel(xlabel)
    plt.ylabel('Frequency')
    if xticks is not None:
        plt.xticks(xticks)
    plt.tight_layout()
    plt.savefig(filename)
    plt.close()

save_hist(
    data=conflicts_depths_hist,
    title='Conflicts histogram of Depths',
    xlabel='Depths',
    filename='conflicts_depth_hist.png'
)

save_hist(
    data=conflicts_diffs_hist,
    title='Conflicts histogram of Diffs (Stacktrace)',
    xlabel='Absolute difference (L -R)',
    filename='conflicts_diffs_hist.png'
)

save_hist(
    data=conflict_counts.values(),
    title='Histogram of Number of Conflicts per Scenario',
    xlabel='Number of Conflicts',
    filename='conflicts_per_scenario.png'
)

jar_conflict_counts = defaultdict(int)
for idx, jar in scenario_jar_list.items():
    jar_conflict_counts[jar] += conflict_counts[idx]

save_hist(
    data=jar_conflict_counts.values(),
    title='Histogram of Number of Conflicts per ScenarioJAR',
    xlabel='Number of Conflicts',
    filename='conflicts_per_jar.png'
)

jar_scenario_counts = defaultdict(int)
for idx, jar in scenario_jar_list.items():
    jar_scenario_counts[jar] += 1

scenario_counts_distribution = list(jar_scenario_counts.values())

save_hist(
    data=scenario_counts_distribution,
    title='Histogram of Number of Scenarios per ScenarioJAR',
    xlabel='Number of Scenarios per JAR',
    filename='scenarios_per_jar_hist.png'
)

save_hist(
    data=scenario_median_depths.values(),
    title='Histogram of Median Conflict Depth per Scenario',
    xlabel='Median Depth',
    filename='median_depth_per_scenario.png'
)

save_hist(
    data=scenario_max_depths.values(),
    title='Histogram of Max Conflict Depth per Scenario',
    xlabel='Max Depth',
    filename='max_depth_per_scenario.png'
)

save_hist(
    data=scenario_min_depths.values(),
    title='Histogram of Min Conflict Depth per Scenario',
    xlabel='Min Depth',
    filename='min_depth_per_scenario.png'
)

save_hist(
    data=scenario_median_diffs.values(),
    title='Histogram of Median StackTrace Diff per Scenario',
    xlabel='Median Diff',
    filename='median_diff_per_scenario.png'
)

save_hist(
    data=scenario_max_diffs.values(),
    title='Histogram of Max StackTrace Diff per Scenario',
    xlabel='Max Diff',
    filename='max_diff_per_scenario.png'
)

save_hist(
    data=scenario_min_diffs.values(),
    title='Histogram of Min StackTrace Diff per Scenario',
    xlabel='Min Diff',
    filename='min_diff_per_scenario.png'
)

def bucket_percentages(pct_map, filename, title):
    buckets = [0]*5
    for pct in pct_map.values():
        if pct <= 20:
            buckets[0] += 1
        elif pct <= 40:
            buckets[1] += 1
        elif pct <= 60:
            buckets[2] += 1
        elif pct <= 80:
            buckets[3] += 1
        else:
            buckets[4] += 1
    labels = ['0–20%', '21–40%', '41–60%', '61–80%', '81–100%']
    plt.figure(figsize=(6, 4))
    plt.bar(labels, buckets, width=0.5)
    plt.title(title)
    plt.ylabel('Number of Scenarios')
    plt.tight_layout()
    plt.savefig(filename)
    plt.close()

bucket_percentages(scenario_same_method_pct, 'same_method_pct_dist.png', 'Same Method % Distribution')
bucket_percentages(scenario_same_class_pct, 'same_class_pct_dist.png', 'Same Class % Distribution')

with open('scenariojar_stats.csv', 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    writer.writerow(['scenario_jar', 'num_conflicts', 'num_scenarios'])

    for jar in jar_conflict_counts:
        num_conflicts = jar_conflict_counts[jar]
        num_scenarios = jar_scenario_counts[jar]
        writer.writerow([jar, num_conflicts, num_scenarios])