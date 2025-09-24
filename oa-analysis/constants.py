# File paths
JSON_INPUT_FILE = 'out.json'
CONFLICT_STATS_CSV = 'conflict_stacktrace_stats.csv'
SCENARIO_STATS_CSV = 'scenariojar_stats.csv'

# CSV column names
COL_CONFLICT_INDEX = 'conflict_index'
COL_LEFT_LENGTH = 'left_stacktrace_length'
COL_RIGHT_LENGTH = 'right_stacktrace_length'
COL_DEPTH = 'conflict_depth'
COL_DIFF = 'stacktrace_diff'
COL_SAME_CLASS = 'same_class'
COL_SAME_METHOD = 'same_method'
COL_SCENARIO_JAR = 'scenario_jar'
COL_SCENARIO_INDEX = 'scenario_index'
COL_NUM_CONFLICTS = 'num_conflicts'
COL_NUM_SCENARIOS = 'num_scenarios'

# Plot filenames
PLOT_DEPTH_HIST = 'conflicts_depth_hist.png'
PLOT_DIFF_HIST = 'conflicts_diffs_hist.png'
PLOT_CONFLICTS_PER_SCENARIO = 'conflicts_per_scenario.png'
PLOT_CONFLICTS_PER_JAR = 'conflicts_per_jar.png'
PLOT_SCENARIOS_PER_JAR = 'scenarios_per_jar_hist.png'
PLOT_SAME_METHOD_DIST = 'same_method_pct_dist.png'
PLOT_SAME_CLASS_DIST = 'same_class_pct_dist.png'
PLOT_MEDIAN_DEPTH = 'median_depth_per_scenario.png'
PLOT_MAX_DEPTH = 'max_depth_per_scenario.png'
PLOT_MIN_DEPTH = 'min_depth_per_scenario.png'
PLOT_MEDIAN_DIFF = 'median_diff_per_scenario.png'
PLOT_MAX_DIFF = 'max_diff_per_scenario.png'
PLOT_MIN_DIFF = 'min_diff_per_scenario.png'

# Analysis thresholds and bins
PERCENTAGE_BUCKETS = [20, 40, 60, 80, 100]
DEFAULT_HIST_BINS = 30