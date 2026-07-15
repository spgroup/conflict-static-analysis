import os

import pandas as pd

from visualization import Visualizer
from constants import *
from utils import extract_dataset_label_from_path


class ScenarioAnalyzer:
    def __init__(self):
        self.visualizer = Visualizer()
        self.output_dir = "."

    def analyze(self, plot=True, output_dir="."):
        self.output_dir = output_dir
        conflict_df = pd.read_csv(os.path.join(output_dir, CONFLICT_STATS_CSV))
        scenario_jar_df = pd.read_csv(os.path.join(output_dir, SCENARIO_STATS_CSV))
        self._print_scenario_stats(conflict_df, scenario_jar_df, title=None)
        if plot:
            self._create_all_plots(conflict_df, scenario_jar_df)

    def analyze_compare(self, plot=True, output_dir=".", output_dir2=".", label1=None, label2=None):
        conflict_df1 = pd.read_csv(os.path.join(output_dir, CONFLICT_STATS_CSV))
        jar_df1 = pd.read_csv(os.path.join(output_dir, SCENARIO_STATS_CSV))
        conflict_df2 = pd.read_csv(os.path.join(output_dir2, CONFLICT_STATS_CSV))
        jar_df2 = pd.read_csv(os.path.join(output_dir2, SCENARIO_STATS_CSV))

        label1 = label1 or extract_dataset_label_from_path(output_dir)
        label2 = label2 or extract_dataset_label_from_path(output_dir2)

        self._print_scenario_stats(conflict_df1, jar_df1, title=label1)
        print("\n" + "=" * 60)
        self._print_scenario_stats(conflict_df2, jar_df2, title=label2)

        if plot:
            self.output_dir = output_dir
            self._create_all_plots(
                conflict_df1, None,
                compare_conflict_df=conflict_df2,
                label1=label1, label2=label2,
            )

    def analyze_compare_multiple(self, plot=True, output_dirs=None, labels=None):
        """Analyze N datasets; grouped bar plots for depth loss/affect."""
        output_dirs = output_dirs or []
        labels = labels or []
        datasets = []
        for i, output_dir in enumerate(output_dirs):
            conflict_df = pd.read_csv(os.path.join(output_dir, CONFLICT_STATS_CSV))
            jar_df = pd.read_csv(os.path.join(output_dir, SCENARIO_STATS_CSV))
            label = labels[i] if i < len(labels) else f"Dataset {i + 1}"
            if i > 0:
                print("\n" + "=" * 60)
            self._print_scenario_stats(conflict_df, jar_df, title=label)
            datasets.append((conflict_df, label))

        if plot and datasets:
            self.output_dir = output_dirs[0]
            self._plot_scenario_depth_loss_multiple(datasets)

    # ------------------------------------------------------------------ stats

    def _print_scenario_stats(self, conflict_df, scenario_jar_df, title=None):
        if title:
            print("-" * 60)
            print(f"\n{title}")
            print("-" * 60)
        else:
            print("\nScenario Analysis Results:")

        total_scenarios = conflict_df[COL_SCENARIO_INDEX].nunique()
        total_jars = scenario_jar_df[COL_SCENARIO_JAR].nunique()

        print(f"Total scenario JARs: {total_jars}")
        print(f"Total scenarios: {total_scenarios}")
        print("\nScenario Statistics:")
        print(f"Average conflicts per JAR: {scenario_jar_df[COL_NUM_CONFLICTS].mean():.2f}")
        print(f"Average scenarios per JAR: {scenario_jar_df[COL_NUM_SCENARIOS].mean():.2f}")

        scenario_stats = self._calculate_scenario_stats(conflict_df)
        self._print_threshold_stats(scenario_stats)
        self._print_diff_concentration_stats(conflict_df)
        self._print_depth_percentiles(conflict_df)

    def _print_depth_percentiles(self, conflict_df):
        scenario_avg_depth = conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].mean()
        if scenario_avg_depth.empty:
            print("\nNo scenario depth data available to compute percentiles.")
            return

        percentiles = [25, 50, 75, 90, 99]
        quantiles = scenario_avg_depth.quantile([p / 100 for p in percentiles])

        print("\nScenario average depth percentiles:")
        for p in percentiles:
            print(f"  {p}th percentile: {quantiles.loc[p / 100]:.2f}")

        scenarios_above_default = (scenario_avg_depth > DEFAULT_DEPTH).sum()
        pct_above = (scenarios_above_default / len(scenario_avg_depth)) * 100
        print(f"\nScenarios with mean depth > {DEFAULT_DEPTH}: {scenarios_above_default} ({pct_above:.2f}%)")

        total_scenarios = conflict_df[COL_SCENARIO_INDEX].nunique()
        scenario_max_depth = conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].max()
        for d in range(DEFAULT_DEPTH, MAX_DEPTH + 1):
            count = sum(scenario_max_depth > d)
            print(f"Scenarios affected when depth is {d}: {count} ({(count/total_scenarios*100):.2f}%)")

        print("\n")
        scenario_min_depth = conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].min()
        for d in range(DEFAULT_DEPTH, MAX_DEPTH + 1):
            count = sum(d < scenario_min_depth)
            print(f"Scenarios lost when depth is {d}: {count} ({(count/total_scenarios*100):.2f}%)")

    def _calculate_scenario_stats(self, conflict_df):
        return conflict_df.groupby(COL_SCENARIO_INDEX).agg({
            COL_SAME_CLASS: "mean",
            COL_SAME_METHOD: "mean",
        }) * 100

    def _print_threshold_stats(self, scenario_stats):
        print("\nSame Class/Method Statistics:")
        print("Percentage of scenarios with:")
        for threshold in PERCENTAGE_BUCKETS:
            class_pct = (scenario_stats[COL_SAME_CLASS] > threshold).mean() * 100
            method_pct = (scenario_stats[COL_SAME_METHOD] > threshold).mean() * 100
            print(f">{threshold}% same class: {class_pct:.1f}%")
            print(f">{threshold}% same method: {method_pct:.1f}%")

        print("\nScenarios with conflicts concentrated in the same class/method:")
        print(f"{'Threshold':<12} {'Class':>10} {'Method':>10}")
        print("-" * 34)
        for threshold in CONCENTRATION_THRESHOLDS:
            class_pct = (scenario_stats[COL_SAME_CLASS] >= threshold).mean() * 100
            method_pct = (scenario_stats[COL_SAME_METHOD] >= threshold).mean() * 100
            print(f">={threshold}%       {class_pct:>9.1f}% {method_pct:>9.1f}%")

    def _print_diff_concentration_stats(self, conflict_df):
        no_diff_pct = conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DIFF].apply(
            lambda x: (x == 0).mean() * 100
        )
        total = len(no_diff_pct)
        print("\nScenarios with conflicts concentrated in no stacktrace diff (diff=0):")
        print(f"{'Threshold':<12} {'Scenarios':>10}")
        print("-" * 24)
        for threshold in CONCENTRATION_THRESHOLDS:
            count = (no_diff_pct >= threshold).sum()
            pct = count / total * 100
            print(f">={threshold}%       {pct:>8.1f}%  ({count}/{total})")

    # ------------------------------------------------------------------ plots

    def _create_all_plots(self, conflict_df, scenario_jar_df, compare_conflict_df=None,
                          label1="Data 1", label2="Data 2"):
        if compare_conflict_df is None and scenario_jar_df is not None:
            self.visualizer.plot_histogram(
                data=scenario_jar_df[COL_NUM_SCENARIOS],
                bins=DEFAULT_HIST_BINS,
                title="Number of Scenarios per ScenarioJAR",
                xlabel="Number of Scenarios",
                filename=os.path.join(self.output_dir, PLOT_SCENARIOS_PER_JAR),
            )

        scenario_stats = self._calculate_scenario_stats(conflict_df)
        metrics = {
            COL_SAME_CLASS:  ("Same Class",  PLOT_SAME_CLASS_DIST),
            COL_SAME_METHOD: ("Same Method", PLOT_SAME_METHOD_DIST),
        }

        if compare_conflict_df is not None:
            compare_stats = self._calculate_scenario_stats(compare_conflict_df)
            for metric, (title, filename) in metrics.items():
                self.visualizer.plot_distribution_buckets_compare(
                    data1=scenario_stats[metric],
                    data2=compare_stats[metric],
                    title=f"{title} % Distribution",
                    label1=label1, label2=label2,
                    filename=os.path.join(self.output_dir, "compare_" + filename),
                )
        else:
            for metric, (title, filename) in metrics.items():
                self.visualizer.plot_histogram(
                    data=scenario_stats[metric],
                    bins=len(PERCENTAGE_BUCKETS),
                    title=f"{title} % Distribution",
                    xlabel="Percentage",
                    filename=os.path.join(self.output_dir, filename),
                )

        self._plot_scenario_depth_metrics(conflict_df, compare_conflict_df, label1, label2)
        self._plot_scenario_metric_histograms(conflict_df, compare_conflict_df, label1, label2)
        self._plot_scenario_depth_grouped_metrics(conflict_df, compare_conflict_df, label1, label2)

    def _plot_scenario_depth_metrics(self, conflict_df, compare_df=None, label1="Data 1", label2="Data 2"):
        total = conflict_df[COL_SCENARIO_INDEX].nunique()
        max_depth = conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].max()
        min_depth = conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].min()
        depths = list(range(DEFAULT_DEPTH, MAX_DEPTH + 1))
        affect_pcts = [(sum(max_depth > d) / total * 100) for d in depths]
        loss_pcts   = [(sum(d < min_depth) / total * 100) for d in depths]

        if compare_df is not None:
            total2 = compare_df[COL_SCENARIO_INDEX].nunique()
            max_depth2 = compare_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].max()
            min_depth2 = compare_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].min()
            affect_pcts2 = [(sum(max_depth2 > d) / total2 * 100) for d in depths]
            loss_pcts2   = [(sum(d < min_depth2) / total2 * 100) for d in depths]

            self.visualizer.plot_bar_chart_compare(
                x=depths, y1=affect_pcts, y2=affect_pcts2,
                label1=label1, label2=label2,
                title="Percentage of Scenarios Affected per Depth",
                xlabel="Depth", ylabel="Scenarios Affected (%)",
                filename=os.path.join(self.output_dir, "compare_" + PLOT_SCENARIO_DEPTH_AFFECT),
            )
            self.visualizer.plot_bar_chart_compare(
                x=depths, y1=loss_pcts, y2=loss_pcts2,
                label1=label1, label2=label2,
                title="Percentage of Scenarios Lost per Depth",
                xlabel="Depth", ylabel="Scenarios Lost (%)",
                filename=os.path.join(self.output_dir, "compare_" + PLOT_SCENARIO_DEPTH_LOSS),
            )
        else:
            self.visualizer.plot_bar_chart(
                x=depths, y=affect_pcts,
                title="Percentage of Scenarios Affected per Depth",
                xlabel="Depth", ylabel="Scenarios Affected (%)",
                filename=os.path.join(self.output_dir, PLOT_SCENARIO_DEPTH_AFFECT),
            )
            self.visualizer.plot_bar_chart(
                x=depths, y=loss_pcts,
                title="Percentage of Scenarios Lost per Depth",
                xlabel="Depth", ylabel="Scenarios Lost (%)",
                filename=os.path.join(self.output_dir, PLOT_SCENARIO_DEPTH_LOSS),
            )

    def _plot_scenario_metric_histograms(self, df, compare_df=None, label1="Data 1", label2="Data 2"):
        metrics = {
            COL_DEPTH: ("Conflict Depth",  [PLOT_MEDIAN_DEPTH, PLOT_MAX_DEPTH, PLOT_MIN_DEPTH]),
            COL_DIFF:  ("StackTrace Diff", [PLOT_MEDIAN_DIFF,  PLOT_MAX_DIFF,  PLOT_MIN_DIFF]),
        }
        for metric_col, (title, filenames) in metrics.items():
            for agg_func, filename in zip(["median", "max", "min"], filenames):
                prefix = agg_func.capitalize()
                if compare_df is not None:
                    self.visualizer.plot_scenario_metric_compare(
                        df1=df, df2=compare_df,
                        metric_col=metric_col, agg_func=agg_func,
                        title=f"{prefix} {title} per Scenario",
                        label1=label1, label2=label2,
                        filename=os.path.join(self.output_dir, "compare_" + filename),
                    )
                else:
                    self.visualizer.plot_scenario_metric(
                        df=df, metric_col=metric_col, agg_func=agg_func,
                        title=f"{prefix} {title} per Scenario",
                        filename=os.path.join(self.output_dir, filename),
                    )

    def _plot_scenario_depth_grouped_metrics(self, df, compare_df=None, label1="Data 1", label2="Data 2"):
        metrics = {
            COL_DEPTH: ("Conflict Depth", [PLOT_MEDIAN_DEPTH_GROUPED, PLOT_MAX_DEPTH_GROUPED, PLOT_MIN_DEPTH_GROUPED]),
        }
        for metric_col, (title, filenames) in metrics.items():
            for agg_func, filename in zip(["median", "max", "min"], filenames):
                prefix = agg_func.capitalize()
                scenario_metrics = df.groupby(COL_SCENARIO_INDEX)[metric_col].agg(agg_func)
                total = len(scenario_metrics)
                pcts1 = [
                    (len(scenario_metrics[(scenario_metrics >= lo) & (scenario_metrics < hi)]) / total * 100)
                    if total > 0 else 0
                    for lo, hi in DEPTH_GROUPS
                ]

                if compare_df is not None:
                    sm2 = compare_df.groupby(COL_SCENARIO_INDEX)[metric_col].agg(agg_func)
                    total2 = len(sm2)
                    pcts2 = [
                        (len(sm2[(sm2 >= lo) & (sm2 < hi)]) / total2 * 100)
                        if total2 > 0 else 0
                        for lo, hi in DEPTH_GROUPS
                    ]
                    self.visualizer.plot_bar_chart_compare(
                        x=DEPTH_GROUP_LABELS, y1=pcts1, y2=pcts2,
                        label1=label1, label2=label2,
                        title=f"{prefix} {title} per Scenario",
                        xlabel="Depth Range", ylabel="Percentage of Scenarios (%)",
                        filename=os.path.join(self.output_dir, "compare_" + filename),
                    )
                else:
                    self.visualizer.plot_bar_chart(
                        x=DEPTH_GROUP_LABELS, y=pcts1,
                        title=f"{prefix} {title} per Scenario",
                        xlabel="Depth Range", ylabel="Percentage of Scenarios (%)",
                        filename=os.path.join(self.output_dir, filename),
                    )

    def _plot_scenario_depth_loss_multiple(self, datasets):
        depths = list(range(DEFAULT_DEPTH, MAX_DEPTH + 1))
        depth_labels = [str(d) for d in depths]
        all_loss_pcts, all_affect_pcts, labels = [], [], []

        for conflict_df, label in datasets:
            total = conflict_df[COL_SCENARIO_INDEX].nunique()
            max_depth = conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].max()
            min_depth = conflict_df.groupby(COL_SCENARIO_INDEX)[COL_DEPTH].min()
            all_affect_pcts.append([(sum(max_depth > d) / total * 100) for d in depths])
            all_loss_pcts.append([(sum(d < min_depth) / total * 100) for d in depths])
            labels.append(label)

        self.visualizer.plot_grouped_bar_chart(
            x=depth_labels, datasets=all_loss_pcts, labels=labels,
            title="Percentage of Scenarios Lost per Depth",
            xlabel="Depth", ylabel="Scenarios Lost (%)",
            filename=os.path.join(self.output_dir, "compare_" + PLOT_SCENARIO_DEPTH_LOSS),
        )
        self.visualizer.plot_grouped_bar_chart(
            x=depth_labels, datasets=all_affect_pcts, labels=labels,
            title="Percentage of Scenarios Affected per Depth",
            xlabel="Depth", ylabel="Scenarios Affected (%)",
            filename=os.path.join(self.output_dir, "compare_" + PLOT_SCENARIO_DEPTH_AFFECT),
        )
