import os
import json
import pandas as pd
from constants import *

class PerformanceAnalyzer:
    def __init__(self):
        self.output_dir = '.'
        self.perf_summary = {}
        self.perf_soot = None
        self.perf_resource = None
    
    def analyze(self, output_dir='.'):
        """Analyze performance data from aggregated files"""
        self.output_dir = output_dir
        
        try:
            # Read the aggregated files
            self._load_performance_data()
            
            # Print performance statistics
            self._print_performance_stats()
        
        except Exception as e:
            print(f"Error analyzing performance data: {e}")
    
    def _load_performance_data(self):
        """Load the three aggregated performance files"""
        # Load performance summary stats (JSON)
        summary_path = os.path.join(self.output_dir, PERFORMANCE_SUMMARY_STATS_JSON)
        if os.path.exists(summary_path):
            with open(summary_path, 'r') as f:
                self.perf_summary = json.load(f)
        
        # Load performance soot results stats (CSV)
        soot_path = os.path.join(self.output_dir, PERFORMANCE_SOOT_STATS_CSV)
        if os.path.exists(soot_path):
            self.perf_soot = pd.read_csv(soot_path, sep=';')
        
        # Load performance resource stats (CSV)
        resource_path = os.path.join(self.output_dir, PERFORMANCE_RESOURCE_STATS_CSV)
        if os.path.exists(resource_path):
            self.perf_resource = pd.read_csv(resource_path)
    
    def _print_performance_stats(self):
        """Print performance statistics"""
        print("\n" + "="*60)
        print("Performance Analysis Results")
        print("="*60)
        
        # Print summary statistics
        if self.perf_summary:
            print("\nPerformance Summary (Aggregated):")
            print(f"  Mode: {self.perf_summary.get('mode', 'N/A')}")
            print(f"  Callgraph: {self.perf_summary.get('callgraph', 'N/A')}")
            print(f"  Status: {self.perf_summary.get('status', 'N/A')}")
            print(f"  Duration (seconds): {self.perf_summary.get('duration_seconds', 'N/A'):.2f}")
            print(f"  Peak Memory (GB): {self.perf_summary.get('peak_memory_gb', 'N/A'):.4f}")
            print(f"  Peak CPU (%): {self.perf_summary.get('peak_cpu_percent', 'N/A'):.2f}")
        
        # Print soot results statistics
        if self.perf_soot is not None:
            print("\nSoot Results (Aggregated):")
            print(f"  Total test methods: {len(self.perf_soot)}")
            print(f"  Average time (ms): {self.perf_soot['Time'].mean():.2f}")
            print(f"  Max time (ms): {self.perf_soot['Time'].max():.2f}")
            print(f"  Min time (ms): {self.perf_soot['Time'].min():.2f}")
            
            # OA Inter statistics
            oa_inter_true = (self.perf_soot['OA Inter'] == True).sum()
            oa_inter_false = len(self.perf_soot) - oa_inter_true
            oa_inter_pct = (oa_inter_true / len(self.perf_soot) * 100) if len(self.perf_soot) > 0 else 0
            print(f"  OA Inter True: {oa_inter_true} ({oa_inter_pct:.1f}%)")
            print(f"  OA Inter False: {oa_inter_false} ({100-oa_inter_pct:.1f}%)")
        
        # Print resource usage statistics
        if self.perf_resource is not None:
            print("\nResource Usage (Aggregated):")
            for col in self.perf_resource.columns:
                value = self.perf_resource[col].iloc[0]
                if isinstance(value, (int, float)):
                    print(f"  {col}: {value:.4f}")
                else:
                    print(f"  {col}: {value}")
        
        print("\n" + "="*60 + "\n")
