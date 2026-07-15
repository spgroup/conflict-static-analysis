import matplotlib.pyplot as plt
import seaborn as sns

from constants import COL_DEPTH


class LineChartMixin:
    def plot_jar_metrics(self, df, title, filename):
        plt.figure(figsize=(10, 6))
        df_melted = df.reset_index().melt(id_vars="index", var_name="Metric", value_name="Value")
        sns.barplot(data=df_melted, x="index", y="Value", hue="Metric", palette="tab10")
        plt.title(title)
        plt.xlabel("JAR")
        plt.xticks(rotation=45)
        self._save_plot(filename)

    def plot_conflict_depth_lines(self, df, title, filename):
        plt.figure(figsize=(15, 10))
        depths = df[COL_DEPTH].values
        for i in range(len(depths)):
            plt.plot([0, depths[i]], [i, i], "-", linewidth=1.5)
        plt.title(title)
        plt.xlabel("Depth")
        plt.ylabel("Conflict Index")
        plt.grid(True, linestyle="--", alpha=0.7)
        self._save_plot(filename)

    def plot_conflict_depth_lines_compare(self, df1, df2, title, label1, label2, filename):
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(20, 10))
        for df, ax, label in [(df1, ax1, label1), (df2, ax2, label2)]:
            depths = df[COL_DEPTH].values
            for i in range(len(depths)):
                ax.plot([0, depths[i]], [i, i], "-", linewidth=1.5)
            ax.set_title(f"{title} - {label}")
            ax.set_xlabel("Depth")
            ax.set_ylabel("Conflict Index")
            ax.grid(True, linestyle="--", alpha=0.7)
        self._save_plot(filename)
