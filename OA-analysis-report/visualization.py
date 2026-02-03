import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
from constants import COL_DEPTH

class Visualizer:
    def __init__(self):
        self.setup_plot_style()

    @staticmethod
    def setup_plot_style():
        sns.set_palette('tab10')

    @staticmethod
    def _save_plot(filename):
        """Save and close figure"""
        plt.tight_layout()
        plt.savefig(filename)
        plt.close()

    @staticmethod
    def _prep_comparison_df(x_data, y_data1, y_data2, label1, label2, x_label, y_label):
        """Prepare dataframe for comparison plots"""
        data = []
        for x_val, y_val in zip(x_data, y_data1):
            data.append({x_label: str(x_val), y_label: y_val, 'Group': label1})
        for x_val, y_val in zip(x_data, y_data2):
            data.append({x_label: str(x_val), y_label: y_val, 'Group': label2})
        return pd.DataFrame(data)

    def plot_histogram(self, data, bins, title, xlabel, filename):
        plt.figure(figsize=(10, 6))
        sns.histplot(data=pd.DataFrame({xlabel: data}), x=xlabel, bins=bins, kde=True)
        plt.ylabel('Frequency')
        plt.title(title)
        self._save_plot(filename)

    def plot_histogram_compare(self, data1, data2, bins, title, xlabel, label1, label2, filename):
        """Plot two histograms side by side for comparison"""
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 6))
        
        sns.histplot(data=pd.DataFrame({xlabel: data1}), x=xlabel, bins=bins, kde=True, ax=ax1)
        ax1.set_ylabel('Frequency')
        ax1.set_title(f'{title} - {label1}')
        
        sns.histplot(data=pd.DataFrame({xlabel: data2}), x=xlabel, bins=bins, kde=True, ax=ax2)
        ax2.set_ylabel('Frequency')
        ax2.set_title(f'{title} - {label2}')
        
        self._save_plot(filename)

    def plot_pie_chart(self, data, title, filename):
        plt.figure(figsize=(12, 6))
        colors = sns.color_palette('tab10', len(data['labels']))
        plt.pie(data['values'], labels=[f"{label}\n({int(count)} - {pct:.1f}%)" 
                for label, count, pct in zip(data['labels'], data['values'], data['percentages'])],
                autopct='', startangle=140, colors=colors)
        plt.axis('equal')
        plt.title(title, pad=20)
        plt.subplots_adjust(top=0.85)
        self._save_plot(filename)

    def plot_bar_chart(self, x, y, title, xlabel, ylabel, filename):
        plt.figure(figsize=(12, 6))
        ax = sns.barplot(data=pd.DataFrame({'x': x, 'y': y}), x='x', y='y', color=sns.color_palette('tab10')[0])
        plt.title(title)
        plt.xlabel(xlabel)
        plt.ylabel(ylabel)
        plt.xticks(range(len(x)))
        
        for i, v in enumerate(y):
            ax.text(i, v, f'{v:.1f}%', ha='center', va='bottom')
        
        ax.grid(True, axis='y', linestyle='--', alpha=0.7)
        self._save_plot(filename)

    def plot_scenario_metric(self, df, metric_col, agg_func, title, filename):
        plt.figure(figsize=(12, 6))
        stats = df.groupby('scenario_index')[metric_col].agg(agg_func)
        sns.histplot(data=pd.DataFrame({metric_col: stats.values}), x=metric_col, bins='auto', kde=True)
        plt.title(title)
        plt.ylabel('Frequency')
        self._save_plot(filename)

    def plot_scenario_metric_compare(self, df1, df2, metric_col, agg_func, title, label1, label2, filename):
        """Plot scenario metrics as side-by-side bars with bucketed ranges"""
        fig, ax = plt.subplots(figsize=(14, 6))
        
        stats1 = df1.groupby('scenario_index')[metric_col].agg(agg_func)
        stats2 = df2.groupby('scenario_index')[metric_col].agg(agg_func)
        
        max_val = max(stats1.max(), stats2.max())
        buckets = self._create_buckets(max_val)
        bucket_labels = self._get_bucket_labels(buckets)
        
        counts1 = self._count_in_buckets(stats1.values, buckets)
        counts2 = self._count_in_buckets(stats2.values, buckets)
        
        data = []
        for label, count in zip(bucket_labels, counts1):
            data.append({'Bucket': label, 'Frequency': count, 'Group': label1})
        for label, count in zip(bucket_labels, counts2):
            data.append({'Bucket': label, 'Frequency': count, 'Group': label2})
        
        sns.barplot(data=pd.DataFrame(data), x='Bucket', y='Frequency', hue='Group', ax=ax, palette='tab10')
        ax.set_title(title)
        ax.set_xlabel(metric_col)
        ax.set_ylabel('Frequency')
        ax.set_xticklabels(ax.get_xticklabels(), rotation=45, ha='right')
        ax.grid(True, axis='y', linestyle='--', alpha=0.7)
        self._save_plot(filename)

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
        df_melted = df.reset_index().melt(id_vars='index', var_name='Metric', value_name='Value')
        sns.barplot(data=df_melted, x='index', y='Value', hue='Metric', palette='tab10')
        plt.title(title)
        plt.xlabel('JAR')
        plt.xticks(rotation=45)
        self._save_plot(filename)
        
    def plot_conflict_depth_lines(self, df, title, filename):
        plt.figure(figsize=(15, 10))
        depths = df[COL_DEPTH].values
        
        for i in range(len(depths)):
            plt.plot([0, depths[i]], [i, i], '-', linewidth=1.5)
            
        plt.title(title)
        plt.xlabel('Depth')
        plt.ylabel('Conflict Index')
        plt.grid(True, linestyle='--', alpha=0.7)
        self._save_plot(filename)

    def plot_conflict_depth_lines_compare(self, df1, df2, title, label1, label2, filename):
        """Plot conflict depth lines side by side for comparison"""
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(20, 10))
        
        for df, ax, label in [(df1, ax1, label1), (df2, ax2, label2)]:
            depths = df[COL_DEPTH].values
            for i in range(len(depths)):
                ax.plot([0, depths[i]], [i, i], '-', linewidth=1.5)
            
            ax.set_title(f'{title} - {label}')
            ax.set_xlabel('Depth')
            ax.set_ylabel('Conflict Index')
            ax.grid(True, linestyle='--', alpha=0.7)
        
        self._save_plot(filename)

    def plot_bar_chart_compare(self, x, y1, y2, label1, label2, title, xlabel, ylabel, filename):
        """Plot two bar charts side by side for comparison"""
        fig, ax = plt.subplots(figsize=(14, 6))
        plot_df = self._prep_comparison_df(x, y1, y2, label1, label2, xlabel, ylabel)
        sns.barplot(data=plot_df, x=xlabel, y=ylabel, hue='Group', ax=ax, palette='tab10')
        
        ax.set_title(title)
        ax.grid(True, axis='y', linestyle='--', alpha=0.7)
        self._save_plot(filename)

    def plot_distribution_buckets_compare(self, data1, data2, title, label1, label2, filename):
        """Plot bucketed distribution comparison for percentages (0-100) with side-by-side bars"""
        fig, ax = plt.subplots(figsize=(14, 6))
        
        buckets = [0, 20, 40, 60, 80, 100]
        bucket_labels = ['0-20', '20-40', '40-60', '60-80', '80-100']
        
        counts1 = self._count_in_percentage_buckets(data1, buckets)
        counts2 = self._count_in_percentage_buckets(data2, buckets)
        
        data = []
        for label, count in zip(bucket_labels, counts1):
            data.append({'Percentage Range': label, 'Count': count, 'Group': label1})
        for label, count in zip(bucket_labels, counts2):
            data.append({'Percentage Range': label, 'Count': count, 'Group': label2})
        
        sns.barplot(data=pd.DataFrame(data), x='Percentage Range', y='Count', hue='Group', ax=ax, palette='tab10')
        ax.set_title(title)
        ax.grid(True, axis='y', linestyle='--', alpha=0.7)
        self._save_plot(filename)

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
        
        buckets = [0, 2, 5, 10, float('inf')]
        bucket_labels = ['1-2', '2-5', '5-10', '>10']
        
        counts1 = self._count_in_conflict_buckets(data1, buckets)
        counts2 = self._count_in_conflict_buckets(data2, buckets)
        
        total1, total2 = len(data1), len(data2)
        pct1 = [(count / total1 * 100) if total1 > 0 else 0 for count in counts1]
        pct2 = [(count / total2 * 100) if total2 > 0 else 0 for count in counts2]
        
        data = []
        for label, pct in zip(bucket_labels, pct1):
            data.append({'Bucket': label, 'Percentage': pct, 'Group': label1})
        for label, pct in zip(bucket_labels, pct2):
            data.append({'Bucket': label, 'Percentage': pct, 'Group': label2})
        
        sns.barplot(data=pd.DataFrame(data), x='Bucket', y='Percentage', hue='Group', ax=ax, palette='tab10')
        ax.set_title(title)
        ax.set_xlabel('Number of Conflicts per Scenario')
        ax.set_ylabel('Percentage (%)')
        ax.grid(True, axis='y', linestyle='--', alpha=0.7)
        self._save_plot(filename)

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
        
        for ax, data, title in [(ax1, data1, title1), (ax2, data2, title2)]:
            colors = sns.color_palette('tab10', len(data['labels']))
            ax.pie(data['values'], labels=[f"{label}\n({int(count)} - {pct:.1f}%)" 
                    for label, count, pct in zip(data['labels'], data['values'], data['percentages'])],
                    autopct='', startangle=140, colors=colors)
            ax.set_title(title)
            ax.axis('equal')
        
        self._save_plot(filename)