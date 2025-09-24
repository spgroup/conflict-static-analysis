import pandas as pd
from visualization import Visualizer
from constants import *

class ConflictAnalyzer:
    def __init__(self):
        self.visualizer = Visualizer()
        
    def analyze(self, plot=True):
        df = pd.read_csv(CONFLICT_STATS_CSV)
        self._print_statistics(df)
        
        if plot:
            self._create_plots(df)
    
    def _print_statistics(self, df):
        total_conflicts = len(df)
        same_depth_count = sum(df[COL_LEFT_LENGTH] == df[COL_RIGHT_LENGTH])
        same_class_count = sum(df[COL_SAME_CLASS])
        same_method_count = sum(df[COL_SAME_METHOD])

        print("\nConflict Analysis Results:")
        print(f"Total conflicts analyzed: {total_conflicts}")
        print(f"Conflicts with same depth: {same_depth_count} ({same_depth_count/total_conflicts*100:.2f}%)")
        print(f"Conflicts in same class: {same_class_count} ({same_class_count/total_conflicts*100:.2f}%)")
        print(f"Conflicts in same method: {same_method_count} ({same_method_count/total_conflicts*100:.2f}%)")
    
    def _create_plots(self, df):
        # Depth and diff distributions
        self.visualizer.plot_histogram(
            data=df[COL_DEPTH],
            bins=DEFAULT_HIST_BINS,
            title='Conflicts Histogram of Depths',
            xlabel='Depths',
            ylabel='Frequency',
            filename=PLOT_DEPTH_HIST
        )

        self.visualizer.plot_histogram(
            data=df[COL_DIFF],
            bins=DEFAULT_HIST_BINS,
            title='Conflicts Histogram of Diffs (Stacktrace)',
            xlabel='Absolute difference (L-R)',
            ylabel='Frequency',
            filename=PLOT_DIFF_HIST
        )

        # Conflicts per scenario/jar
        conflicts_per_scenario = df.groupby(COL_SCENARIO_INDEX).size()
        self.visualizer.plot_distribution(
            data=conflicts_per_scenario,
            title='Number of Conflicts per Scenario',
            xlabel='Number of Conflicts',
            ylabel='Frequency',
            filename=PLOT_CONFLICTS_PER_SCENARIO
        )

        conflicts_per_jar = df.groupby(COL_SCENARIO_JAR).size()
        self.visualizer.plot_distribution(
            data=conflicts_per_jar,
            title='Number of Conflicts per ScenarioJAR',
            xlabel='Number of Conflicts',
            ylabel='Frequency',
            filename=PLOT_CONFLICTS_PER_JAR
        )

        # Metrics per scenario
        self._plot_scenario_metrics(df)
    
    def _plot_scenario_metrics(self, df):
        metrics = {
            COL_DEPTH: ('Conflict Depth', [PLOT_MEDIAN_DEPTH, PLOT_MAX_DEPTH, PLOT_MIN_DEPTH]),
            COL_DIFF: ('StackTrace Diff', [PLOT_MEDIAN_DIFF, PLOT_MAX_DIFF, PLOT_MIN_DIFF])
        }
        
        for metric_col, (title, filenames) in metrics.items():
            for agg_func, filename in zip(['median', 'max', 'min'], filenames):
                prefix = agg_func.capitalize()
                self.visualizer.plot_scenario_metric(
                    df=df,
                    metric_col=metric_col,
                    agg_func=agg_func,
                    title=f'{prefix} {title} per Scenario',
                    filename=filename
                )

if __name__ == "__main__":
    analyzer = ConflictAnalyzer()
    analyzer.analyze(plot=True)
