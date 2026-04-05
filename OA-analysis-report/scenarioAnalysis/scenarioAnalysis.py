import os
import pandas as pd
from visualization import Visualizer
from constants import *

class ScenarioAnalyzer:
    def __init__(self):
        self.visualizer = Visualizer()
        self.output_dir = '.'
    
    @staticmethod
    def _extract_dataset_label_from_path(path):
        """Extract dataset name from path like analysisReport/depth20RefDatasetSPARK/conflictsData/"""
        # Check for RefDataset or MergeDataset keywords in the path
        if 'RefDataset' in path:
            return 'Ref Dataset'
        elif 'MergeDataset' in path:
            return 'Merge Dataset'
        
        # Otherwise get the parent directory name (depth20RefDatasetSPARK, etc.)
        parent_dir = os.path.basename(os.path.dirname(path))
        # Try to extract dataset name from parent directory
        if 'RefDataset' in parent_dir:
            return 'Ref Dataset'
        elif 'MergeDataset' in parent_dir:
            return 'Merge Dataset'
        else:
            return parent_dir

    def analyze(self, plot=True, output_dir='.'):
        self.output_dir = output_dir
        conflict_df = pd.read_csv(os.path.join(output_dir, CONFLICT_STATS_CSV))
        scenarioJAR_df = pd.read_csv(os.path.join(output_dir, SCENARIO_STATS_CSV))

        self._print_scenario_stats(conflict_df, scenarioJAR_df, title=None)
        
        if plot:
            self._create_plots(conflict_df, scenarioJAR_df)

    def _print_statistics(self, conflict_df, scenarioJAR_df):
        self._print_scenario_stats(conflict_df, scenarioJAR_df, title=None)

    def _print_scenario_stats(self, conflict_df, scenarioJAR_df, title=None):
        """Print scenario statistics with optional title for comparison mode"""
        if title:
            print("-" * 60)
            print(f"\n{title}")
            print("-" * 60)
        else:
            print("\nScenario Analysis Results:")
        
        total_scenarios = conflict_df[COL_SCENARIO_INDEX].nunique()
        total_jars = scenarioJAR_df[COL_SCENARIO_JAR].nunique()

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
        self._create_all_plots(conflict_df, scenarioJAR_df)

    def _create_all_plots(self, conflict_df, scenarioJAR_df, compare_conflict_df=None, 
                          compare_jar_df=None, label1='Data 1', label2='Data 2'):
        """Generic method to create all scenario plots in single or comparison mode"""
        # Plot scenarios per jar distribution (single mode only)
        if compare_conflict_df is None:
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
        
        if compare_conflict_df is not None:
            compare_scenario_stats = self._calculate_scenario_stats(compare_conflict_df)
            for metric, (title, filename) in metrics.items():
                self.visualizer.plot_distribution_buckets_compare(
                    data1=scenario_stats[metric],
                    data2=compare_scenario_stats[metric],
                    title=f'{title} % Distribution',
                    label1=label1,
                    label2=label2,
                    filename=os.path.join(self.output_dir, 'compare_' + filename)
                )
        else:
            for metric, (title, filename) in metrics.items():
                self.visualizer.plot_histogram(
                    data=scenario_stats[metric],
                    bins=len(PERCENTAGE_BUCKETS),
                    title=f'{title} % Distribution',
                    xlabel='Percentage',
                    filename=os.path.join(self.output_dir, filename)
                )

        # Metrics per scenario - depth affected/lost
        self._plot_scenario_depth_metrics(conflict_df, compare_conflict_df, label1, label2)
        
        # Metric histograms - median/max/min per scenario
        self._plot_scenario_metric_histograms(conflict_df, compare_conflict_df, label1, label2)
        
        # Grouped depth metrics - median/max/min per scenario grouped by depth ranges
        self._plot_scenario_depth_grouped_metrics(conflict_df, compare_conflict_df, label1, label2)

    def _plot_scenario_depth_metrics(self, conflict_df, compare_conflict_df=None, label1='Data 1', label2='Data 2'):
        """Plot scenario depth affected and lost per depth"""
        total_scenarios = conflict_df[COL_SCENARIO_INDEX].nunique()
        scenario_max_depth = conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].max()
        scenario_min_depth = conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].min()

        depths = list(range(DEFAULT_DEPTH, MAX_DEPTH + 1))
        affect_percentages = [(sum(scenario_max_depth > d) / total_scenarios * 100) for d in depths]
        loss_percentages = [(sum(d < scenario_min_depth) / total_scenarios * 100) for d in depths]

        if compare_conflict_df is not None:
            total_scenarios2 = compare_conflict_df[COL_SCENARIO_INDEX].nunique()
            scenario_max_depth2 = compare_conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].max()
            scenario_min_depth2 = compare_conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].min()
            
            affect_percentages2 = [(sum(scenario_max_depth2 > d) / total_scenarios2 * 100) for d in depths]
            loss_percentages2 = [(sum(d < scenario_min_depth2) / total_scenarios2 * 100) for d in depths]
            
            self.visualizer.plot_bar_chart_compare(
                x=depths,
                y1=affect_percentages,
                y2=affect_percentages2,
                label1=label1,
                label2=label2,
                title='Percentage of Scenarios Affected per Depth',
                xlabel='Depth',
                ylabel='Scenarios Affected (%)',
                filename=os.path.join(self.output_dir, 'compare_' + PLOT_SCENARIO_DEPTH_AFFECT)
            )

            self.visualizer.plot_bar_chart_compare(
                x=depths,
                y1=loss_percentages,
                y2=loss_percentages2,
                label1=label1,
                label2=label2,
                title='Percentage of Scenarios Lost per Depth',
                xlabel='Depth',
                ylabel='Scenarios Lost (%)',
                filename=os.path.join(self.output_dir, 'compare_' + PLOT_SCENARIO_DEPTH_LOSS)
            )
        else:
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

    def _plot_scenario_metric_histograms(self, df, compare_df=None, label1='Data 1', label2='Data 2'):
        """Plot histograms for median/max/min metrics per scenario"""
        metrics = {
            COL_DEPTH: ('Conflict Depth', [PLOT_MEDIAN_DEPTH, PLOT_MAX_DEPTH, PLOT_MIN_DEPTH]),
            COL_DIFF: ('StackTrace Diff', [PLOT_MEDIAN_DIFF, PLOT_MAX_DIFF, PLOT_MIN_DIFF])
        }
        
        for metric_col, (title, filenames) in metrics.items():
            for agg_func, filename in zip(['median', 'max', 'min'], filenames):
                prefix = agg_func.capitalize()
                
                if compare_df is not None:
                    self.visualizer.plot_scenario_metric_compare(
                        df1=df,
                        df2=compare_df,
                        metric_col=metric_col,
                        agg_func=agg_func,
                        title=f'{prefix} {title} per Scenario',
                        label1=label1,
                        label2=label2,
                        filename=os.path.join(self.output_dir, 'compare_' + filename)
                    )
                else:
                    self.visualizer.plot_scenario_metric(
                        df=df,
                        metric_col=metric_col,
                        agg_func=agg_func,
                        title=f'{prefix} {title} per Scenario',
                        filename=os.path.join(self.output_dir, filename)
                    )

    def _plot_scenario_depth_grouped_metrics(self, df, compare_df=None, label1='Data 1', label2='Data 2'):
        """Plot grouped bar charts for median/max/min depth per scenario by depth ranges (0-2, 2-5, 5-10, 10-20)"""
        # Define depth range groups
        depth_groups = [(0, 2), (2, 5), (5, 10), (10, 20)]
        group_labels = ['0-2', '2-5', '5-10', '10-20']
        
        # Calculate metrics for each depth group
        metrics = {
            COL_DEPTH: ('Conflict Depth', [PLOT_MEDIAN_DEPTH_GROUPED, PLOT_MAX_DEPTH_GROUPED, PLOT_MIN_DEPTH_GROUPED]),
        }
        
        for metric_col, (title, filenames) in metrics.items():
            for agg_func, filename in zip(['median', 'max', 'min'], filenames):
                prefix = agg_func.capitalize()
                
                # Calculate metrics for each depth range group
                scenario_metrics = df.groupby(COL_SCENARIO_INDEX)[metric_col].agg(agg_func)
                total_scenarios = len(scenario_metrics)
                
                # Calculate percentages for each depth group
                percentages1 = []
                for min_depth, max_depth in depth_groups:
                    count = len(scenario_metrics[(scenario_metrics >= min_depth) & (scenario_metrics < max_depth)])
                    percentage = (count / total_scenarios * 100) if total_scenarios > 0 else 0
                    percentages1.append(percentage)
                
                if compare_df is not None:
                    scenario_metrics2 = compare_df.groupby(COL_SCENARIO_INDEX)[metric_col].agg(agg_func)
                    total_scenarios2 = len(scenario_metrics2)
                    percentages2 = []
                    for min_depth, max_depth in depth_groups:
                        count = len(scenario_metrics2[(scenario_metrics2 >= min_depth) & (scenario_metrics2 < max_depth)])
                        percentage = (count / total_scenarios2 * 100) if total_scenarios2 > 0 else 0
                        percentages2.append(percentage)
                    
                    self.visualizer.plot_grouped_bar_chart_compare(
                        x=group_labels,
                        y1=percentages1,
                        y2=percentages2,
                        label1=label1,
                        label2=label2,
                        title=f'{prefix} {title} per Scenario',
                        xlabel='Depth Range',
                        ylabel='Percentage of Scenarios (%)',
                        filename=os.path.join(self.output_dir, 'compare_' + filename)
                    )
                else:
                    self.visualizer.plot_bar_chart(
                        x=group_labels,
                        y=percentages1,
                        title=f'{prefix} {title} per Scenario',
                        xlabel='Depth Range',
                        ylabel='Percentage of Scenarios (%)',
                        filename=os.path.join(self.output_dir, filename)
                    )

    def analyze_compare(self, plot=True, output_dir='.', output_dir2='.', label1=None, label2=None):
        """Analyze and compare two JSON file results"""
        conflict_df1 = pd.read_csv(os.path.join(output_dir, CONFLICT_STATS_CSV))
        scenarioJAR_df1 = pd.read_csv(os.path.join(output_dir, SCENARIO_STATS_CSV))
        
        conflict_df2 = pd.read_csv(os.path.join(output_dir2, CONFLICT_STATS_CSV))
        scenarioJAR_df2 = pd.read_csv(os.path.join(output_dir2, SCENARIO_STATS_CSV))
        
        # Use provided labels, otherwise extract dataset names from paths
        if label1 is None:
            label1 = self._extract_dataset_label_from_path(output_dir)
        if label2 is None:
            label2 = self._extract_dataset_label_from_path(output_dir2)
        
        self._print_scenario_stats(conflict_df1, scenarioJAR_df1, title=label1)
        print("\n" + "="*60)
        self._print_scenario_stats(conflict_df2, scenarioJAR_df2, title=label2)
        
        if plot:
            # Set output_dir to data1 directory for saving comparison plots
            self.output_dir = output_dir
            self._create_compare_plots(conflict_df1, conflict_df2, label1, label2, output_dir)

    def _create_compare_plots(self, conflict_df1, conflict_df2, label1, label2, output_dir):
        """Create comparison plots for two datasets"""
        self.output_dir = output_dir
        self._create_all_plots(
            conflict_df1, None,
            compare_conflict_df=conflict_df2,
            compare_jar_df=None,
            label1=label1,
            label2=label2
        )

if __name__ == "__main__":
    analyzer = ScenarioAnalyzer()
    analyzer.analyze(plot=True)
