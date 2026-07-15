import os
from datetime import datetime

import numpy as np
import pandas as pd
from sklearn.metrics import (
    accuracy_score,
    confusion_matrix,
    f1_score,
    precision_score,
    recall_score,
)


class GroundTruthComparator:
    """Compares performance analysis results against ground truth (LOI)."""

    def __init__(self):
        self.ground_truth_df = None
        self.perf_soot_df = None
        self.output_dir = "."
        self.comparison_results = {}

    def compare(self, perf_soot_path, ground_truth_path, output_dir="."):
        self.output_dir = output_dir
        os.makedirs(self.output_dir, exist_ok=True)
        try:
            self._load_data(perf_soot_path, ground_truth_path)
            matched_df = self._match_scenarios()
            self._generate_comparisons(matched_df)
            self._save_results()
            self._print_summary()
        except Exception as e:
            print(f"Error in ground truth comparison: {e}")
            raise

    # ------------------------------------------------------------------ data loading

    def _load_data(self, perf_soot_path, ground_truth_path):
        if not os.path.exists(perf_soot_path):
            raise FileNotFoundError(f"Performance file not found: {perf_soot_path}")
        self.perf_soot_df = pd.read_csv(perf_soot_path, sep=";", encoding="utf-8-sig")
        self.perf_soot_df.columns = self.perf_soot_df.columns.str.strip()
        print(f"Loaded performance data: {len(self.perf_soot_df)} records")

        if not os.path.exists(ground_truth_path):
            raise FileNotFoundError(f"Ground truth file not found: {ground_truth_path}")
        self.ground_truth_df = pd.read_csv(ground_truth_path, encoding="utf-8-sig")
        self.ground_truth_df.columns = self.ground_truth_df.columns.str.strip()
        print(f"Loaded ground truth data: {len(self.ground_truth_df)} records")

    # ------------------------------------------------------------------ matching

    def _match_scenarios(self):
        loi_col = "Locally Observable Interference"
        filtered_gt = self.ground_truth_df[
            self.ground_truth_df[loi_col].isin(["Yes", "No"])
        ].copy()
        filtered_gt["loi_ground_truth"] = filtered_gt[loi_col] == "Yes"
        print(f"Filtered ground truth: {len(filtered_gt)} records (LOI = Yes or No)")

        merged = self.perf_soot_df.merge(
            filtered_gt[["project", "class", "method", "merge commit", "loi_ground_truth"]],
            on=["project", "class", "method", "merge commit"],
            how="inner",
        )
        print(f"Matched scenarios: {len(merged)} records")

        merged["oa_inter_prediction"] = merged["OA Inter"].apply(
            lambda v: v if isinstance(v, bool) else (str(v).lower() == "true")
        )
        return merged

    # ------------------------------------------------------------------ comparisons

    def _generate_comparisons(self, matched_df):
        """
        Three parity-rule subsets:
          strict:         exclude timeout and not-found; direct boolean match
          timeout_false:  treat timeout as False; exclude not-found
          timeout_true:   treat timeout as True; exclude not-found
        """
        self.comparison_results = {}

        subset1 = matched_df[
            (matched_df["OA Inter"] != "timeout") &
            (matched_df["OA Inter"] != "not-found")
        ].copy()
        subset1["prediction_strict"] = subset1["oa_inter_prediction"]
        self._analyze_subset(subset1, "strict", "Strict Matching (excluding timeout/not-found)")

        subset2 = matched_df[matched_df["OA Inter"] != "not-found"].copy()
        subset2["prediction_timeout_false"] = subset2["OA Inter"].apply(
            lambda x: x == "true" or x is True
        )
        self._analyze_subset(subset2, "timeout_false", "Timeout as False (excluding not-found)")

        subset3 = matched_df[matched_df["OA Inter"] != "not-found"].copy()
        subset3["prediction_timeout_true"] = subset3["OA Inter"].apply(
            lambda x: x == "true" or x is True or x == "timeout"
        )
        self._analyze_subset(subset3, "timeout_true", "Timeout as True (excluding not-found)")

    def _analyze_subset(self, df, subset_name, subset_description):
        pred_col = f"prediction_{subset_name}"
        y_true = df["loi_ground_truth"].values
        y_pred = df[pred_col].values

        tn, fp, fn, tp = confusion_matrix(y_true, y_pred).ravel()
        self.comparison_results[subset_name] = {
            "description": subset_description,
            "num_scenarios": len(df),
            "confusion_matrix": {"TP": int(tp), "FP": int(fp), "TN": int(tn), "FN": int(fn)},
            "metrics": {
                "accuracy":    float(accuracy_score(y_true, y_pred)),
                "precision":   float(precision_score(y_true, y_pred, zero_division=0)),
                "recall":      float(recall_score(y_true, y_pred, zero_division=0)),
                "f1":          float(f1_score(y_true, y_pred, zero_division=0)),
                "specificity": tn / (tn + fp) if (tn + fp) > 0 else 0,
            },
            "class_distribution": {
                "positive_ground_truth": int(y_true.sum()),
                "negative_ground_truth": int((~y_true).sum()),
                "positive_predicted":   int(y_pred.sum()),
                "negative_predicted":   int((~y_pred).sum()),
            },
        }

    # ------------------------------------------------------------------ output

    def _save_results(self):
        summary_file = os.path.join(self.output_dir, "ground_truth_comparison_summary.txt")
        with open(summary_file, "w") as f:
            f.write("Ground Truth Comparison Analysis\n")
            f.write(f"Generated: {datetime.now().isoformat()}\n\n")
            for results in self.comparison_results.values():
                f.write(f"\n{results['description']}\n")
                f.write("=" * 80 + "\n")
                f.write(f"Number of Scenarios: {results['num_scenarios']}\n\n")
                cm = results["confusion_matrix"]
                f.write("Confusion Matrix:\n")
                for k, v in cm.items():
                    f.write(f"  {k}: {v}\n")
                f.write("\nMetrics:\n")
                for k, v in results["metrics"].items():
                    f.write(f"  {k.capitalize()}: {v:.4f}\n")
                f.write("\nClass Distribution:\n")
                for k, v in results["class_distribution"].items():
                    f.write(f"  {k}: {v}\n")
                f.write("\n")
        print(f"Saved summary report to: {summary_file}")

    def _print_summary(self):
        print("\n" + "=" * 80)
        print("GROUND TRUTH COMPARISON RESULTS")
        print("=" * 80)
        for results in self.comparison_results.values():
            print(f"\n{results['description']}")
            print("-" * 80)
            print(f"Number of scenarios: {results['num_scenarios']}")
            cm = results["confusion_matrix"]
            print(f"\nConfusion Matrix:")
            print(f"  TP: {cm['TP']:5d}  |  FP: {cm['FP']:5d}")
            print(f"  FN: {cm['FN']:5d}  |  TN: {cm['TN']:5d}")
            m = results["metrics"]
            print(f"\nMetrics:")
            print(f"  Accuracy:    {m['accuracy']:.4f}")
            print(f"  Precision:   {m['precision']:.4f}")
            print(f"  Recall:      {m['recall']:.4f}")
            print(f"  F1-Score:    {m['f1']:.4f}")
            print(f"  Specificity: {m['specificity']:.4f}")
            dist = results["class_distribution"]
            print(f"\nClass Distribution:")
            print(f"  Ground Truth - Positive: {dist['positive_ground_truth']}, Negative: {dist['negative_ground_truth']}")
            print(f"  Predicted    - Positive: {dist['positive_predicted']}, Negative: {dist['negative_predicted']}")
        print("\n" + "=" * 80 + "\n")
