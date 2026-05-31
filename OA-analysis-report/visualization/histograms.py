import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns


class HistogramMixin:
    def plot_histogram(self, data, bins, title, xlabel, filename):
        plt.figure(figsize=(10, 6))
        sns.histplot(data=pd.DataFrame({xlabel: data}), x=xlabel, bins=bins, kde=True)
        plt.ylabel("Frequency")
        plt.title(title)
        self._save_plot(filename)

    def plot_histogram_compare(self, data1, data2, bins, title, xlabel, label1, label2, filename):
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 6))
        for ax, data, label in [(ax1, data1, label1), (ax2, data2, label2)]:
            sns.histplot(data=pd.DataFrame({xlabel: data}), x=xlabel, bins=bins, kde=True, ax=ax)
            ax.set_ylabel("Frequency")
            ax.set_title(f"{title} - {label}")
        self._save_plot(filename)

    def plot_scenario_metric(self, df, metric_col, agg_func, title, filename):
        plt.figure(figsize=(12, 6))
        stats = df.groupby("scenario_index")[metric_col].agg(agg_func)
        sns.histplot(
            data=pd.DataFrame({metric_col: stats.values}),
            x=metric_col, bins="auto", kde=True,
        )
        plt.title(title)
        plt.ylabel("Frequency")
        self._save_plot(filename)

    def plot_scenario_metric_compare(self, df1, df2, metric_col, agg_func, title, label1, label2, filename):
        fig, ax = plt.subplots(figsize=(14, 6))
        stats1 = df1.groupby("scenario_index")[metric_col].agg(agg_func)
        stats2 = df2.groupby("scenario_index")[metric_col].agg(agg_func)
        max_val = max(stats1.max(), stats2.max())
        buckets = self._create_buckets(max_val)
        bucket_labels = self._get_bucket_labels(buckets)
        counts1 = self._count_in_buckets(stats1.values, buckets)
        counts2 = self._count_in_buckets(stats2.values, buckets)

        data = []
        for lbl, cnt in zip(bucket_labels, counts1):
            data.append({"Bucket": lbl, "Frequency": cnt, "Group": label1})
        for lbl, cnt in zip(bucket_labels, counts2):
            data.append({"Bucket": lbl, "Frequency": cnt, "Group": label2})

        sns.barplot(data=pd.DataFrame(data), x="Bucket", y="Frequency", hue="Group",
                    ax=ax, palette="tab10")
        ax.set_title(title)
        ax.set_xlabel(metric_col)
        ax.set_ylabel("Frequency")
        ax.set_xticklabels(ax.get_xticklabels(), rotation=45, ha="right")
        ax.grid(True, axis="y", linestyle="--", alpha=0.7)
        self._save_plot(filename)

    def plot_distribution_buckets_compare(self, data1, data2, title, label1, label2, filename):
        """Bucketed distribution comparison for percentages (0–100)."""
        fig, ax = plt.subplots(figsize=(14, 6))
        buckets = [0, 20, 40, 60, 80, 100]
        bucket_labels = ["0-20", "20-40", "40-60", "60-80", "80-100"]
        counts1 = self._count_in_percentage_buckets(data1, buckets)
        counts2 = self._count_in_percentage_buckets(data2, buckets)

        data = []
        for lbl, cnt in zip(bucket_labels, counts1):
            data.append({"Percentage Range": lbl, "Count": cnt, "Group": label1})
        for lbl, cnt in zip(bucket_labels, counts2):
            data.append({"Percentage Range": lbl, "Count": cnt, "Group": label2})

        sns.barplot(data=pd.DataFrame(data), x="Percentage Range", y="Count",
                    hue="Group", ax=ax, palette="tab10")
        ax.set_title(title)
        ax.grid(True, axis="y", linestyle="--", alpha=0.7)
        self._save_plot(filename)

    def plot_conflicts_per_scenario_compare(self, data1, data2, title, label1, label2, filename):
        """Bucketed conflicts-per-scenario comparison: 1-2, 2-5, 5-10, >10."""
        fig, ax = plt.subplots(figsize=(12, 6))
        buckets = [0, 2, 5, 10, float("inf")]
        bucket_labels = ["1-2", "2-5", "5-10", ">10"]
        counts1 = self._count_in_buckets(data1, buckets)
        counts2 = self._count_in_buckets(data2, buckets)
        total1, total2 = len(data1), len(data2)
        pct1 = [(c / total1 * 100) if total1 > 0 else 0 for c in counts1]
        pct2 = [(c / total2 * 100) if total2 > 0 else 0 for c in counts2]

        data = []
        for lbl, pct in zip(bucket_labels, pct1):
            data.append({"Bucket": lbl, "Percentage": pct, "Group": label1})
        for lbl, pct in zip(bucket_labels, pct2):
            data.append({"Bucket": lbl, "Percentage": pct, "Group": label2})

        sns.barplot(data=pd.DataFrame(data), x="Bucket", y="Percentage",
                    hue="Group", ax=ax, palette="tab10")
        ax.set_title(title)
        ax.set_xlabel("Number of Conflicts per Scenario")
        ax.set_ylabel("Percentage (%)")
        ax.grid(True, axis="y", linestyle="--", alpha=0.7)
        self._save_plot(filename)
