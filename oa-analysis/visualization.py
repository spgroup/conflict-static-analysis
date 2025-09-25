import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
from constants import COL_DEPTH

class Visualizer:
    def __init__(self):
        self.setup_plot_style()

    @staticmethod
    def setup_plot_style():
        sns.set_palette("husl")

    def plot_histogram(self, data, bins, title, xlabel, filename):
        plt.figure(figsize=(10, 6))
        plt.hist(data, bins=bins)
        plt.title(title)
        plt.xlabel(xlabel)
        plt.ylabel('Frequency')
        plt.savefig(filename)
        plt.close()

    def plot_scenario_metric(self, df, metric_col, agg_func, title, filename):
        plt.figure(figsize=(12, 6))
        scenario_stats = df.groupby('scenario_index')[metric_col].agg(agg_func)
        plt.hist(scenario_stats.values, bins='auto', edgecolor='black')
        plt.title(title)
        plt.xlabel(metric_col)
        plt.ylabel('Frequency')
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
        
    def plot_conflict_depth_lines(self, df, title, filename):
        plt.figure(figsize=(15, 10))
        
        # Get unique depths and sort conflicts by depth
        depths = df[COL_DEPTH].values
        num_conflicts = len(depths)
        
        # Plot horizontal lines for each conflict
        for i in range(num_conflicts):
            plt.plot([0, depths[i]], [i, i], '-', linewidth=1.5)
            
        plt.title(title)
        plt.xlabel('Depth')
        plt.ylabel('Conflict Index')
        plt.grid(True, linestyle='--', alpha=0.7)
        plt.tight_layout()
        plt.savefig(filename)
        plt.close()