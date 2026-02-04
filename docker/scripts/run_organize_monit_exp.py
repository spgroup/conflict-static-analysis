import csv
import json
import os
import platform
import psutil
import shutil
import subprocess
import threading
import time
from datetime import datetime

# ================= CONFIGURAÇÕES =================
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
MODES = ["ioa", "icf", "idfp"]
CALL_GRAPH_TYPES = ["CHA", "RTA", "VTA", "SPARK"]
SAMPLING_INTERVAL = 1.0  # Segundos


# ================= MONITORAMENTO DE RECURSOS =================

class ResourceMonitor(threading.Thread):
    """Monitor de CPU e memória em thread separada"""

    def __init__(self, pid, output_csv, interval=1.0):
        super().__init__()
        self.pid = pid
        self.output_csv = output_csv
        self.interval = interval
        self.stop_event = threading.Event()
        self.peak_memory_gb = 0.0
        self.peak_cpu_percent = 0.0
        self.process_cache = {}

    def run(self):
        try:
            parent = psutil.Process(self.pid)
            self.process_cache[self.pid] = parent
        except psutil.NoSuchProcess:
            return

        # Prepara CSV
        with open(self.output_csv, 'w', newline='') as f:
            writer = csv.writer(f)
            writer.writerow(["Time_Sec", "CPU_Percent", "Memory_GB"])

        start_time = time.time()

        while not self.stop_event.is_set():
            try:
                # Atualiza lista de processos filhos
                try:
                    current_children = parent.children(recursive=True)
                except psutil.NoSuchProcess:
                    break

                current_pids = {p.pid for p in current_children}
                current_pids.add(self.pid)

                # Atualiza cache de processos
                for child in current_children:
                    if child.pid not in self.process_cache:
                        self.process_cache[child.pid] = child
                        child.cpu_percent(interval=None)

                # Remove processos mortos
                for pid in list(self.process_cache.keys()):
                    if pid not in current_pids:
                        del self.process_cache[pid]

                # Coleta métricas
                total_cpu = 0.0
                total_mem_bytes = 0

                for proc in self.process_cache.values():
                    try:
                        total_cpu += proc.cpu_percent(interval=None)
                        total_mem_bytes += proc.memory_info().rss
                    except (psutil.NoSuchProcess, psutil.AccessDenied):
                        continue

                # Conversões e gravação
                current_mem_gb = total_mem_bytes / (1024 ** 3)
                elapsed = round(time.time() - start_time, 2)

                self.peak_memory_gb = max(self.peak_memory_gb, current_mem_gb)
                self.peak_cpu_percent = max(self.peak_cpu_percent, total_cpu)

                with open(self.output_csv, 'a', newline='') as f:
                    writer = csv.writer(f)
                    writer.writerow([elapsed, round(total_cpu, 2), round(current_mem_gb, 4)])

                time.sleep(self.interval)

            except Exception as e:
                print(f"[Monitor Erro] {e}")
                break

    def stop(self):
        self.stop_event.set()


# ================= FUNÇÕES AUXILIARES =================

def ensure_dirs():
    """Cria diretórios para cada modo e tipo de call graph"""
    for mode in MODES:
        for cg in CALL_GRAPH_TYPES:
            os.makedirs(os.path.join(BASE_RESULTS_DIR, mode, cg), exist_ok=True)


def move_files_to_result_folder(mode, cg, run_number):
    """Move arquivos gerados para pasta de resultados"""
    dest_dir = os.path.join(BASE_RESULTS_DIR, mode, cg, f"data{run_number}")
    os.makedirs(dest_dir, exist_ok=True)

    for filename in FILES_TO_MOVE:
        src_path = os.path.join(SOURCE_BASE, filename)

        if os.path.exists(src_path):
            dest_path = os.path.join(dest_dir, os.path.basename(filename))
            shutil.move(src_path, dest_path)


def get_gradle_command():
    """Retorna comando gradle apropriado para o OS"""
    return "gradlew.bat" if platform.system() == "Windows" else "./gradlew"


# ================= EXECUÇÃO DO SOOT =================

def run_soot(mode, cg, run_number):
    """Executa análise Soot com monitoramento de recursos"""

    print(f"[{datetime.now().strftime('%H:%M:%S')}] [{mode.upper()} | {cg}] Run {run_number}...")

    cmd = [
        get_gradle_command(),
        "--no-daemon",
        "run",
        "-DmainClass=services.outputProcessors.soot.Main",
        f"--args=-{mode} -t 300 -cg {cg}"
    ]

    # Arquivos temporários
    temp_series_csv = os.path.join(SOURCE_BASE, "resource_usage_series.csv")
    temp_summary_json = os.path.join(SOURCE_BASE, "performance_summary.json")
    log_file_path = os.path.join(SOURCE_BASE, "execution_log.txt")
    err_file_path = os.path.join(SOURCE_BASE, "execution_err.txt")

    # Configuração de ambiente (descomente se necessário)
    env_vars = os.environ.copy()
    env_vars["_JAVA_OPTIONS"] = "-Xmx20g -Xms20g -XX:-UseGCOverheadLimit"

    start_wall_time = time.time()
    exit_code = None
    monitor = None
    process = None

    try:
        with open(log_file_path, "w", buffering=1) as f_out, \
                open(err_file_path, "w", buffering=1) as f_err:

            process = subprocess.Popen(
                cmd,
                stdout=f_out,
                stderr=f_err,
                env=env_vars
            )

            # Inicia monitoramento
            monitor = ResourceMonitor(process.pid, temp_series_csv, interval=SAMPLING_INTERVAL)
            monitor.start()

            # Espera conclusão
            exit_code = process.wait()

    except KeyboardInterrupt:
        print("\n[INTERROMPIDO] Usuário cancelou a execução.")
        if process:
            process.kill()
        exit_code = "INTERRUPTED"

    except Exception as e:
        print(f"[ERRO CRÍTICO] {e}")
        if process:
            process.kill()
        exit_code = "PYTHON_ERROR"

    finally:
        # Garante salvamento das métricas independente do resultado
        end_wall_time = time.time()
        duration = end_wall_time - start_wall_time

        peak_mem = 0.0
        peak_cpu = 0.0

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

        print(f"[REC] Métricas salvas | Duração: {round(duration, 2)}s | "
              f"Pico Mem: {round(peak_mem, 2)} GB | Status: {status}")

        # Tratamento de erro
        if exit_code not in (0, "INTERRUPTED"):
            print(f"[ERRO] Execução falhou (Exit Code: {exit_code})")
            if os.path.exists(err_file_path):
                print("--- ÚLTIMOS ERROS ---")
                try:
                    with open(err_file_path, "r") as fer:
                        print(fer.read()[-500:])
                except:
                    pass
                print("---------------------")
            return

    # Move arquivos apenas se sucesso
    move_files_to_result_folder(mode, cg, run_number)


# ================= MAIN =================

def main():
    """Executa múltiplas rodadas do benchmark"""
    ensure_dirs()

    print(f"\n{'=' * 60}")
    print(f"Benchmark Soot - {RUNS_PER_MODE} rodada(s) por configuração")
    print(f"Modos: {MODES} | Call Graphs: {CALL_GRAPH_TYPES}")
    print(f"{'=' * 60}\n")

    for i in range(1, RUNS_PER_MODE + 1):
        for mode in MODES:
            for cg in CALL_GRAPH_TYPES:
                run_soot(mode, cg, i)

    print(f"\n{'=' * 60}")
    print("Benchmark concluído!")
    print(f"{'=' * 60}\n")


if __name__ == "__main__":
    main()
