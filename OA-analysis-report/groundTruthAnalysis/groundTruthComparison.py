import os
import pandas as pd
import numpy as np
from sklearn.metrics import confusion_matrix, precision_score, recall_score, f1_score, accuracy_score
from datetime import datetime


class GroundTruthComparator:
    """Compares performance analysis results against ground truth (LOI)"""
    
    def __init__(self):
        self.ground_truth_df = None
        self.perf_soot_df = None
        self.output_dir = "."
        self.comparison_results = {}

    def compare(self, perf_soot_path, ground_truth_path, output_dir="."):
        """
        Compare performance results with ground truth
        
        Args:
            perf_soot_path: Path to performance_soot_results_stats.csv
            ground_truth_path: Path to loi.csv (ground truth file)
            output_dir: Output directory for results
        """
        self.output_dir = output_dir
        os.makedirs(self.output_dir, exist_ok=True)
        
        try:
            # Load the files
            self._load_data(perf_soot_path, ground_truth_path)
            
            # Filter and match scenarios
            matched_df = self._match_scenarios()
            
            # Generate comparisons with two different parity rules
            self._generate_comparisons(matched_df)
            
            # Save results
            self._save_results()
            
            # Print summary
            self._print_summary()
            
        except Exception as e:
            print(f"Error in ground truth comparison: {e}")
            raise

    def _load_data(self, perf_soot_path, ground_truth_path):
        """Load performance and ground truth data"""
        # Load performance soot results
        if not os.path.exists(perf_soot_path):
            raise FileNotFoundError(f"Performance file not found: {perf_soot_path}")
        
        self.perf_soot_df = pd.read_csv(perf_soot_path, sep=";")
        print(f"Loaded performance data: {len(self.perf_soot_df)} records")
        
        # Load ground truth
        if not os.path.exists(ground_truth_path):
            raise FileNotFoundError(f"Ground truth file not found: {ground_truth_path}")
        
        self.ground_truth_df = pd.read_csv(ground_truth_path)
        print(f"Loaded ground truth data: {len(self.ground_truth_df)} records")

    def _match_scenarios(self):
        """
        Match performance results with ground truth based on:
        project, class, method, merge commit
        
        Also filter ground truth to only include "Yes" or "No" for LOI
        """
        # Filter ground truth: only keep rows where LOI is "Yes" or "No"
        loi_col = "Locally Observable Interference"
        filtered_gt = self.ground_truth_df[
            self.ground_truth_df[loi_col].isin(["Yes", "No"])
        ].copy()
        
        # Convert "Yes"/"No" to True/False for easier comparison
        filtered_gt["loi_ground_truth"] = filtered_gt[loi_col] == "Yes"
        
        print(f"Filtered ground truth: {len(filtered_gt)} records (LOI = Yes or No)")
        
        # Merge on matching columns
        merged = self.perf_soot_df.merge(
            filtered_gt[["project", "class", "method", "merge commit", "loi_ground_truth"]],
            on=["project", "class", "method", "merge commit"],
            how="inner"
        )
        
        print(f"Matched scenarios: {len(merged)} records")
        
        # Convert OA Inter to boolean
        # Handle different values: "true"/"false" (strings) or true/false (booleans)
        def parse_oa_inter(val):
            if isinstance(val, bool):
                return val
            if isinstance(val, str):
                return val.lower() == "true"
            return False
        
        merged["oa_inter_prediction"] = merged["OA Inter"].apply(parse_oa_inter)
        
        return merged

    def _generate_comparisons(self, matched_df):
        """
        Generate four comparison subsets with different parity rules
        
        Subset 1: Strict matching
        - Only consider scenarios where OA Inter is "true" or "false" (exclude timeout/not-found)
        - loi false (No) = perf false
        - loi true (Yes) = perf true
        
        Subset 2: Timeout as False matching
        - Only consider scenarios where OA Inter is "true", "false", or "timeout" (exclude not-found)
        - Map timeout to false
        - loi false (No) = perf false or timeout
        - loi true (Yes) = perf true
        
        Subset 3: Timeout as True matching
        - Only consider scenarios where OA Inter is "true", "false", or "timeout" (exclude not-found)
        - Map timeout to true
        - loi false (No) = perf false
        - loi true (Yes) = perf true or timeout
        """
        self.comparison_results = {}
        
        # Subset 1: Strict matching - exclude timeout and not-found scenarios
        subset1_df = matched_df[
            (matched_df["OA Inter"] != "timeout") & 
            (matched_df["OA Inter"] != "not-found")
        ].copy()
        subset1_df["prediction_strict"] = subset1_df["oa_inter_prediction"]
        self._analyze_subset(subset1_df, "strict", "Strict Matching (excluding timeout/not-found)")
        
        # Subset 2: Timeout as False matching - exclude not-found, treat timeout as false
        subset2_df = matched_df[
            matched_df["OA Inter"] != "not-found"
        ].copy()
        subset2_df["prediction_timeout_false"] = subset2_df["OA Inter"].apply(
            lambda x: (x == "true" or x == True)
        )
        self._analyze_subset(subset2_df, "timeout_false", "Timeout as False (excluding not-found)")
        
        # Subset 3: Timeout as True matching - exclude not-found, treat timeout as true
        subset3_df = matched_df[
            matched_df["OA Inter"] != "not-found"
        ].copy()
        subset3_df["prediction_timeout_true"] = subset3_df["OA Inter"].apply(
            lambda x: (x == "true" or x == True or x == "timeout")
        )
        self._analyze_subset(subset3_df, "timeout_true", "Timeout as True (excluding not-found)")

    def _analyze_subset(self, df, subset_name, subset_description):
        """Analyze a comparison subset and calculate metrics"""
        
        pred_col = f"prediction_{subset_name}"
        truth_col = "loi_ground_truth"
        
        # Get predictions and ground truth
        y_true = df[truth_col].values
        y_pred = df[pred_col].values
        
        # Calculate confusion matrix
        tn, fp, fn, tp = confusion_matrix(y_true, y_pred).ravel()
        
        # Calculate metrics
        accuracy = accuracy_score(y_true, y_pred)
        precision = precision_score(y_true, y_pred, zero_division=0)
        recall = recall_score(y_true, y_pred, zero_division=0)
        f1 = f1_score(y_true, y_pred, zero_division=0)
        specificity = tn / (tn + fp) if (tn + fp) > 0 else 0
        
        # Store results
        self.comparison_results[subset_name] = {
            "description": subset_description,
            "num_scenarios": len(df),
            "confusion_matrix": {
                "TP": int(tp),
                "FP": int(fp),
                "TN": int(tn),
                "FN": int(fn)
            },
            "metrics": {
                "accuracy": float(accuracy),
                "precision": float(precision),
                "recall": float(recall),
                "f1": float(f1),
                "specificity": float(specificity)
            },
            "class_distribution": {
                "positive_ground_truth": int(y_true.sum()),
                "negative_ground_truth": int((~y_true).sum()),
                "positive_predicted": int(y_pred.sum()),
                "negative_predicted": int((~y_pred).sum())
            }
        }

    def _save_results(self):
        """Save comparison results to txt files"""
        # Create a summary report
        summary_file = os.path.join(self.output_dir, "ground_truth_comparison_summary.txt")
        
        with open(summary_file, "w") as f:
            f.write("Ground Truth Comparison Analysis\n")
            f.write(f"Generated: {datetime.now().isoformat()}\n\n")
            
            for subset_name, results in self.comparison_results.items():
                f.write(f"\n{results['description']}\n")
                f.write("=" * 80 + "\n")
                f.write(f"Number of Scenarios: {results['num_scenarios']}\n\n")
                
                cm = results['confusion_matrix']
                f.write("Confusion Matrix:\n")
                f.write(f"  TP (True Positive): {cm['TP']}\n")
                f.write(f"  FP (False Positive): {cm['FP']}\n")
                f.write(f"  TN (True Negative): {cm['TN']}\n")
                f.write(f"  FN (False Negative): {cm['FN']}\n\n")
                
                metrics = results['metrics']
                f.write("Metrics:\n")
                f.write(f"  Accuracy: {metrics['accuracy']:.4f}\n")
                f.write(f"  Precision: {metrics['precision']:.4f}\n")
                f.write(f"  Recall: {metrics['recall']:.4f}\n")
                f.write(f"  F1-Score: {metrics['f1']:.4f}\n")
                f.write(f"  Specificity: {metrics['specificity']:.4f}\n\n")
                
                dist = results['class_distribution']
                f.write("Class Distribution:\n")
                f.write(f"  Positive (Ground Truth): {dist['positive_ground_truth']}\n")
                f.write(f"  Negative (Ground Truth): {dist['negative_ground_truth']}\n")
                f.write(f"  Positive (Predicted): {dist['positive_predicted']}\n")
                f.write(f"  Negative (Predicted): {dist['negative_predicted']}\n\n")
        
        print(f"Saved summary report to: {summary_file}")

    def _print_summary(self):
        """Print summary statistics to console"""
        print("\n" + "=" * 80)
        print("GROUND TRUTH COMPARISON RESULTS")
        print("=" * 80)
        
        for subset_name, results in self.comparison_results.items():
            print(f"\n{results['description']}")
            print("-" * 80)
            print(f"Number of scenarios: {results['num_scenarios']}")
            
            cm = results['confusion_matrix']
            print(f"\nConfusion Matrix:")
            print(f"  TP: {cm['TP']:5d}  |  FP: {cm['FP']:5d}")
            print(f"  FN: {cm['FN']:5d}  |  TN: {cm['TN']:5d}")
            
            metrics = results['metrics']
            print(f"\nMetrics:")
            print(f"  Accuracy:   {metrics['accuracy']:.4f}")
            print(f"  Precision:  {metrics['precision']:.4f}")
            print(f"  Recall:     {metrics['recall']:.4f}")
            print(f"  F1-Score:   {metrics['f1']:.4f}")
            print(f"  Specificity:{metrics['specificity']:.4f}")
            
            dist = results['class_distribution']
            print(f"\nClass Distribution:")
            print(f"  Ground Truth - Positive: {dist['positive_ground_truth']}, Negative: {dist['negative_ground_truth']}")
            print(f"  Predicted    - Positive: {dist['positive_predicted']}, Negative: {dist['negative_predicted']}")
        
        print("\n" + "=" * 80 + "\n")
