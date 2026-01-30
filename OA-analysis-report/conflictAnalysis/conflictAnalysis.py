import os
import pandas as pd
from visualization import Visualizer
from constants import *

class ConflictAnalyzer:
    def __init__(self):
        self.visualizer = Visualizer()
        self.output_dir = '.'
        
    def analyze(self, plot=True, output_dir='.'):
        self.output_dir = output_dir
        df = pd.read_csv(os.path.join(output_dir, CONFLICT_STATS_CSV))
        self._print_statistics(df)
        
        if plot:
            self._create_plots(df)
    
    def _print_statistics(self, df):
        self._print_conflict_stats(df, title=None)
    
    def _print_conflict_stats(self, df, title=None):
        """Print conflict statistics with optional title for comparison mode"""
        if title:
            print("-" * 60)
            print(f"\n{title}")
            print("-" * 60)
        else:
            print("\nConflict Analysis Results:")
        
        total_conflicts = len(df)
        total_scenarios = df[COL_SCENARIO_INDEX].nunique()
        same_depth_count = sum(df[COL_LEFT_LENGTH] == df[COL_RIGHT_LENGTH])
        same_class_count = sum(df[COL_SAME_CLASS])
        same_method_count = sum(df[COL_SAME_METHOD])        
        depth_mean = df[COL_DEPTH].mean()
        depth_median = df[COL_DEPTH].median()
        diff_mean = df[COL_DIFF].mean()
        diff_median = df[COL_DIFF].median()

        print(f"Total conflicts analyzed: {total_conflicts}")
        print(f"Conflicts with same depth: {same_depth_count} ({same_depth_count/total_conflicts*100:.2f}%)")
        print(f"Conflicts in same class: {same_class_count} ({same_class_count/total_conflicts*100:.2f}%)")
        print(f"Conflicts in same method: {same_method_count} ({same_method_count/total_conflicts*100:.2f}%)")
        
        print("\nDepth statistics:")
        print(f"  Mean depth: {depth_mean:.2f}")
        print(f"  Median depth: {depth_median:.2f}")

        print("\nStacktrace diff statistics:")
        print(f"  Mean diff: {diff_mean:.2f}")
        print(f"  Median diff: {diff_median:.2f}")

        print("\nConflicts per scenario distribution:")
        conflicts_per_scenario = df.groupby(COL_SCENARIO_INDEX).size()
        for num_conflicts in range(0, MAX_DEPTH + 1):
            count = sum(conflicts_per_scenario == num_conflicts)
            print(f"Scenarios with {num_conflicts} conflicts: {count} ({(count/total_scenarios*100):.2f}%)")
        
        print("\nConflict Depth Distribution:")

        for depth in range(0, MAX_DEPTH + 1):
            count = sum(df[COL_DEPTH] == depth)
            print(f"Conflicts with depth {depth}: {count} ({(count/total_conflicts*100):.2f}%)")

        print("\nConflicts diff Distribution:")
        for diff in range(0, 10):
            count = sum(df[COL_DIFF] == diff)
            print(f"Conflicts with diff {diff}: {count} ({(count/total_conflicts*100):.2f}%)")

    def _get_conflict_type_distribution(self, df):
        col = pd.Series("", index=df.index)
        A1_same_depth_same_class_depth_equal_1 = (df[COL_SAME_CLASS]) & (df[COL_LEFT_LENGTH] ==  df[COL_RIGHT_LENGTH]) & (df[COL_DEPTH] == 1)
        F2_same_depth_same_class_depth_larger_1 = (df[COL_SAME_CLASS]) & (df[COL_LEFT_LENGTH] ==  df[COL_RIGHT_LENGTH]) & (df[COL_DEPTH] > 1)
        D3_same_depth_different_class_depth_large_1 = (~df[COL_SAME_CLASS]) & (df[COL_LEFT_LENGTH] ==  df[COL_RIGHT_LENGTH]) & (df[COL_DEPTH] > 1)
        B2_C2_different_depth_different_class_depth_larger_1 = (~df[COL_SAME_CLASS]) & (df[COL_LEFT_LENGTH] !=  df[COL_RIGHT_LENGTH]) & (df[COL_DEPTH] > 1)
        B1_different_depth_same_class_depth_larger_1 = (df[COL_SAME_CLASS]) & (df[COL_LEFT_LENGTH] !=  df[COL_RIGHT_LENGTH]) & (df[COL_DEPTH] > 1)
        
        col[A1_same_depth_same_class_depth_equal_1] = "A1: Same depth, same class, depth = 1"
        col[F2_same_depth_same_class_depth_larger_1] = "F2: Same depth, same class, depth > 1"
        col[D3_same_depth_different_class_depth_large_1] = "D3: Same depth, different class, depth > 1"
        col[B2_C2_different_depth_different_class_depth_larger_1] = "B2/C2: Different depth, different class, depth > 1"
        col[B1_different_depth_same_class_depth_larger_1] = "B1: Different depth, same class, depth > 1"
        col[(col == "")] = "Other cases"
        
        # Convert to value counts and calculate percentages for pie chart
        value_counts = col.value_counts()
        total = len(df)
        labels = value_counts.index.tolist()
        values = value_counts.values
        percentages = [(count/total)*100 for count in values]
        
        # Define specific colors for each conflict type
        color_map = {
            "A1: Same depth, same class, depth = 1": "#FF6B6B",  # Red
            "F2: Same depth, same class, depth > 1":  "#45B7D1",   # Blue
            "D3: Same depth, different class, depth > 1": "#F0F407",   # Yellow,
            "B2/C2: Different depth, different class, depth > 1": "#12F02F",  # Green
            "B1: Different depth, same class, depth > 1": "#6D07EA",  # Purple
            "Other cases": "#C7CEEA"  # Lavender
        }
        colors = [color_map.get(label, "#CCCCCC") for label in labels]
        
        # Create a dict with all the pie chart data
        return {
            'values': values,
            'labels': labels,
            'percentages': percentages,
            'colors': colors
        }

    def _get_boolean_pie_data(self, df, column, true_label='True', false_label='False'):

        true_count = int(df[column].sum())
        false_count = int((~df[column]).sum())
        total = len(df)

        labels = [true_label, false_label]
        values = [true_count, false_count]
        percentages = [ (true_count/total)*100 if total>0 else 0,
                        (false_count/total)*100 if total>0 else 0 ]

        return {
            'values': values,
            'labels': labels,
            'percentages': percentages
        }

    def _create_plots(self, df):
        self._create_all_plots(df)

    def _create_all_plots(self, df, compare_df=None, label1='Data 1', label2='Data 2', output_dir=None):
        """Generic method to create all plots in single or comparison mode"""
        if output_dir is None:
            output_dir = self.output_dir
        
        # Depth loss bar charts
        self._plot_depth_loss(df, output_dir, compare_df, label1, label2)
        
        # Histograms
        self._plot_histograms(df, output_dir, compare_df, label1, label2)
        
        # Line plots
        self._plot_depth_lines(df, output_dir, compare_df, label1, label2)
        
        # Pie charts (comparison or single)
        self._plot_pie_charts(df, output_dir, compare_df, label1, label2)

    def _plot_depth_loss(self, df, output_dir, compare_df=None, label1='Data 1', label2='Data 2'):
        """Plot depth loss - bar chart (single or comparison)"""
        total_conflicts = len(df)
        depths = list(range(DEFAULT_DEPTH, MAX_DEPTH + 1))
        loss_percentages = [(sum(df[COL_DEPTH] > depth) / total_conflicts * 100) for depth in depths]
        
        if compare_df is not None:
            total_conflicts2 = len(compare_df)
            loss_percentages2 = [(sum(compare_df[COL_DEPTH] > depth) / total_conflicts2 * 100) for depth in depths]
            
            self.visualizer.plot_bar_chart_compare(
                x=depths,
                y1=loss_percentages,
                y2=loss_percentages2,
                label1=label1,
                label2=label2,
                title='Percentage of Conflicts Lost per Depth',
                xlabel='Depth',
                ylabel='Conflicts Lost (%)',
                filename=os.path.join(output_dir, 'compare_' + PLOT_DEPTH_LOSS)
            )
        else:
            self.visualizer.plot_bar_chart(
                x=depths,
                y=loss_percentages,
                title='Percentage of Conflicts Lost per Depth',
                xlabel='Depth',
                ylabel='Conflicts Lost (%)',
                filename=os.path.join(output_dir, PLOT_DEPTH_LOSS)
            )

    def _plot_histograms(self, df, output_dir, compare_df=None, label1='Data 1', label2='Data 2'):
        """Plot all histograms - depth, diff, etc."""
        # Depth distribution (discrete values - use side-by-side bars)
        self._plot_distribution_bars(
            df=df, compare_df=compare_df, col=COL_DEPTH, 
            max_val=MAX_DEPTH, title='Conflict Depth Distribution', 
            xlabel='Depth', filename=PLOT_DEPTH_HIST,
            output_dir=output_dir, label1=label1, label2=label2
        )
        
        # Diff distribution (discrete values up to 10 - use side-by-side bars)
        self._plot_distribution_bars(
            df=df, compare_df=compare_df, col=COL_DIFF,
            max_val=10, title='Conflicts Diff Distribution',
            xlabel='Absolute difference (L-R)', filename=PLOT_DIFF_HIST,
            output_dir=output_dir, label1=label1, label2=label2
        )
        
        # Filtered histograms (only in single mode)
        if compare_df is None:
            same_class_df = df[df[COL_SAME_CLASS] == False]
            self.visualizer.plot_histogram(
                data=same_class_df[COL_DEPTH],
                bins=30,
                title='Different Class Conflicts Histogram of Depths',
                xlabel='Depths',
                filename=os.path.join(output_dir, PLOT_DIFFERENT_CLASS_DEPTH_HIST)
            )

            same_method_df = df[df[COL_SAME_METHOD] == False]
            self.visualizer.plot_histogram(
                data=same_method_df[COL_DEPTH],
                bins=30,
                title='Different Method Conflicts Histogram of Depths',
                xlabel='Depths',
                filename=os.path.join(output_dir, PLOT_DIFFERENT_METHOD_DEPTH_HIST)
            )

            # Conflicts per scenario (discrete values - use side-by-side bars if comparing)
            conflicts_per_scenario = df.groupby(COL_SCENARIO_INDEX).size()
            self.visualizer.plot_histogram(
                data=conflicts_per_scenario,
                bins=100,
                title='Number of Conflicts per Scenario',
                xlabel='Number of Conflicts',
                filename=os.path.join(output_dir, PLOT_CONFLICTS_PER_SCENARIO)
            )

            conflicts_per_jar = df.groupby(COL_SCENARIO_JAR).size()
            self.visualizer.plot_histogram(
                data=conflicts_per_jar,
                bins=100,
                title='Number of Conflicts per ScenarioJAR',
                xlabel='Number of Conflicts',
                filename=os.path.join(output_dir, PLOT_CONFLICTS_PER_JAR)
            )

    def _plot_distribution_bars(self, df, col, max_val, title, xlabel, filename, output_dir, 
                                compare_df=None, label1='Data 1', label2='Data 2'):
        """Plot discrete distribution as side-by-side bars for comparison or single bar chart"""
        x_vals = list(range(0, max_val + 1))
        counts1 = [sum(df[col] == val) for val in x_vals]
        total1 = len(df)
        percentages1 = [(count / total1 * 100) if total1 > 0 else 0 for count in counts1]
        
        if compare_df is not None:
            counts2 = [sum(compare_df[col] == val) for val in x_vals]
            total2 = len(compare_df)
            percentages2 = [(count / total2 * 100) if total2 > 0 else 0 for count in counts2]
            
            self.visualizer.plot_bar_chart_compare(
                x=x_vals,
                y1=percentages1,
                y2=percentages2,
                label1=label1,
                label2=label2,
                title=title,
                xlabel=xlabel,
                ylabel='Percentage (%)',
                filename=os.path.join(output_dir, 'compare_' + filename)
            )
        else:
            self.visualizer.plot_bar_chart(
                x=x_vals,
                y=percentages1,
                title=title,
                xlabel=xlabel,
                ylabel='Percentage (%)',
                filename=os.path.join(output_dir, filename)
            )

    def _plot_depth_lines(self, df, output_dir, compare_df=None, label1='Data 1', label2='Data 2'):
        """Plot conflict depth lines"""
        if compare_df is not None:
            self.visualizer.plot_conflict_depth_lines_compare(
                df1=df,
                df2=compare_df,
                title='Conflict Depths Behavior',
                label1=label1,
                label2=label2,
                filename=os.path.join(output_dir, 'compare_' + PLOT_DEPTH_LINES)
            )
        else:
            self.visualizer.plot_conflict_depth_lines(
                df=df,
                title='Conflict Depths Behavior',
                filename=os.path.join(output_dir, PLOT_DEPTH_LINES)
            )

    def _plot_pie_charts(self, df, output_dir, compare_df=None, label1='Data 1', label2='Data 2'):
        """Plot pie charts - conflict types and boolean data"""
        # Conflict type distribution
        if compare_df is not None:
            data1 = self._get_conflict_type_distribution(df)
            data2 = self._get_conflict_type_distribution(compare_df)
            self.visualizer.plot_pie_chart_compare(
                data1=data1,
                data2=data2,
                title1=f'{label1} - Conflict Types',
                title2=f'{label2} - Conflict Types',
                filename=os.path.join(output_dir, 'compare_' + PLOT_TYPES_HIST)
            )
            self._create_compare_pie_charts(df, compare_df, label1, label2, output_dir)
        else:
            self.visualizer.plot_pie_chart(
                data=self._get_conflict_type_distribution(df),
                title='Distribution of Conflict Types',
                filename=os.path.join(output_dir, PLOT_TYPES_HIST)
            )
            
            same_class_data = self._get_boolean_pie_data(df, COL_SAME_CLASS, true_label='Same class', false_label='Different class')
            self.visualizer.plot_pie_chart(
                data=same_class_data,
                title='Proportion of Conflicts in Same Class',
                filename=os.path.join(output_dir, PLOT_SAME_CLASS_PIE)
            )

            same_method_data = self._get_boolean_pie_data(df, COL_SAME_METHOD, true_label='Same method', false_label='Different method')
            self.visualizer.plot_pie_chart(
                data=same_method_data,
                title='Proportion of Conflicts in Same Method',
                filename=os.path.join(output_dir, PLOT_SAME_METHOD_PIE)
            )

    def analyze_compare(self, plot=True, output_dir='.', output_dir2='.', label1=None, label2=None):
        """Analyze and compare two JSON file results"""
        df1 = pd.read_csv(os.path.join(output_dir, CONFLICT_STATS_CSV))
        df2 = pd.read_csv(os.path.join(output_dir2, CONFLICT_STATS_CSV))
        
        # Use provided labels, otherwise extract JSON file names from paths
        if label1 is None:
            label1 = os.path.basename(output_dir)
        if label2 is None:
            label2 = os.path.basename(output_dir2)
        
        self._print_conflict_stats(df1, title=label1)
        print("\n" + "="*60)
        self._print_conflict_stats(df2, title=label2)
        
        if plot:
            self._create_compare_plots(df1, df2, label1, label2, output_dir)

    def _print_statistics_with_title(self, df, title):
        self._print_conflict_stats(df, title=title)

    def _create_compare_plots(self, df1, df2, label1, label2, output_dir):
        """Create comparison plots for two datasets"""
        self._create_all_plots(df1, compare_df=df2, label1=label1, label2=label2, output_dir=output_dir)

    def _create_compare_pie_charts(self, df1, df2, label1, label2, output_dir):
        """Create side-by-side pie chart comparisons"""
        pie_configs = [
            (COL_SAME_CLASS, 'Same class', 'Different class', PLOT_SAME_CLASS_PIE, 'Same Class'),
            (COL_SAME_METHOD, 'Same method', 'Different method', PLOT_SAME_METHOD_PIE, 'Same Method')
        ]
        
        for col, true_label, false_label, filename, title_suffix in pie_configs:
            data1 = self._get_boolean_pie_data(df1, col, true_label=true_label, false_label=false_label)
            data2 = self._get_boolean_pie_data(df2, col, true_label=true_label, false_label=false_label)
            
            self.visualizer.plot_pie_chart_compare(
                data1=data1,
                data2=data2,
                title1=f'{label1} - {title_suffix}',
                title2=f'{label2} - {title_suffix}',
                filename=os.path.join(output_dir, 'compare_' + filename)
            )