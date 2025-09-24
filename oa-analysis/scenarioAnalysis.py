import pandas as pd
from visualization import Visualizer
from constants import *

class ScenarioAnalyzer:
    def __init__(self):
        self.visualizer = Visualizer()

    def analyze(self, plot=True):
        conflict_df = pd.read_csv(CONFLICT_STATS_CSV)
        scenario_df = pd.read_csv(SCENARIO_STATS_CSV)

        self._print_statistics(conflict_df, scenario_df)
        
        if plot:
            self._create_plots(conflict_df, scenario_df)

    def _print_statistics(self, conflict_df, scenario_df):
        total_scenarios = len(scenario_df)
        total_jars = scenario_df[COL_SCENARIO_JAR].nunique()

        print("\nScenario Analysis Results:")
        print(f"Total scenario JARs: {total_jars}")
        print(f"Total scenarios: {total_scenarios}")
        print("\nScenario Statistics:")
        print(f"Average conflicts per scenario: {scenario_df[COL_NUM_CONFLICTS].mean():.2f}")
        print(f"Average scenarios per JAR: {scenario_df[COL_NUM_SCENARIOS].mean():.2f}")

        scenario_stats = self._calculate_scenario_stats(conflict_df)
        self._print_threshold_stats(scenario_stats)

    def _calculate_scenario_stats(self, conflict_df):
        return conflict_df.groupby(COL_SCENARIO_INDEX).agg({
            COL_SAME_CLASS: 'mean',
            COL_SAME_METHOD: 'mean'
        }) * 100

    def _print_threshold_stats(self, scenario_stats):
        print("\nSame Class/Method Statistics:")
        print("Percentage of scenarios with:")
        for threshold in PERCENTAGE_BUCKETS:
            class_pct = (scenario_stats[COL_SAME_CLASS] > threshold).mean() * 100
            method_pct = (scenario_stats[COL_SAME_METHOD] > threshold).mean() * 100
            print(f">{threshold}% same class: {class_pct:.1f}%")
            print(f">{threshold}% same method: {method_pct:.1f}%")

    def _create_plots(self, conflict_df, scenario_df):
        # Plot scenarios per jar distribution
        self.visualizer.plot_distribution(
            data=scenario_df[COL_NUM_SCENARIOS],
            title='Number of Scenarios per ScenarioJAR',
            xlabel='Number of Scenarios',
            ylabel='Frequency',
            filename=PLOT_SCENARIOS_PER_JAR
        )

        # Plot same class/method percentage distributions
        scenario_stats = self._calculate_scenario_stats(conflict_df)
        metrics = {
            COL_SAME_CLASS: ('Same Class', PLOT_SAME_CLASS_DIST),
            COL_SAME_METHOD: ('Same Method', PLOT_SAME_METHOD_DIST)
        }

        for metric, (title, filename) in metrics.items():
            self.visualizer.plot_histogram(
                data=scenario_stats[metric],
                bins=len(PERCENTAGE_BUCKETS),
                title=f'{title} % Distribution',
                xlabel='Percentage',
                ylabel='Number of Scenarios',
                filename=filename
            )

if __name__ == "__main__":
    analyzer = ScenarioAnalyzer()
    analyzer.analyze(plot=True)
