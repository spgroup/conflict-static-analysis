import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
import numpy as np
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
        plt.xlabel(xlabel)
        plt.ylabel('Frequency')
        plt.title(title)
        plt.savefig(filename)
        plt.close()

    def plot_pie_chart(self, data, title, filename):
        plt.figure(figsize=(12, 6))

        colors = data.get('colors', sns.color_palette("husl", len(data['labels'])))
        plt.pie(data['values'], labels=[f"{label}\n({int(count)} - {pct:.1f}%)" 
                for label, count, pct in zip(data['labels'], 
                                            data['values'], 
                                            data['percentages'])],
                autopct='',
                startangle=140,
                colors=colors
        )
        plt.axis('equal')
        plt.title(title, pad=20)
        plt.subplots_adjust(top=0.85)
        plt.savefig(filename)
        plt.close()

    def plot_bar_chart(self, x, y, title, xlabel, ylabel, filename):
        plt.figure(figsize=(12, 6))
        plt.bar(x, y)
        plt.title(title)
        plt.xlabel(xlabel)
        plt.ylabel(ylabel)
        
        plt.xticks(x)
        
        for i, v in enumerate(y):
            plt.text(x[i], v, f'{v:.1f}%', ha='center', va='bottom')
        
        plt.grid(True, axis='y', linestyle='--', alpha=0.7)
        plt.tight_layout()
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

    def plot_bar_chart_compare(self, x, y1, y2, label1, label2, title, xlabel, ylabel, filename):
        """Plot two bar charts side by side for comparison"""
        fig, ax = plt.subplots(figsize=(14, 6))
        
        x_arr = np.array(x)
        width = 0.35
        
        ax.bar(x_arr - width/2, y1, width, label=label1, color='steelblue')
        ax.bar(x_arr + width/2, y2, width, label=label2, color='coral')
        
        ax.set_title(title)
        ax.set_xlabel(xlabel)
        ax.set_ylabel(ylabel)
        ax.set_xticks(x_arr)
        ax.legend()
        ax.grid(True, axis='y', linestyle='--', alpha=0.7)
        
        plt.tight_layout()
        plt.savefig(filename)
        plt.close()

    def plot_pie_chart_compare(self, data1, data2, title1, title2, filename):
        """Plot two pie charts side by side"""
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 6))
        
        self._plot_pie_on_axis(ax1, data1, title1)
        self._plot_pie_on_axis(ax2, data2, title2)
        
        plt.tight_layout()
        plt.savefig(filename)
        plt.close()

    def _plot_pie_on_axis(self, ax, data, title):
        """Helper method to plot a single pie chart on given axis"""
        colors = data.get('colors', None)
        
        ax.pie(data['values'], labels=[f"{label}\n({int(count)} - {pct:.1f}%)" 
                for label, count, pct in zip(data['labels'], 
                                            data['values'], 
                                            data['percentages'])],
                autopct='',
                startangle=140,
                colors=colors)
        ax.set_title(title)
        ax.axis('equal')