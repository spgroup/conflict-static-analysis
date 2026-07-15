import os

import pandas as pd

from visualization import Visualizer
from constants import *
from utils import extract_dataset_label_from_path


class ConflictAnalyzer:
    def __init__(self):
        self.visualizer = Visualizer()
        self.output_dir = "."

    def analyze(self, plot=True, output_dir="."):
        self.output_dir = output_dir
        df = pd.read_csv(os.path.join(output_dir, CONFLICT_STATS_CSV))
        self._print_conflict_stats(df, title=None)
        if plot:
            self._create_all_plots(df)

    def analyze_compare(self, plot=True, output_dir=".", output_dir2=".", label1=None, label2=None):
        df1 = pd.read_csv(os.path.join(output_dir, CONFLICT_STATS_CSV))
        df2 = pd.read_csv(os.path.join(output_dir2, CONFLICT_STATS_CSV))

        label1 = label1 or extract_dataset_label_from_path(output_dir)
        label2 = label2 or extract_dataset_label_from_path(output_dir2)

        self._print_conflict_stats(df1, title=label1)
        print("\n" + "=" * 60)
        self._print_conflict_stats(df2, title=label2)

        if plot:
            self._create_all_plots(df1, compare_df=df2, label1=label1, label2=label2,
                                   output_dir=output_dir)

    def analyze_compare_multiple(self, plot=True, output_dirs=None, labels=None):
        """Print conflict stats for N datasets. Plots are not generated for N > 2."""
        output_dirs = output_dirs or []
        labels = labels or []
        for i, output_dir in enumerate(output_dirs):
            df = pd.read_csv(os.path.join(output_dir, CONFLICT_STATS_CSV))
            label = labels[i] if i < len(labels) else f"Dataset {i + 1}"
            if i > 0:
                print("\n" + "=" * 60)
            self._print_conflict_stats(df, title=label)

    # ------------------------------------------------------------------ stats

    def _print_conflict_stats(self, df, title=None):
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

    # ------------------------------------------------------------------ data helpers

    def _get_conflict_type_distribution(self, df):
        col = pd.Series("", index=df.index)
        A1 = (df[COL_SAME_CLASS]) & (df[COL_LEFT_LENGTH] == df[COL_RIGHT_LENGTH]) & (df[COL_DEPTH] == 1)
        F2 = (df[COL_SAME_CLASS]) & (df[COL_LEFT_LENGTH] == df[COL_RIGHT_LENGTH]) & (df[COL_DEPTH] > 1)
        D3 = (~df[COL_SAME_CLASS]) & (df[COL_LEFT_LENGTH] == df[COL_RIGHT_LENGTH]) & (df[COL_DEPTH] > 1)
        B2C2 = (~df[COL_SAME_CLASS]) & (df[COL_LEFT_LENGTH] != df[COL_RIGHT_LENGTH]) & (df[COL_DEPTH] > 1)
        B1 = (df[COL_SAME_CLASS]) & (df[COL_LEFT_LENGTH] != df[COL_RIGHT_LENGTH]) & (df[COL_DEPTH] > 1)

        col[A1]   = "A1: Same depth, same class, depth = 1"
        col[F2]   = "F2: Same depth, same class, depth > 1"
        col[D3]   = "D3: Same depth, different class, depth > 1"
        col[B2C2] = "B2/C2: Different depth, different class, depth > 1"
        col[B1]   = "B1: Different depth, same class, depth > 1"
        col[col == ""] = "Other cases"

        value_counts = col.value_counts()
        total = len(df)
        labels = value_counts.index.tolist()
        values = value_counts.values
        percentages = [(count / total) * 100 for count in values]

        color_map = {
            "A1: Same depth, same class, depth = 1":           "#FF6B6B",
            "F2: Same depth, same class, depth > 1":           "#45B7D1",
            "D3: Same depth, different class, depth > 1":      "#F0F407",
            "B2/C2: Different depth, different class, depth > 1": "#12F02F",
            "B1: Different depth, same class, depth > 1":      "#6D07EA",
            "Other cases":                                      "#C7CEEA",
        }
        return {
            "values": values,
            "labels": labels,
            "percentages": percentages,
            "colors": [color_map.get(l, "#CCCCCC") for l in labels],
        }

    def _get_boolean_pie_data(self, df, column, true_label="True", false_label="False"):
        true_count = int(df[column].sum())
        false_count = int((~df[column]).sum())
        total = len(df)
        return {
            "values": [true_count, false_count],
            "labels": [true_label, false_label],
            "percentages": [
                (true_count / total) * 100 if total > 0 else 0,
                (false_count / total) * 100 if total > 0 else 0,
            ],
            "colors": ["steelblue", "coral"],
        }

    # ------------------------------------------------------------------ plots

    def _create_all_plots(self, df, compare_df=None, label1="Data 1", label2="Data 2", output_dir=None):
        if output_dir is None:
            output_dir = self.output_dir
        self._plot_depth_loss(df, output_dir, compare_df, label1, label2)
        self._plot_histograms(df, output_dir, compare_df, label1, label2)
        self._plot_depth_lines(df, output_dir, compare_df, label1, label2)
        self._plot_pie_charts(df, output_dir, compare_df, label1, label2)

    def _plot_depth_loss(self, df, output_dir, compare_df=None, label1="Data 1", label2="Data 2"):
        total_conflicts = len(df)
        depths = list(range(DEFAULT_DEPTH, MAX_DEPTH + 1))
        loss_pcts = [(sum(df[COL_DEPTH] > d) / total_conflicts * 100) for d in depths]

        if compare_df is not None:
            total2 = len(compare_df)
            loss_pcts2 = [(sum(compare_df[COL_DEPTH] > d) / total2 * 100) for d in depths]
            self.visualizer.plot_bar_chart_compare(
                x=depths, y1=loss_pcts, y2=loss_pcts2,
                label1=label1, label2=label2,
                title="Percentage of Conflicts Lost per Depth",
                xlabel="Depth", ylabel="Conflicts Lost (%)",
                filename=os.path.join(output_dir, "compare_" + PLOT_DEPTH_LOSS),
            )
        else:
            self.visualizer.plot_bar_chart(
                x=depths, y=loss_pcts,
                title="Percentage of Conflicts Lost per Depth",
                xlabel="Depth", ylabel="Conflicts Lost (%)",
                filename=os.path.join(output_dir, PLOT_DEPTH_LOSS),
            )

    def _plot_distribution_bars(self, df, col, max_val, title, xlabel, filename, output_dir,
                                compare_df=None, label1="Data 1", label2="Data 2"):
        x_vals = list(range(0, max_val + 1))
        total1 = len(df)
        pcts1 = [(sum(df[col] == v) / total1 * 100) if total1 > 0 else 0 for v in x_vals]

        if compare_df is not None:
            total2 = len(compare_df)
            pcts2 = [(sum(compare_df[col] == v) / total2 * 100) if total2 > 0 else 0 for v in x_vals]
            self.visualizer.plot_bar_chart_compare(
                x=x_vals, y1=pcts1, y2=pcts2,
                label1=label1, label2=label2,
                title=title, xlabel=xlabel, ylabel="Percentage (%)",
                filename=os.path.join(output_dir, "compare_" + filename),
            )
        else:
            self.visualizer.plot_bar_chart(
                x=x_vals, y=pcts1,
                title=title, xlabel=xlabel, ylabel="Percentage (%)",
                filename=os.path.join(output_dir, filename),
            )

    def _plot_histograms(self, df, output_dir, compare_df=None, label1="Data 1", label2="Data 2"):
        self._plot_distribution_bars(
            df=df, compare_df=compare_df, col=COL_DEPTH, max_val=MAX_DEPTH,
            title="Conflict Depth Distribution", xlabel="Depth",
            filename=PLOT_DEPTH_HIST, output_dir=output_dir,
            label1=label1, label2=label2,
        )
        self._plot_distribution_bars(
            df=df, compare_df=compare_df, col=COL_DIFF, max_val=10,
            title="Conflicts Diff Distribution", xlabel="Absolute difference (L-R)",
            filename=PLOT_DIFF_HIST, output_dir=output_dir,
            label1=label1, label2=label2,
        )

        conflicts_per_scenario = df.groupby(COL_SCENARIO_INDEX).size()
        if compare_df is not None:
            conflicts_per_scenario2 = compare_df.groupby(COL_SCENARIO_INDEX).size()
            self.visualizer.plot_conflicts_per_scenario_compare(
                data1=conflicts_per_scenario, data2=conflicts_per_scenario2,
                title="Number of Conflicts per Scenario",
                label1=label1, label2=label2,
                filename=os.path.join(output_dir, "compare_" + PLOT_CONFLICTS_PER_SCENARIO),
            )
        else:
            self.visualizer.plot_histogram(
                data=conflicts_per_scenario, bins=100,
                title="Number of Conflicts per Scenario",
                xlabel="Number of Conflicts",
                filename=os.path.join(output_dir, PLOT_CONFLICTS_PER_SCENARIO),
            )

            same_class_df = df[df[COL_SAME_CLASS] == False]
            self.visualizer.plot_histogram(
                data=same_class_df[COL_DEPTH], bins=30,
                title="Different Class Conflicts Histogram of Depths",
                xlabel="Depths",
                filename=os.path.join(output_dir, PLOT_DIFFERENT_CLASS_DEPTH_HIST),
            )
            same_method_df = df[df[COL_SAME_METHOD] == False]
            self.visualizer.plot_histogram(
                data=same_method_df[COL_DEPTH], bins=30,
                title="Different Method Conflicts Histogram of Depths",
                xlabel="Depths",
                filename=os.path.join(output_dir, PLOT_DIFFERENT_METHOD_DEPTH_HIST),
            )
            conflicts_per_jar = df.groupby(COL_SCENARIO_JAR).size()
            self.visualizer.plot_histogram(
                data=conflicts_per_jar, bins=100,
                title="Number of Conflicts per ScenarioJAR",
                xlabel="Number of Conflicts",
                filename=os.path.join(output_dir, PLOT_CONFLICTS_PER_JAR),
            )

    def _plot_depth_lines(self, df, output_dir, compare_df=None, label1="Data 1", label2="Data 2"):
        if compare_df is not None:
            self.visualizer.plot_conflict_depth_lines_compare(
                df1=df, df2=compare_df,
                title="Conflict Depths Behavior",
                label1=label1, label2=label2,
                filename=os.path.join(output_dir, "compare_" + PLOT_DEPTH_LINES),
            )
        else:
            self.visualizer.plot_conflict_depth_lines(
                df=df,
                title="Conflict Depths Behavior",
                filename=os.path.join(output_dir, PLOT_DEPTH_LINES),
            )

    def _plot_pie_charts(self, df, output_dir, compare_df=None, label1="Data 1", label2="Data 2"):
        if compare_df is not None:
            self.visualizer.plot_pie_chart_compare(
                data1=self._get_conflict_type_distribution(df),
                data2=self._get_conflict_type_distribution(compare_df),
                title1=f"{label1} - Conflict Types",
                title2=f"{label2} - Conflict Types",
                filename=os.path.join(output_dir, "compare_" + PLOT_TYPES_HIST),
            )
            for col, true_lbl, false_lbl, filename, suffix in [
                (COL_SAME_CLASS,   "Same class",   "Different class",   PLOT_SAME_CLASS_PIE,   "Same Class"),
                (COL_SAME_METHOD,  "Same method",  "Different method",  PLOT_SAME_METHOD_PIE,  "Same Method"),
            ]:
                self.visualizer.plot_pie_chart_compare(
                    data1=self._get_boolean_pie_data(df,         col, true_lbl, false_lbl),
                    data2=self._get_boolean_pie_data(compare_df, col, true_lbl, false_lbl),
                    title1=f"{label1} - {suffix}",
                    title2=f"{label2} - {suffix}",
                    filename=os.path.join(output_dir, "compare_" + filename),
                )
        else:
            self.visualizer.plot_pie_chart(
                data=self._get_conflict_type_distribution(df),
                title="Distribution of Conflict Types",
                filename=os.path.join(output_dir, PLOT_TYPES_HIST),
            )
            self.visualizer.plot_pie_chart(
                data=self._get_boolean_pie_data(df, COL_SAME_CLASS, "Same class", "Different class"),
                title="Proportion of Conflicts in Same Class",
                filename=os.path.join(output_dir, PLOT_SAME_CLASS_PIE),
            )
            self.visualizer.plot_pie_chart(
                data=self._get_boolean_pie_data(df, COL_SAME_METHOD, "Same method", "Different method"),
                title="Proportion of Conflicts in Same Method",
                filename=os.path.join(output_dir, PLOT_SAME_METHOD_PIE),
            )
