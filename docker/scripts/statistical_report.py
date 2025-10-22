import io
import matplotlib.pyplot as plt
import pandas as pd
import sys
from matplotlib.backends.backend_pdf import PdfPages
from scipy.stats import ttest_rel, shapiro, wilcoxon

# Redirect stdout to capture script output
buffer = io.StringIO()
sys.stdout = buffer

# Read the input CSV
df = pd.read_csv("merged_results.csv", delimiter=';')

# Ensure values are numeric
df["oa_inter_mean"] = pd.to_numeric(df["oa_inter_mean"], errors='coerce')
df["oa_inter_no_pa_mean"] = pd.to_numeric(df["oa_inter_no_pa_mean"], errors='coerce')
df["oa_inter_std"] = pd.to_numeric(df["oa_inter_std"], errors='coerce')
df["oa_inter_no_pa_std"] = pd.to_numeric(df["oa_inter_no_pa_std"], errors='coerce')

# Remove scenarios with missing values
df_clean = df.dropna(subset=["oa_inter_mean", "oa_inter_no_pa_mean", "oa_inter_std", "oa_inter_no_pa_std"])

# ----------------------------
# Shapiro-Wilk normality test
# ----------------------------
print("\n=== Normality Test (Shapiro-Wilk) ===")

shapiro_oa_inter = shapiro(df_clean["oa_inter_mean"])
shapiro_oa_inter_no_pa = shapiro(df_clean["oa_inter_no_pa_mean"])


def interpret_shapiro(name, result):
    stat, p = result
    print(f"{name}:")
    print(f"  W-statistic = {stat:.4f}, p-value = {p:.6f}")
    if p < 0.05:
        print("  → Reject the null hypothesis of normality (p < 0.05)")
    else:
        print("  → Do not reject the null hypothesis of normality (p ≥ 0.05)")


interpret_shapiro("OA Inter Mean", shapiro_oa_inter)
interpret_shapiro("OA Inter Without PA Mean", shapiro_oa_inter_no_pa)

# ----------------------------
# Wilcoxon signed-rank test
# ----------------------------
stat, p = wilcoxon(df_clean["oa_inter_mean"], df_clean["oa_inter_no_pa_mean"])

print("\n=== Wilcoxon Signed-Rank Test ===")
print(f"W-statistic: {stat}")
print(f"p-value: {p:.6f}")

if p < 0.05:
    print("→ Statistically significant difference (p < 0.05)")
else:
    print("→ No statistically significant difference (p ≥ 0.05)")

# ----------------------------
# Paired Student's t-test
# ----------------------------
print("\n=== Paired Student's t-test ===")
t_stat, p_value = ttest_rel(df_clean["oa_inter_mean"], df_clean["oa_inter_no_pa_mean"])
print(f"Number of compared scenarios: {len(df_clean)}")
print(f"t-statistic: {t_stat:.4f}")
print(f"p-value: {p_value:.6f}")

alpha = 0.05
if p_value < alpha:
    print("→ Statistically significant difference (p < 0.05)")
else:
    print("→ No statistically significant difference (p ≥ 0.05)")

# ----------------------------
# Check scenarios with std deviation > 10% of the mean
# ----------------------------
print("\n=== Scenarios with Standard Deviation > 10% of Mean ===")


def percent_std_over_mean(std_col, mean_col):
    if mean_col == 0:
        return 0.0
    return (std_col / mean_col) * 100


high_std_oa_inter = df_clean[df_clean["oa_inter_std"] > 0.1 * df_clean["oa_inter_mean"]]
high_std_oa_inter_no_pa = df_clean[df_clean["oa_inter_no_pa_std"] > 0.1 * df_clean["oa_inter_no_pa_mean"]]

print(f"OA Inter: {len(high_std_oa_inter)} scenarios with std > 10% of mean")
print(f"OA Inter Without PA: {len(high_std_oa_inter_no_pa)} scenarios with std > 10% of mean")

# Show names of affected methods
print("\n→ OA Inter scenarios with high std deviation:")
for _, row in high_std_oa_inter.iterrows():
    percent = percent_std_over_mean(row["oa_inter_std"], row["oa_inter_mean"])
    over_10 = percent - 10
    print(f"  - {row['project']}::{row['class']}::{row['method']} — {percent:.1f}% ({over_10:.1f}% above 10%)")

