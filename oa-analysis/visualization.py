import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd

class Visualizer:
    def __init__(self):
        self.setup_plot_style()

    @staticmethod
    def setup_plot_style():
        plt.style.use('seaborn')
        sns.set_palette("husl")

    def plot_histogram(self, data, bins, title, xlabel, ylabel, filename):
        plt.figure(figsize=(10, 6))
        plt.hist(data, bins=bins)
        plt.title(title)
        plt.xlabel(xlabel)
        plt.ylabel(ylabel)
        plt.savefig(filename)
        plt.close()

    def plot_distribution(self, data, title, xlabel, ylabel, filename):
        plt.figure(figsize=(10, 6))
        sns.histplot(data=data, kde=True)
        plt.title(title)
        plt.xlabel(xlabel)
        plt.ylabel(ylabel)
        plt.savefig(filename)
        plt.close()

    def plot_scenario_metric(self, df, metric_col, agg_func, title, filename):
        plt.figure(figsize=(12, 6))
        scenario_stats = df.groupby('scenario_index')[metric_col].agg(agg_func)
        scenario_stats.plot(kind='bar')
        plt.title(title)
        plt.xticks(rotation=45)
        plt.tight_layout()
        plt.savefig(filename)
        plt.close()

    def plot_jar_metrics(self, df, title, filename):
        plt.figure(figsize=(10, 6))
        df.plot(kind='bar')
        plt.title(title)
        plt.xticks(rotation=45)
        plt.tight_layout()
        plt.savefig(filename)
        plt.close()