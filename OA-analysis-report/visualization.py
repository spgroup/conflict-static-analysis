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

    def plot_histogram_compare(self, data1, data2, bins, title, xlabel, label1, label2, filename):
        """Plot two histograms side by side for comparison"""
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 6))
        
        ax1.hist(data1, bins=bins, color='steelblue', alpha=0.7, edgecolor='black')
        ax1.set_xlabel(xlabel)
        ax1.set_ylabel('Frequency')
        ax1.set_title(f'{title} - {label1}')
        
        ax2.hist(data2, bins=bins, color='coral', alpha=0.7, edgecolor='black')
        ax2.set_xlabel(xlabel)
        ax2.set_ylabel('Frequency')
        ax2.set_title(f'{title} - {label2}')
        
        plt.tight_layout()
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

    def plot_scenario_metric_compare(self, df1, df2, metric_col, agg_func, title, label1, label2, filename):
        """Plot scenario metrics as side-by-side bars with bucketed ranges"""
        fig, ax = plt.subplots(figsize=(14, 6))
        
        stats1 = df1.groupby('scenario_index')[metric_col].agg(agg_func)
        stats2 = df2.groupby('scenario_index')[metric_col].agg(agg_func)
        
        # Create buckets based on data range
        max_val = max(stats1.max(), stats2.max())
        buckets = self._create_buckets(max_val)
        bucket_labels = self._get_bucket_labels(buckets)
        
        # Count frequencies in each bucket
        counts1 = self._count_in_buckets(stats1.values, buckets)
        counts2 = self._count_in_buckets(stats2.values, buckets)
        
        # Create x positions for bars
        x_pos = np.arange(len(bucket_labels))
        width = 0.35
        
        ax.bar(x_pos - width/2, counts1, width, label=label1, color='steelblue', alpha=0.8)
        ax.bar(x_pos + width/2, counts2, width, label=label2, color='coral', alpha=0.8)
        
        ax.set_title(title)
        ax.set_xlabel(metric_col)
        ax.set_ylabel('Frequency')
        ax.set_xticks(x_pos)
        ax.set_xticklabels(bucket_labels, rotation=45, ha='right')
        ax.legend()
        ax.grid(True, axis='y', linestyle='--', alpha=0.7)
        
        plt.tight_layout()
        plt.savefig(filename)
        plt.close()

    def _create_buckets(self, max_val):
        """Create intelligent buckets based on max value"""
        if max_val <= 5:
            return [0, 1, 2, 3, 4, 5, float('inf')]
        elif max_val <= 10:
            return [0, 1, 3, 5, 8, 10, float('inf')]
        elif max_val <= 20:
            return [0, 2, 5, 10, 15, 20, float('inf')]
        elif max_val <= 50:
            return [0, 5, 10, 20, 30, 50, float('inf')]
        else:
            return [0, 10, 20, 50, 100, 200, float('inf')]

    def _get_bucket_labels(self, buckets):
        """Generate readable labels for buckets"""
        labels = []
        for i in range(len(buckets) - 1):
            start = int(buckets[i])
            end = int(buckets[i + 1]) if buckets[i + 1] != float('inf') else None
            
            if end is None:
                labels.append(f'>{start}')
            elif start == 0:
                labels.append(f'0-{end}')
            else:
                labels.append(f'{start}-{end}')
        return labels

    def _count_in_buckets(self, values, buckets):
        """Count values that fall into each bucket"""
        counts = []
        for i in range(len(buckets) - 1):
            if buckets[i + 1] == float('inf'):
                count = sum(values > buckets[i])
            else:
                count = sum((values > buckets[i]) & (values <= buckets[i + 1]))
            counts.append(count)
        return counts

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

    def plot_conflict_depth_lines_compare(self, df1, df2, title, label1, label2, filename):
        """Plot conflict depth lines side by side for comparison"""
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(20, 10))
        
        for df, ax, label in [(df1, ax1, label1), (df2, ax2, label2)]:
            depths = df[COL_DEPTH].values
            num_conflicts = len(depths)
            
            for i in range(num_conflicts):
                ax.plot([0, depths[i]], [i, i], '-', linewidth=1.5)
            
            ax.set_title(f'{title} - {label}')
            ax.set_xlabel('Depth')
            ax.set_ylabel('Conflict Index')
            ax.grid(True, linestyle='--', alpha=0.7)
        
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

    def plot_distribution_buckets_compare(self, data1, data2, title, label1, label2, filename):
        """Plot bucketed distribution comparison for percentages (0-100) with side-by-side bars"""
        fig, ax = plt.subplots(figsize=(14, 6))
        
        # Create percentage buckets: 0-20, 20-40, 40-60, 60-80, 80-100
        buckets = [0, 20, 40, 60, 80, 100]
        bucket_labels = ['0-20', '20-40', '40-60', '60-80', '80-100']
        
        # Count values in each bucket
        counts1 = self._count_in_percentage_buckets(data1, buckets)
        counts2 = self._count_in_percentage_buckets(data2, buckets)
        
        x = np.arange(len(bucket_labels))
        width = 0.35
        
        ax.bar(x - width/2, counts1, width, label=label1, color='steelblue')
        ax.bar(x + width/2, counts2, width, label=label2, color='coral')
        
        ax.set_title(title)
        ax.set_xlabel('Percentage Range')
        ax.set_ylabel('Count')
        ax.set_xticks(x)
        ax.set_xticklabels(bucket_labels)
        ax.legend()
        ax.grid(True, axis='y', linestyle='--', alpha=0.7)
        
        plt.tight_layout()
        plt.savefig(filename)
        plt.close()

    def _count_in_percentage_buckets(self, values, buckets):
        """Count percentage values that fall into each bucket (0-100 range)"""
        counts = []
        for i in range(len(buckets) - 1):
            # Last bucket includes values equal to upper bound
            if i == len(buckets) - 2:
                count = sum((values >= buckets[i]) & (values <= buckets[i + 1]))
            else:
                count = sum((values >= buckets[i]) & (values < buckets[i + 1]))
            counts.append(count)
        return counts

    def plot_conflicts_per_scenario_compare(self, data1, data2, title, label1, label2, filename):
        """Plot bucketed conflicts per scenario comparison with buckets: 1-2, 2-5, 5-10, >10"""
        fig, ax = plt.subplots(figsize=(12, 6))
        
        # Create buckets: 1-2, 2-5, 5-10, >10
        buckets = [0, 2, 5, 10, float('inf')]
        bucket_labels = ['1-2', '2-5', '5-10', '>10']
        
        # Count values in each bucket
        counts1 = self._count_in_conflict_buckets(data1, buckets)
        counts2 = self._count_in_conflict_buckets(data2, buckets)
        
        # Convert to percentages
        total1 = len(data1)
        total2 = len(data2)
        percentages1 = [(count / total1 * 100) if total1 > 0 else 0 for count in counts1]
        percentages2 = [(count / total2 * 100) if total2 > 0 else 0 for count in counts2]
        
        x = np.arange(len(bucket_labels))
        width = 0.35
        
        ax.bar(x - width/2, percentages1, width, label=label1, color='steelblue')
        ax.bar(x + width/2, percentages2, width, label=label2, color='coral')
        
        ax.set_title(title)
        ax.set_xlabel('Number of Conflicts per Scenario')
        ax.set_ylabel('Percentage (%)')
        ax.set_xticks(x)
        ax.set_xticklabels(bucket_labels)
        ax.legend()
        ax.grid(True, axis='y', linestyle='--', alpha=0.7)
        
        plt.tight_layout()
        plt.savefig(filename)
        plt.close()

    def _count_in_conflict_buckets(self, values, buckets):
        """Count conflict values that fall into each bucket"""
        counts = []
        for i in range(len(buckets) - 1):
            if buckets[i + 1] == float('inf'):
                count = sum(values > buckets[i])
            else:
                count = sum((values > buckets[i]) & (values <= buckets[i + 1]))
            counts.append(count)
        return counts

    def plot_pie_chart_compare(self, data1, data2, title1, title2, filename):
        """Plot two pie charts side by side"""
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 6))
        
        # Use steelblue for first dataset, coral for second
        data1_colored = data1.copy()
        data2_colored = data2.copy()
        
        # If colors not in data, use default palette for each dataset
        if 'colors' not in data1_colored or data1_colored['colors'] is None:
            data1_colored['colors'] = ['steelblue'] * len(data1_colored['labels'])
        if 'colors' not in data2_colored or data2_colored['colors'] is None:
            data2_colored['colors'] = ['coral'] * len(data2_colored['labels'])
        
        self._plot_pie_on_axis(ax1, data1_colored, title1)
        self._plot_pie_on_axis(ax2, data2_colored, title2)
        
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