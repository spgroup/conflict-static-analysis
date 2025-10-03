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
    
    def _get_conflict_type_distribution(self, df):
        col = pd.Series("", index=df.index)
        same_class_path_one = (df[COL_SAME_CLASS]) & (df[COL_LEFT_LENGTH] == 1) & (df[COL_RIGHT_LENGTH] == 1)
        same_class_path_larger_than_one = (df[COL_SAME_CLASS]) & (df[COL_LEFT_LENGTH] > 1) & (df[COL_RIGHT_LENGTH] > 1)
        different_class_path_larger_than_one = (~df[COL_SAME_CLASS]) & (df[COL_LEFT_LENGTH] > 1) & (df[COL_RIGHT_LENGTH] > 1)

        col[same_class_path_one] = "Same class, path size 1"
        col[same_class_path_larger_than_one] = "Same class, path size larger than 1"
        col[different_class_path_larger_than_one] = "Different class, path size larger than 1"
        col[(col == "")] = "Other cases"
        
        # Convert to value counts and calculate percentages for pie chart
        value_counts = col.value_counts()
        total = len(df)
        labels = value_counts.index.tolist()
        values = value_counts.values
        percentages = [(count/total)*100 for count in values]
        
        # Create a dict with all the pie chart data
        return {
            'values': values,
            'labels': labels,
            'percentages': percentages
        }

    def _create_plots(self, df):
        # Depth and diff distributions
        self.visualizer.plot_histogram(
            data=df[COL_DEPTH],
            bins=50,
            title='Conflicts Histogram of Depths',
            xlabel='Depths',
            filename=PLOT_DEPTH_HIST
        )

        self.visualizer.plot_pie_chart(
            data=self._get_conflict_type_distribution(df),
            title='Distribution of Conflict Types',
            filename=PLOT_TYPES_HIST
        )

        self.visualizer.plot_histogram(
            data=df[COL_DIFF],
            bins=DEFAULT_HIST_BINS,
            title='Conflicts Histogram of Diffs (Stacktrace)',
            xlabel='Absolute difference (L-R)',
            filename=PLOT_DIFF_HIST
        )

        # Conflicts per scenario/jar
        conflicts_per_scenario = df.groupby(COL_SCENARIO_INDEX).size()
        self.visualizer.plot_histogram(
            data=conflicts_per_scenario,
            bins=DEFAULT_HIST_BINS,
            title='Number of Conflicts per Scenario',
            xlabel='Number of Conflicts',
            filename=PLOT_CONFLICTS_PER_SCENARIO
        )

        conflicts_per_jar = df.groupby(COL_SCENARIO_JAR).size()
        self.visualizer.plot_histogram(
            data=conflicts_per_jar,
            bins=DEFAULT_HIST_BINS,
            title='Number of Conflicts per ScenarioJAR',
            xlabel='Number of Conflicts',
            filename=PLOT_CONFLICTS_PER_JAR
        )
        
        # Plot conflict depth lines
        self.visualizer.plot_conflict_depth_lines(
            df=df,
            title='Conflict Depths Behavior',
            filename=PLOT_DEPTH_LINES
        )
    

if __name__ == "__main__":
    analyzer = ConflictAnalyzer()
    analyzer.analyze(plot=True)
