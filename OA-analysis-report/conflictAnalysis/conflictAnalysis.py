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
        total_conflicts = len(df)
        total_scenarios = df[COL_SCENARIO_INDEX].nunique()
        same_depth_count = sum(df[COL_LEFT_LENGTH] == df[COL_RIGHT_LENGTH])
        same_class_count = sum(df[COL_SAME_CLASS])
        same_method_count = sum(df[COL_SAME_METHOD])        
        depth_mean = df[COL_DEPTH].mean()
        depth_median = df[COL_DEPTH].median()
        diff_mean = df[COL_DIFF].mean()
        diff_median = df[COL_DIFF].median()

        print("\nConflict Analysis Results:")
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
        total_conflicts = len(df)
        depths = list(range(DEFAULT_DEPTH, MAX_DEPTH + 1))
        loss_percentages = [(sum(df[COL_DEPTH] > depth) / total_conflicts * 100) for depth in depths]
        
        self.visualizer.plot_bar_chart(
            x=depths,
            y=loss_percentages,
            title='Percentage of Conflicts Lost per Depth',
            xlabel='Depth',
            ylabel='Conflicts Lost (%)',
            filename=os.path.join(self.output_dir, PLOT_DEPTH_LOSS)
        )

        self.visualizer.plot_histogram(
            data=df[COL_DEPTH],
            bins=30,
            title='Conflicts Histogram of Depths',
            xlabel='Depths',
            filename=os.path.join(self.output_dir, PLOT_DEPTH_HIST)
        )

        same_class_df = df[df[COL_SAME_CLASS] == False]
        self.visualizer.plot_histogram(
            data=same_class_df[COL_DEPTH],
            bins=DEFAULT_HIST_BINS,
            title='Different Class Conflicts Histogram of Depths',
            xlabel='Depths',
            filename=os.path.join(self.output_dir, PLOT_DIFFERENT_CLASS_DEPTH_HIST)
        )

        same_method_df = df[df[COL_SAME_METHOD] == False]
        self.visualizer.plot_histogram(
            data=same_method_df[COL_DEPTH],
            bins=DEFAULT_HIST_BINS,
            title='Different Method Conflicts Histogram of Depths',
            xlabel='Depths',
            filename=os.path.join(self.output_dir, PLOT_DIFFERENT_METHOD_DEPTH_HIST)
        )

        self.visualizer.plot_pie_chart(
            data=self._get_conflict_type_distribution(df),
            title='Distribution of Conflict Types',
            filename=os.path.join(self.output_dir, PLOT_TYPES_HIST)
        )

        self.visualizer.plot_histogram(
            data=df[COL_DIFF],
            bins=DEFAULT_HIST_BINS,
            title='Conflicts Histogram of Diffs (Stacktrace)',
            xlabel='Absolute difference (L-R)',
            filename=os.path.join(self.output_dir, PLOT_DIFF_HIST)
        )

        conflicts_per_scenario = df.groupby(COL_SCENARIO_INDEX).size()
        self.visualizer.plot_histogram(
            data=conflicts_per_scenario,
            bins=100,
            title='Number of Conflicts per Scenario',
            xlabel='Number of Conflicts',
            filename=os.path.join(self.output_dir, PLOT_CONFLICTS_PER_SCENARIO)
        )

        conflicts_per_jar = df.groupby(COL_SCENARIO_JAR).size()
        self.visualizer.plot_histogram(
            data=conflicts_per_jar,
            bins=100,
            title='Number of Conflicts per ScenarioJAR',
            xlabel='Number of Conflicts',
            filename=os.path.join(self.output_dir, PLOT_CONFLICTS_PER_JAR)
        )
        
        self.visualizer.plot_conflict_depth_lines(
            df=df,
            title='Conflict Depths Behavior',
            filename=os.path.join(self.output_dir, PLOT_DEPTH_LINES)
        )

        same_class_data = self._get_boolean_pie_data(df, COL_SAME_CLASS, true_label='Same class', false_label='Different class')
        self.visualizer.plot_pie_chart(
            data=same_class_data,
            title='Proportion of Conflicts in Same Class',
            filename=os.path.join(self.output_dir, PLOT_SAME_CLASS_PIE)
        )

        same_method_data = self._get_boolean_pie_data(df, COL_SAME_METHOD, true_label='Same method', false_label='Different method')
        self.visualizer.plot_pie_chart(
            data=same_method_data,
            title='Proportion of Conflicts in Same Method',
            filename=os.path.join(self.output_dir, PLOT_SAME_METHOD_PIE)
        )

    def analyze_compare(self, plot=True, output_dir='.', output_dir2='.'):
        """Analyze and compare two JSON file results"""
        df1 = pd.read_csv(os.path.join(output_dir, CONFLICT_STATS_CSV))
        df2 = pd.read_csv(os.path.join(output_dir2, CONFLICT_STATS_CSV))
        
        # Extract JSON file names from paths for titles
        json_name1 = os.path.basename(output_dir)
        json_name2 = os.path.basename(output_dir2)
        
        self._print_statistics_with_title(df1, json_name1)
        print("\n" + "="*60)
        self._print_statistics_with_title(df2, json_name2)
        
        if plot:
            self._create_compare_plots(df1, df2, json_name1, json_name2, output_dir)

    def _print_statistics_with_title(self, df, title):
        total_conflicts = len(df)
        total_scenarios = df[COL_SCENARIO_INDEX].nunique()
        same_depth_count = sum(df[COL_LEFT_LENGTH] == df[COL_RIGHT_LENGTH])
        same_class_count = sum(df[COL_SAME_CLASS])
        same_method_count = sum(df[COL_SAME_METHOD])        
        depth_mean = df[COL_DEPTH].mean()
        depth_median = df[COL_DEPTH].median()
        diff_mean = df[COL_DIFF].mean()
        diff_median = df[COL_DIFF].median()

        print(f"\n{title}")
        print("-" * 60)
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

    def _create_compare_plots(self, df1, df2, label1, label2, output_dir):
        """Create comparison plots for two datasets"""
        total_conflicts1 = len(df1)
        total_conflicts2 = len(df2)
        depths = list(range(DEFAULT_DEPTH, MAX_DEPTH + 1))
        loss_percentages1 = [(sum(df1[COL_DEPTH] > depth) / total_conflicts1 * 100) for depth in depths]
        loss_percentages2 = [(sum(df2[COL_DEPTH] > depth) / total_conflicts2 * 100) for depth in depths]
        
        self.visualizer.plot_bar_chart_compare(
            x=depths,
            y1=loss_percentages1,
            y2=loss_percentages2,
            label1=label1,
            label2=label2,
            title='Percentage of Conflicts Lost per Depth (Comparison)',
            xlabel='Depth',
            ylabel='Conflicts Lost (%)',
            filename=os.path.join(output_dir, 'compare_' + PLOT_DEPTH_LOSS)
        )

        # Pie charts comparison
        same_class_data1 = self._get_boolean_pie_data(df1, COL_SAME_CLASS, true_label='Same class', false_label='Different class')
        same_class_data2 = self._get_boolean_pie_data(df2, COL_SAME_CLASS, true_label='Same class', false_label='Different class')
        
        self.visualizer.plot_pie_chart_compare(
            data1=same_class_data1,
            data2=same_class_data2,
            title1=f'{label1} - Same Class',
            title2=f'{label2} - Same Class',
            filename=os.path.join(output_dir, 'compare_' + PLOT_SAME_CLASS_PIE)
        )

        same_method_data1 = self._get_boolean_pie_data(df1, COL_SAME_METHOD, true_label='Same method', false_label='Different method')
        same_method_data2 = self._get_boolean_pie_data(df2, COL_SAME_METHOD, true_label='Same method', false_label='Different method')
        
        self.visualizer.plot_pie_chart_compare(
            data1=same_method_data1,
            data2=same_method_data2,
            title1=f'{label1} - Same Method',
            title2=f'{label2} - Same Method',
            filename=os.path.join(output_dir, 'compare_' + PLOT_SAME_METHOD_PIE)
        )
