import matplotlib.pyplot as plt
import seaborn as sns


class PieChartMixin:
    def plot_pie_chart(self, data, title, filename):
        plt.figure(figsize=(12, 6))
        colors = sns.color_palette("tab10", len(data["labels"]))
        plt.pie(
            data["values"],
            labels=[
                f"{label}\n({int(count)} - {pct:.1f}%)"
                for label, count, pct in zip(data["labels"], data["values"], data["percentages"])
            ],
            autopct="",
            startangle=140,
            colors=colors,
        )
        plt.axis("equal")
        plt.title(title, pad=20)
        plt.subplots_adjust(top=0.85)
        self._save_plot(filename)

    def plot_pie_chart_compare(self, data1, data2, title1, title2, filename):
        """Two pie charts side by side."""
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 6))
        for ax, data, title in [(ax1, data1, title1), (ax2, data2, title2)]:
            colors = sns.color_palette("tab10", len(data["labels"]))
            ax.pie(
                data["values"],
                labels=[
                    f"{label}\n({int(count)} - {pct:.1f}%)"
                    for label, count, pct in zip(data["labels"], data["values"], data["percentages"])
                ],
                autopct="",
                startangle=140,
                colors=colors,
            )
            ax.set_title(title)
            ax.axis("equal")
        self._save_plot(filename)
