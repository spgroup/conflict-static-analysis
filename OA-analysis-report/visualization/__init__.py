import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns

from .bar_charts import BarChartMixin
from .histograms import HistogramMixin
from .line_charts import LineChartMixin
from .pie_charts import PieChartMixin
from .timeseries import TimeseriesMixin


class Visualizer(BarChartMixin, HistogramMixin, PieChartMixin, TimeseriesMixin, LineChartMixin):
    def __init__(self):
        self.setup_plot_style()

    @staticmethod
    def setup_plot_style():
        sns.set_palette("tab10")

    @staticmethod
    def _save_plot(filename):
        plt.tight_layout()
        plt.savefig(filename)
        plt.close()

    @staticmethod
    def _prep_comparison_df(x_data, y_data1, y_data2, label1, label2, x_label, y_label):
        data = []
        for x_val, y_val in zip(x_data, y_data1):
            data.append({x_label: str(x_val), y_label: y_val, "Group": label1})
        for x_val, y_val in zip(x_data, y_data2):
            data.append({x_label: str(x_val), y_label: y_val, "Group": label2})
        return pd.DataFrame(data)

    @staticmethod
    def _create_buckets(max_val):
        if max_val <= 5:
            return [0, 1, 2, 3, 4, 5, float("inf")]
        elif max_val <= 10:
            return [0, 1, 3, 5, 8, 10, float("inf")]
        elif max_val <= 20:
            return [0, 2, 5, 10, 15, 20, float("inf")]
        elif max_val <= 50:
            return [0, 5, 10, 20, 30, 50, float("inf")]
        else:
            return [0, 10, 20, 50, 100, 200, float("inf")]

    @staticmethod
    def _get_bucket_labels(buckets):
        labels = []
        for i in range(len(buckets) - 1):
            start = int(buckets[i])
            end = int(buckets[i + 1]) if buckets[i + 1] != float("inf") else None
            if end is None:
                labels.append(f">{start}")
            elif start == 0:
                labels.append(f"0-{end}")
            else:
                labels.append(f"{start}-{end}")
        return labels

    @staticmethod
    def _count_in_buckets(values, buckets):
        counts = []
        for i in range(len(buckets) - 1):
            if buckets[i + 1] == float("inf"):
                count = sum(values > buckets[i])
            else:
                count = sum((values > buckets[i]) & (values <= buckets[i + 1]))
            counts.append(count)
        return counts

    @staticmethod
    def _count_in_percentage_buckets(values, buckets):
        counts = []
        for i in range(len(buckets) - 1):
            if i == len(buckets) - 2:
                count = sum((values >= buckets[i]) & (values <= buckets[i + 1]))
            else:
                count = sum((values >= buckets[i]) & (values < buckets[i + 1]))
            counts.append(count)
        return counts

    @staticmethod
    def _normalize_x(x_data):
        """Normalise x values to 0–100% so datasets with different durations are comparable."""
        x = np.asarray(x_data, dtype=float)
        x_min, x_max = x.min(), x.max()
        if x_max == x_min:
            return np.zeros_like(x)
        return (x - x_min) / (x_max - x_min) * 100.0
