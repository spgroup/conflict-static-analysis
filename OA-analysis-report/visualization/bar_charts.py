import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns


class BarChartMixin:
    def plot_bar_chart(self, x, y, title, xlabel, ylabel, filename):
        plt.figure(figsize=(12, 6))
        ax = sns.barplot(
            data=pd.DataFrame({"x": x, "y": y}),
            x="x", y="y",
            color=sns.color_palette("tab10")[0],
        )
        plt.title(title)
        plt.xlabel(xlabel)
        plt.ylabel(ylabel)
        plt.xticks(range(len(x)))
        for i, v in enumerate(y):
            label_text = f"{v:.1f}%" if "%" in ylabel else f"{int(v)}"
            ax.text(i, v, label_text, ha="center", va="bottom")
        ax.grid(True, axis="y", linestyle="--", alpha=0.7)
        self._save_plot(filename)

    def plot_grouped_bar_chart(self, x, datasets, labels, title, xlabel, ylabel, filename):
        """Grouped bar chart – one cluster of bars per x-category, one bar per dataset."""
        n_groups = len(x)
        n_datasets = len(datasets)
        bar_width = 0.8 / n_datasets
        palette = sns.color_palette("tab10", n_datasets)

        fig, ax = plt.subplots(figsize=(14, 6))
        x_indices = np.arange(n_groups)

        for i, (data, label) in enumerate(zip(datasets, labels)):
            offset = (i - (n_datasets - 1) / 2) * bar_width
            bars = ax.bar(
                x_indices + offset, data, bar_width,
                label=label, color=palette[i], alpha=0.85,
            )
            for bar, val in zip(bars, data):
                if val > 0:
                    ax.text(
                        bar.get_x() + bar.get_width() / 2,
                        bar.get_height(),
                        f"{val:.1f}%",
                        ha="center", va="bottom", fontsize=7,
                    )

        ax.set_title(title)
        ax.set_xlabel(xlabel)
        ax.set_ylabel(ylabel)
        ax.set_xticks(x_indices)
        ax.set_xticklabels(x)
        ax.legend(loc="best")
        ax.grid(True, axis="y", linestyle="--", alpha=0.7)
        self._save_plot(filename)

    def plot_bar_chart_compare(self, x, y1, y2, label1, label2, title, xlabel, ylabel, filename):
        """Side-by-side grouped bar chart comparing two datasets."""
        fig, ax = plt.subplots(figsize=(14, 6))
        n_groups = len(x)
        bar_width = 0.35
        x_indices = np.arange(n_groups)
        palette = sns.color_palette("tab10", 2)

        bars1 = ax.bar(x_indices - bar_width / 2, y1, bar_width, label=label1, color=palette[0], alpha=0.85)
        bars2 = ax.bar(x_indices + bar_width / 2, y2, bar_width, label=label2, color=palette[1], alpha=0.85)

        for bars in [bars1, bars2]:
            for bar in bars:
                height = bar.get_height()
                if height > 0:
                    label_text = f"{height:.1f}%" if "%" in ylabel else f"{int(height)}"
                    ax.text(
                        bar.get_x() + bar.get_width() / 2, height,
                        label_text, ha="center", va="bottom", fontsize=9,
                    )

        ax.set_xlabel(xlabel, fontsize=11)
        ax.set_ylabel(ylabel, fontsize=11)
        ax.set_title(title, fontsize=12, fontweight="bold")
        ax.set_xticks(x_indices)
        ax.set_xticklabels(x)
        ax.legend(loc="best", fontsize=10)
        ax.grid(True, axis="y", linestyle="--", alpha=0.7)
        self._save_plot(filename)

    def plot_grouped_bar_chart_compare(self, x, y1, y2, label1, label2, title, xlabel, ylabel, filename):
        """Alias for plot_bar_chart_compare (same signature and behaviour)."""
        self.plot_bar_chart_compare(x, y1, y2, label1, label2, title, xlabel, ylabel, filename)

    def plot_horizontal_bar_chart(self, labels, values, title, xlabel, ylabel, filename,
                                  plot_text_on_bars=False):
        plt.figure(figsize=(14, 6))
        palette = sns.color_palette("tab10", len(values))
        bars = plt.barh(labels, values, color=palette[:len(values)], alpha=0.85)
        if plot_text_on_bars:
            for bar, val in zip(bars, values):
                plt.text(val, bar.get_y() + bar.get_height() / 2,
                         f"{val:.1f}", ha="left", va="center", fontsize=10, fontweight="bold")
        plt.title(title, fontsize=12, fontweight="bold")
        plt.xlabel(xlabel, fontsize=11)
        plt.ylabel(ylabel, fontsize=11)
        plt.grid(True, axis="x", linestyle="--", alpha=0.7)
        self._save_plot(filename)
