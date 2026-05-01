# -*- coding: utf-8 -*-
import os
import platform
import shutil
import subprocess
import time
import threading
import csv
import json
from datetime import datetime

# ================= CONFIGURATIONS =================
SOURCE_BASE = "."
FILES_TO_MOVE = [
    "output/data/soot-results.csv",
    "AnalysisRecords.csv",
    "conflicts_log.txt",
    "outConsole.txt",
    "out.txt",
    "out.json",
    "HasMainMethod.csv",
    "PANotResolve.csv",
    "visited_methods.txt",
    "time.txt",
    "resource_usage_series.csv", 
    "performance_summary.json"
]

BASE_RESULTS_DIR = "results"
RUNS_PER_MODE = 1
MODES = ["ioa", "idf", "icf"]
CALL_GRAPH_TYPES = ["CHA", "RTA", "VTA", "SPARK"]
SAMPLING_INTERVAL = 1.0  # Seconds

# ================= RESOURCE MONITOR (CGROUPS) =================

class ResourceMonitor(threading.Thread):
    def __init__(self, output_csv, interval=1.0):
        super().__init__()
        self.output_csv = output_csv
        self.interval = interval
        self.stop_event = threading.Event()
        self.peak_memory_gb = 0.0
        self.peak_cpu_percent = 0.0
        
        # Detect Cgroup version for Docker
        self.is_v2 = os.path.exists("/sys/fs/cgroup/cgroup.controllers")
        
        if self.is_v2:
            self.mem_path = "/sys/fs/cgroup/memory.current"
            self.cpu_path = "/sys/fs/cgroup/cpu.stat"
        else:
            self.mem_path = "/sys/fs/cgroup/memory/memory.usage_in_bytes"
            self.cpu_path = "/sys/fs/cgroup/cpuacct/cpuacct.usage"

    def _get_cpu_usage_ns(self):
        try:
            if self.is_v2:
                with open(self.cpu_path, "r") as f:
                    for line in f:
                        if line.startswith("usage_usec"):
                            return int(line.split()[1]) * 1000
            else:
                with open(self.cpu_path, "r") as f:
                    return int(f.read().strip())
        except Exception: return None

    def _get_mem_usage_bytes(self):
        try:
            with open(self.mem_path, "r") as f:
                rss = int(f.read().strip())
            # Also check swap if available
            swap_path = "/sys/fs/cgroup/memory.swap.current"
            swap = 0
            if os.path.exists(swap_path):
                with open(swap_path, "r") as f:
                    swap = int(f.read().strip())
            return rss + swap
        except Exception: return 0

    def run(self):
        with open(self.output_csv, 'w', newline='') as f:
            writer = csv.writer(f)
            writer.writerow(["Time_Sec", "CPU_Percent_Total", "Memory_GB"])

        start_time = time.time()
        last_cpu_ns = self._get_cpu_usage_ns()
        last_measure_time = start_time

        while not self.stop_event.is_set():
            time.sleep(self.interval)
            
            curr_cpu_ns = self._get_cpu_usage_ns()
            curr_time = time.time()
            mem_bytes = self._get_mem_usage_bytes()
            
            if curr_cpu_ns is not None and last_cpu_ns is not None:
                delta_cpu_ns = curr_cpu_ns - last_cpu_ns
                delta_time_ns = (curr_time - last_measure_time) * 1e9
                if delta_time_ns > 0:
                    cpu_count = len(os.sched_getaffinity(0))
                    cpu_p = (delta_cpu_ns / delta_time_ns) * 100 / cpu_count
                else:
                    cpu_p = 0.0
                
                self.peak_cpu_percent = max(self.peak_cpu_percent, cpu_p)
                last_cpu_ns = curr_cpu_ns
                last_measure_time = curr_time
            else:
                cpu_p = 0.0

            current_mem_gb = mem_bytes / (1024 ** 3)
            self.peak_memory_gb = max(self.peak_memory_gb, current_mem_gb)

            elapsed = round(curr_time - start_time, 2)
            with open(self.output_csv, 'a', newline='') as f:
                writer = csv.writer(f)
                writer.writerow([elapsed, round(cpu_p, 2), round(current_mem_gb, 4)])

    def stop(self):
        self.stop_event.set()

# ================= AUXILIARY FUNCTIONS =================

def ensure_dirs():
    for mode in MODES:
        for cg in CALL_GRAPH_TYPES:
            os.makedirs(os.path.join(BASE_RESULTS_DIR, mode, cg), exist_ok=True)

