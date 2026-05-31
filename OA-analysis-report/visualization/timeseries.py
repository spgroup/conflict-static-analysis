import matplotlib.pyplot as plt
import numpy as np
import seaborn as sns


class TimeseriesMixin:
    def plot_timeseries(self, df, x_col, y_cols, title, xlabel, ylabel, filename):
        plt.figure(figsize=(14, 6))
        for y_col in y_cols:
            plt.plot(df[x_col], df[y_col], label=y_col, linewidth=2, marker="o", markersize=4)
        plt.title(title)
        plt.xlabel(xlabel)
        plt.ylabel(ylabel)
        plt.legend(loc="best")
        plt.grid(True, linestyle="--", alpha=0.7)
        self._save_plot(filename)

    def plot_grouped_timeseries(self, series_list, labels, title, xlabel, ylabel, filename):
        """Multiple timeseries on one axes; x-axis normalised to 0–100%."""
        palette = sns.color_palette("tab10", len(series_list))
        plt.figure(figsize=(14, 6))
        for (x_data, y_data), label, color in zip(series_list, labels, palette):
            x_norm = self._normalize_x(x_data)
            plt.plot(x_norm, y_data, linewidth=2, marker="o", markersize=3,
                     label=label, color=color, alpha=0.8)
        plt.title(title)
        plt.xlabel("Execution Progress (%)")
        plt.ylabel(ylabel)
        plt.legend(loc="best")
        plt.grid(True, linestyle="--", alpha=0.7)
        self._save_plot(filename)

    def plot_grouped_smoothed_timeseries(self, series_list, labels, title, xlabel, ylabel,
                                         filename, include_raw=False):
        """Smoothed curves for multiple datasets; x-axis normalised to 0–100%."""
        palette = sns.color_palette("tab10", len(series_list))
        plt.figure(figsize=(14, 6))
        for (x_data, y_data), label, color in zip(series_list, labels, palette):
            x_norm = self._normalize_x(x_data)
            if include_raw:
                plt.plot(x_norm, y_data, "o", markersize=3, color=color, alpha=0.25)
            if len(x_norm) > 3:
                x_smooth, y_smooth = self._compute_smoothed_curve(x_norm, y_data)
                if x_smooth is not None:
                    plt.plot(x_smooth, y_smooth, "-", linewidth=2.5, label=label, color=color)
                else:
                    plt.plot(x_norm, y_data, "-", linewidth=2, label=label, color=color)
            else:
                plt.plot(x_norm, y_data, "-", linewidth=2, label=label, color=color)
        plt.title(title)
        plt.xlabel("Execution Progress (%)")
        plt.ylabel(ylabel)
        plt.legend(loc="best")
        plt.grid(True, linestyle="--", alpha=0.7)
        self._save_plot(filename)

    def plot_normalized_smoothed_timeseries(self, df, x_col, y_col, title, xlabel, ylabel, filename):
        """Original data with overlaid smoothed curve (no x-normalisation)."""
        plt.figure(figsize=(14, 6))
        x_data = df[x_col].values
        y_data = df[y_col].values
        plt.plot(x_data, y_data, "o-", label="Original Data", linewidth=2, markersize=4, alpha=0.6)
        if len(x_data) > 3:
            x_smooth, y_smooth = self._compute_smoothed_curve(x_data, y_data)
            if x_smooth is not None:
                plt.plot(x_smooth, y_smooth, "-", label="Smoothed Curve", linewidth=2.5, color="red")
        plt.title(title)
        plt.xlabel(xlabel)
        plt.ylabel(ylabel)
        plt.legend(loc="best")
        plt.grid(True, linestyle="--", alpha=0.7)
        self._save_plot(filename)

    def plot_smoothed_only_timeseries(self, df, x_col, y_col, title, xlabel, ylabel, filename):
        """Only the smoothed curve, without original data."""
        plt.figure(figsize=(14, 6))
        x_data = df[x_col].values
        y_data = df[y_col].values
        if len(x_data) > 3:
            x_smooth, y_smooth = self._compute_smoothed_curve(x_data, y_data)
            if x_smooth is not None:
                plt.plot(x_smooth, y_smooth, "-", label="Smoothed Curve", linewidth=3, color="red")
        plt.title(title)
        plt.xlabel(xlabel)
        plt.ylabel(ylabel)
        plt.legend(loc="best")
        plt.grid(True, linestyle="--", alpha=0.7)
        self._save_plot(filename)

    @staticmethod
    def _compute_smoothed_curve(x_data, y_data):
        """Return (x_smooth, y_smooth) using spline or polynomial fallback; None on failure."""
        try:
            from scipy.interpolate import UnivariateSpline
            spline = UnivariateSpline(x_data, y_data, k=min(3, len(x_data) - 1), s=None)
            x_smooth = np.linspace(x_data.min(), x_data.max(), 300)
            return x_smooth, spline(x_smooth)
        except Exception:
            try:
                degree = min(3, len(x_data) - 1)
                coeffs = np.polyfit(x_data, y_data, degree)
                x_smooth = np.linspace(x_data.min(), x_data.max(), 300)
                return x_smooth, np.poly1d(coeffs)(x_smooth)
            except Exception as e:
                print(f"Warning: Could not apply smoothing: {e}")
                return None, None
