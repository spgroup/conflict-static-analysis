import os
import pandas as pd
from visualization import Visualizer
from constants import *

class ScenarioAnalyzer:
    def __init__(self):
        self.visualizer = Visualizer()
        self.output_dir = '.'

    def analyze(self, plot=True, output_dir='.'):
        self.output_dir = output_dir
        conflict_df = pd.read_csv(os.path.join(output_dir, CONFLICT_STATS_CSV))
        scenarioJAR_df = pd.read_csv(os.path.join(output_dir, SCENARIO_STATS_CSV))

        self._print_statistics(conflict_df, scenarioJAR_df)
        
        if plot:
            self._create_plots(conflict_df, scenarioJAR_df)

    def _print_statistics(self, conflict_df, scenarioJAR_df):
        total_scenarios = conflict_df[COL_SCENARIO_INDEX].nunique()
        total_jars = scenarioJAR_df[COL_SCENARIO_JAR].nunique()

        print("\nScenario Analysis Results:")
        print(f"Total scenario JARs: {total_jars}")
        print(f"Total scenarios: {total_scenarios}")
        print("\nScenario Statistics:")
        print(f"Average conflicts per JAR: {scenarioJAR_df[COL_NUM_CONFLICTS].mean():.2f}")
        print(f"Average scenarios per JAR: {scenarioJAR_df[COL_NUM_SCENARIOS].mean():.2f}")

        self._print_threshold_stats_common(conflict_df)

    def _print_threshold_stats_common(self, conflict_df):
        """Common statistics printing logic for both single and comparison modes"""
        scenario_stats = self._calculate_scenario_stats(conflict_df)
        self._print_threshold_stats(scenario_stats)

        # Print percentiles for average conflict depth per scenario
        scenario_avg_depth = conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].mean()
        if scenario_avg_depth.empty:
            print("\nNo scenario depth data available to compute percentiles.")
            return

        percentiles = [25, 50, 75, 90, 99]
        quantiles = scenario_avg_depth.quantile([p / 100 for p in percentiles])

        print("\nScenario average depth percentiles:")
        for p in percentiles:
            qval = quantiles.loc[p / 100]
            print(f"  {p}th percentile: {qval:.2f}")

        # Count scenarios with mean depth greater than DEFAULT_DEPTH
        scenarios_above_default = (scenario_avg_depth > DEFAULT_DEPTH).sum()
        pct_above_default = (scenarios_above_default / len(scenario_avg_depth)) * 100
        print(f"\nScenarios with mean depth > {DEFAULT_DEPTH}: {scenarios_above_default} ({pct_above_default:.2f}%)")

        total_scenarios = conflict_df[COL_SCENARIO_INDEX].nunique()
        scenario_max_depth = conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].max()
        for depth_stat in range(DEFAULT_DEPTH, MAX_DEPTH + 1):
            count = sum(scenario_max_depth > depth_stat)
            print(f"Scenarios affected when depth is {depth_stat}: {count} ({(count/total_scenarios*100):.2f}%)")
        
        print('\n')

        scenario_min_depth = conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].min()
        for depth_stat in range(DEFAULT_DEPTH, MAX_DEPTH + 1):
            count = sum(depth_stat < scenario_min_depth)
            print(f"Scenarios lost when depth is {depth_stat}: {count} ({(count/total_scenarios*100):.2f}%)")

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

    def _create_plots(self, conflict_df, scenarioJAR_df):
        # Plot scenarios per jar distribution
        self.visualizer.plot_histogram(
            data=scenarioJAR_df[COL_NUM_SCENARIOS],
            bins=DEFAULT_HIST_BINS,
            title='Number of Scenarios per ScenarioJAR',
            xlabel='Number of Scenarios',
            filename=os.path.join(self.output_dir, PLOT_SCENARIOS_PER_JAR)
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
                filename=os.path.join(self.output_dir, filename)
            )

        # Metrics per scenario
        total_scenarios = conflict_df[COL_SCENARIO_INDEX].nunique()
        scenario_max_depth = conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].max()
        scenario_min_depth = conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].min()

        depths = list(range(DEFAULT_DEPTH, MAX_DEPTH + 1))
        affect_percentages = [(sum(scenario_max_depth > d) / total_scenarios * 100) for d in depths]
        loss_percentages = [(sum(d < scenario_min_depth) / total_scenarios * 100) for d in depths]

        self.visualizer.plot_bar_chart(
            x=depths,
            y=affect_percentages,
            title='Percentage of Scenarios Affected per Depth',
            xlabel='Depth',
            ylabel='Scenarios Affected (%)',
            filename=os.path.join(self.output_dir, PLOT_SCENARIO_DEPTH_AFFECT)
        )

        self.visualizer.plot_bar_chart(
            x=depths,
            y=loss_percentages,
            title='Percentage of Scenarios Lost per Depth',
            xlabel='Depth',
            ylabel='Scenarios Lost (%)',
            filename=os.path.join(self.output_dir, PLOT_SCENARIO_DEPTH_LOSS)
        )

        self._plot_scenario_metrics(conflict_df)

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
                    filename=os.path.join(self.output_dir, filename)
                )

    def analyze_compare(self, plot=True, output_dir='.', output_dir2='.'):
        """Analyze and compare two JSON file results"""
        conflict_df1 = pd.read_csv(os.path.join(output_dir, CONFLICT_STATS_CSV))
        scenarioJAR_df1 = pd.read_csv(os.path.join(output_dir, SCENARIO_STATS_CSV))
        
        conflict_df2 = pd.read_csv(os.path.join(output_dir2, CONFLICT_STATS_CSV))
        scenarioJAR_df2 = pd.read_csv(os.path.join(output_dir2, SCENARIO_STATS_CSV))
        
        # Extract JSON file names from paths for titles
        json_name1 = os.path.basename(output_dir)
        json_name2 = os.path.basename(output_dir2)
        
        self._print_statistics_with_title(conflict_df1, scenarioJAR_df1, json_name1)
        print("\n" + "="*60)
        self._print_statistics_with_title(conflict_df2, scenarioJAR_df2, json_name2)
        
        if plot:
            self._create_compare_plots(conflict_df1, conflict_df2, json_name1, json_name2, output_dir)

    def _print_statistics_with_title(self, conflict_df, scenarioJAR_df, title):
        total_scenarios = conflict_df[COL_SCENARIO_INDEX].nunique()
        total_jars = scenarioJAR_df[COL_SCENARIO_JAR].nunique()

        print(f"\n{title}")
        print("-" * 60)
        print(f"Total scenario JARs: {total_jars}")
        print(f"Total scenarios: {total_scenarios}")
        print("\nScenario Statistics:")
        print(f"Average conflicts per JAR: {scenarioJAR_df[COL_NUM_CONFLICTS].mean():.2f}")
        print(f"Average scenarios per JAR: {scenarioJAR_df[COL_NUM_SCENARIOS].mean():.2f}")

        self._print_threshold_stats_common(conflict_df)

    def _create_compare_plots(self, conflict_df1, conflict_df2, label1, label2, output_dir):
        """Create comparison plots for two datasets"""
        total_scenarios1 = conflict_df1[COL_SCENARIO_INDEX].nunique()
        total_scenarios2 = conflict_df2[COL_SCENARIO_INDEX].nunique()
        
        # Bar chart comparison for scenarios affected
        scenario_max_depth1 = conflict_df1.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].max()
        scenario_max_depth2 = conflict_df2.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].max()
        
        depths = list(range(DEFAULT_DEPTH, MAX_DEPTH + 1))
        affect_percentages1 = [(sum(scenario_max_depth1 > d) / total_scenarios1 * 100) for d in depths]
        affect_percentages2 = [(sum(scenario_max_depth2 > d) / total_scenarios2 * 100) for d in depths]
        
        self.visualizer.plot_bar_chart_compare(
            x=depths,
            y1=affect_percentages1,
            y2=affect_percentages2,
            label1=label1,
            label2=label2,
            title='Percentage of Scenarios Affected per Depth (Comparison)',
            xlabel='Depth',
            ylabel='Scenarios Affected (%)',
            filename=os.path.join(output_dir, 'compare_' + PLOT_SCENARIO_DEPTH_AFFECT)
        )

        # Bar chart comparison for scenarios lost
        scenario_min_depth1 = conflict_df1.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].min()
        scenario_min_depth2 = conflict_df2.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].min()
        
        loss_percentages1 = [(sum(d < scenario_min_depth1) / total_scenarios1 * 100) for d in depths]
        loss_percentages2 = [(sum(d < scenario_min_depth2) / total_scenarios2 * 100) for d in depths]
        
        self.visualizer.plot_bar_chart_compare(
            x=depths,
            y1=loss_percentages1,
            y2=loss_percentages2,
            label1=label1,
            label2=label2,
            title='Percentage of Scenarios Lost per Depth (Comparison)',
            xlabel='Depth',
            ylabel='Scenarios Lost (%)',
            filename=os.path.join(output_dir, 'compare_' + PLOT_SCENARIO_DEPTH_LOSS)
        )

