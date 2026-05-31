# File paths
JSON_INPUT_FILE = 'out.json'
CONFLICT_STATS_CSV = 'conflict_stacktrace_stats.csv'
SCENARIO_STATS_CSV = 'scenariojar_stats.csv'
PERFORMANCE_SOOT_STATS_CSV = 'performance_soot_results_stats.csv'
PERFORMANCE_SUMMARY_STATS_JSON = 'performance_summary_stats.json'
PERFORMANCE_RESOURCE_STATS_CSV = 'performance_resource_stats.csv'
PERFORMANCE_REPORT_DIR = 'perfomanceReport'

# CSV column names
COL_CONFLICT_INDEX = 'conflict_index'
COL_LEFT_LENGTH = 'left_stacktrace_length'
COL_RIGHT_LENGTH = 'right_stacktrace_length'
COL_DEPTH = 'conflict_depth'
COL_DIFF = 'stacktrace_diff'
COL_SAME_CLASS = 'same_class'
COL_SAME_METHOD = 'same_method'
COL_SAME_START_CLASS = 'same_start_class'
COL_SAME_START_METHOD = 'same_start_method'
COL_SCENARIO_JAR = 'scenario_jar'
COL_SCENARIO_INDEX = 'scenario_index'
COL_NUM_CONFLICTS = 'num_conflicts'
COL_NUM_SCENARIOS = 'num_scenarios'

# Plot filenames
PLOT_DEPTH_HIST = 'conflicts_depth_hist.png'
PLOT_DIFF_HIST = 'conflicts_diffs_hist.png'
PLOT_TYPES_HIST = 'conflicts_types_pie.png'
PLOT_DEPTH_LINES = 'conflicts_depth_lines.png'
PLOT_CONFLICTS_PER_SCENARIO = 'conflicts_per_scenario.png'
PLOT_CONFLICTS_PER_JAR = 'conflicts_per_jar.png'
PLOT_SCENARIOS_PER_JAR = 'scenarios_per_jar_hist.png'
PLOT_SAME_METHOD_DIST = 'scenarios_same_method_dist.png'
PLOT_SAME_CLASS_DIST = 'scenarios_same_class_dist.png'
PLOT_SAME_CLASS_PIE = 'conflicts_same_class_pie.png'
PLOT_SAME_METHOD_PIE = 'conflicts_same_method_pie.png'
PLOT_DIFFERENT_CLASS_DEPTH_HIST = 'conflicts_different_class_depth_hist.png'
PLOT_DIFFERENT_METHOD_DEPTH_HIST = 'conflicts_different_method_depth_hist.png'
PLOT_MEDIAN_DEPTH = 'median_depth_per_scenario.png'
PLOT_MAX_DEPTH = 'max_depth_per_scenario.png'
PLOT_MIN_DEPTH = 'min_depth_per_scenario.png'
PLOT_MEDIAN_DIFF = 'median_diff_per_scenario.png'
PLOT_MAX_DIFF = 'max_diff_per_scenario.png'
PLOT_MIN_DIFF = 'min_diff_per_scenario.png'
PLOT_DEPTH_LOSS = 'conflicts_depth_loss.png'
PLOT_SCENARIO_DEPTH_AFFECT = 'scenarios_depth_affect.png'
PLOT_SCENARIO_DEPTH_LOSS = 'scenarios_depth_loss.png'
PLOT_MEDIAN_DEPTH_GROUPED = 'median_depth_grouped_per_scenario.png'
PLOT_MAX_DEPTH_GROUPED = 'max_depth_grouped_per_scenario.png'
PLOT_MIN_DEPTH_GROUPED = 'min_depth_grouped_per_scenario.png'

# Analysis thresholds and bins
PERCENTAGE_BUCKETS = [20, 40, 60, 80, 100]
CONCENTRATION_THRESHOLDS = [50, 75, 90, 99]
DEPTH_GROUPS = [(0, 2), (2, 5), (5, 10), (10, 20)]  # Depth range groups
CONFLICT_TYPES_BINS = 4
DEFAULT_HIST_BINS = 30
DEFAULT_DEPTH = 5
MAX_DEPTH = 14

DEPTH_GROUP_LABELS = [f"{lo}-{hi}" for lo, hi in DEPTH_GROUPS]

TIME_GROUPS = {
    "0-5":   (0,   5),
    "5-10":  (5,   10),
    "10-30": (10,  30),
    "30-60": (30,  60),
    "60-120": (60, 120),
    "120+":  (120, float("inf")),
}

TIME_GROUPS_WITH_TIMEOUTS = {
    "0-5":     (0,   5),
    "5-10":    (5,   10),
    "10-30":   (10,  30),
    "30-60":   (30,  60),
    "60-120":  (60,  120),
    "120-300": (120, 300),
    "timeouts": None,
}

CPU_BUCKETS = [
    ("0-30%",   (0,  30)),
    ("30-50%",  (30, 50)),
    ("50-70%",  (50, 70)),
    ("70-100%", (70, float("inf"))),
]

MEM_BUCKETS = [
    ("0-4GB",  (0,  4)),
    ("4-8GB",  (4,  8)),
    ("8-12GB", (8,  12)),
    ("12+GB",  (12, float("inf"))),
]