def move_files_to_result_folder(mode, cg, run_number):
    dest_dir = os.path.join(BASE_RESULTS_DIR, mode, cg, f"data{run_number}")
    os.makedirs(dest_dir, exist_ok=True)

    for filename in FILES_TO_MOVE:
        src_path = os.path.join(SOURCE_BASE, filename)
        if os.path.exists(src_path):
            dest_path = os.path.join(dest_dir, os.path.basename(filename))
            shutil.move(src_path, dest_path)

def get_gradle_command():
    return "gradlew.bat" if platform.system() == "Windows" else "./gradlew"

# ================= SOOT EXECUTION =================

def run_soot(mode, cg, run_number):
    print(f"[{datetime.now().strftime('%H:%M:%S')}] [{mode.upper()} | {cg}] Run {run_number}...")
    
    cmd = [
        get_gradle_command(),
        "--no-daemon",
        "run",
        "-DmainClass=services.outputProcessors.soot.Main",
        f"--args=-{mode} -t 300 -cg {cg} -depthLimit 5"
    ]

    temp_series_csv = os.path.join(SOURCE_BASE, "resource_usage_series.csv")
    temp_summary_json = os.path.join(SOURCE_BASE, "performance_summary.json")
    log_file_path = os.path.join(SOURCE_BASE, "execution_log.txt")
    err_file_path = os.path.join(SOURCE_BASE, "execution_err.txt")

    env_vars = os.environ.copy()
    env_vars["_JAVA_OPTIONS"] = "-Xmx20g -Xms20g -XX:-UseGCOverheadLimit"

    start_wall_time = time.time()
    exit_code = None
    monitor = None
    process = None

    try:
        with open(log_file_path, "w", buffering=1) as f_out, \
             open(err_file_path, "w", buffering=1) as f_err:
            
            # 1. Inicia o Monitor ANTES do processo (Cgroups monitora o container todo)
            # CORRECAO: Removido process.pid daqui
            monitor = ResourceMonitor(temp_series_csv, interval=SAMPLING_INTERVAL)
            monitor.start()
            
            # 2. Inicia o processo do Soot
            process = subprocess.Popen(cmd, stdout=f_out, stderr=f_err, env=env_vars)
            
            # 3. Espera o Soot terminar
            exit_code = process.wait()
            
    except KeyboardInterrupt:
        print("\n[INTERRUPTED] User cancelled execution.")
        if process: process.kill()
        exit_code = "INTERRUPTED"
    except Exception as e:
        print(f"[CRITICAL ERROR] {e}")
        if process: process.kill()
        exit_code = "PYTHON_ERROR"
    finally:
        duration = time.time() - start_wall_time
        peak_mem, peak_cpu = 0.0, 0.0
        
        if monitor:
            monitor.stop()
            monitor.join()
            peak_mem = monitor.peak_memory_gb
            peak_cpu = monitor.peak_cpu_percent

        status = "SUCCESS" if exit_code == 0 else f"FAILED_CODE_{exit_code}"

        summary_data = {
            "mode": mode,
            "callgraph": cg,
            "run_number": run_number,
            "status": status,
            "duration_seconds": round(duration, 2),
            "peak_memory_gb": round(peak_mem, 4),
            "peak_cpu_percent": round(peak_cpu, 2),
            "timestamp": datetime.now().isoformat()
        }

        with open(temp_summary_json, 'w') as f:
            json.dump(summary_data, f, indent=4)
        
        print(f"[REC] Metrics Saved | Duration: {round(duration, 2)}s | "
              f"Peak Mem: {round(peak_mem, 2)} GB | Peak CPU: {round(peak_cpu, 2)}% | Status: {status}")

        if exit_code not in (0, "INTERRUPTED"):
            if os.path.exists(err_file_path):
                print("--- LAST ERRORS ---")
                try:
                    with open(err_file_path, "r") as fer:
                        print(fer.read()[-500:])
                except: pass
            return

    move_files_to_result_folder(mode, cg, run_number)

# ================= MAIN =================

def main():
    ensure_dirs()
    print(f"\n{'='*60}")
    print(f"Benchmark Soot - {RUNS_PER_MODE} run(s) per configuration")
    print(f"Modes: {MODES} | Call Graphs: {CALL_GRAPH_TYPES}")
    print(f"{'='*60}\n")
    
    for i in range(1, RUNS_PER_MODE + 1):
        for mode in MODES:
            for cg in CALL_GRAPH_TYPES:
                run_soot(mode, cg, i)
    
    print(f"\n{'='*60}")
    print("Benchmark Finished!")
    print(f"{'='*60}\n")

if __name__ == "__main__":
    main()