print("\n→ OA Inter Without PA scenarios with high std deviation:")
for _, row in high_std_oa_inter_no_pa.iterrows():
    percent = percent_std_over_mean(row["oa_inter_no_pa_std"], row["oa_inter_no_pa_mean"])
    over_10 = percent - 10
    print(f"  - {row['project']}::{row['class']}::{row['method']} — {percent:.1f}% ({over_10:.1f}% above 10%)")


def extreme_std_report(df, std_col, mean_col, label):
    row_max = df.nlargest(1, std_col).iloc[0]
    row_min = df.nsmallest(1, std_col).iloc[0]

    max_std = row_max[std_col]
    min_std = row_min[std_col]
    mean_max = row_max[mean_col]
    mean_min = row_min[mean_col]

    perc_max = (max_std / mean_max) * 100 if mean_max != 0 else 0
    perc_min = (min_std / mean_min) * 100 if mean_min != 0 else 0

    print(f"\n→ {label}")
    print(f"  Highest std deviation: {max_std:.2f} ({perc_max:.1f}% of mean)")
    print(f"    - Scenario: {row_max['project']}::{row_max['class']}::{row_max['method']}")
    print(f"    - Corresponding mean: {mean_max:.2f}")
    print(f"  Lowest std deviation: {min_std:.2f} ({perc_min:.1f}% of mean)")
    print(f"    - Scenario: {row_min['project']}::{row_min['class']}::{row_min['method']}")
    print(f"    - Corresponding mean: {mean_min:.2f}")


extreme_std_report(df_clean, "oa_inter_std", "oa_inter_mean", "OA Inter")
extreme_std_report(df_clean, "oa_inter_no_pa_std", "oa_inter_no_pa_mean", "OA Inter Without PA")

# ----------------------------
# Global average execution time
# ----------------------------
global_mean_oa = df_clean["oa_inter_mean"].mean()
global_mean_no_pa = df_clean["oa_inter_no_pa_mean"].mean()

print("\n=== Global Execution Mean ===")
print(f"OA Inter: {global_mean_oa:.4f}")
print(f"OA Inter Without PA: {global_mean_no_pa:.4f}")

# ----------------------------
# Comparison: which was faster per scenario
# ----------------------------
faster_oa = df_clean[df_clean["oa_inter_mean"] < df_clean["oa_inter_no_pa_mean"]]
faster_no_pa = df_clean[df_clean["oa_inter_mean"] > df_clean["oa_inter_no_pa_mean"]]
equal_times = df_clean[df_clean["oa_inter_mean"] == df_clean["oa_inter_no_pa_mean"]]

print("\n=== Scenario-by-Scenario Comparison ===")
print(f"Scenarios where OA Inter was faster: {len(faster_oa)}")
print(f"Scenarios where OA Inter Without PA was faster: {len(faster_no_pa)}")
print(f"Scenarios with equal times: {len(equal_times)}")

print("\n→ OA Inter faster in:")
for _, row in faster_oa.iterrows():
    diff = row["oa_inter_no_pa_mean"] - row["oa_inter_mean"]
    print(f"  - {row['project']}::{row['class']}::{row['method']} — {diff:.4f} seconds faster")

print("\n→ OA Inter Without PA faster in:")
for _, row in faster_no_pa.iterrows():
    diff = row["oa_inter_mean"] - row["oa_inter_no_pa_mean"]
    print(f"  - {row['project']}::{row['class']}::{row['method']} — {diff:.4f} seconds faster")

print("\n→ Scenarios with equal execution time:")
for _, row in equal_times.iterrows():
    diff = row["oa_inter_mean"] - row["oa_inter_no_pa_mean"]
    print(f"  - {row['project']}::{row['class']}::{row['method']} — {diff:.4f} seconds difference")

# ----------------------------
# Generate paginated PDF report
# ----------------------------
print_output = buffer.getvalue()
buffer.close()
sys.stdout = sys.__stdout__  # Restore stdout

# Rendering parameters
lines = print_output.splitlines()
lines_per_page = 60  # Adjust depending on font size and page size
font_size = 10

# Save to paginated PDF
pdf_path = "statistical_analysis.pdf"
with PdfPages(pdf_path) as pdf:
    for i in range(0, len(lines), lines_per_page):
        page = lines[i:i + lines_per_page]
        fig, ax = plt.subplots(figsize=(8.5, 11))  # Approx. A4 size
        ax.axis("off")
        ax.text(0, 1, "\n".join(page), fontsize=font_size, va='top', family="monospace")
        pdf.savefig(fig, bbox_inches="tight")
        plt.close()

print(f"\n✅ PDF successfully generated: {pdf_path}")
