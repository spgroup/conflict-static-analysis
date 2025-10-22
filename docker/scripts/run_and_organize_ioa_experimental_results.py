import os
import platform
import shutil
import subprocess
from datetime import datetime

# CONFIGURAÇÕES
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
    "time.txt"
]

BASE_RESULTS_DIR = "results"
RUNS_PER_MODE = 10
MODES = ["ioa", "ioa-without-pa"]


def ensure_dirs():
    for mode in MODES:
        os.makedirs(os.path.join(BASE_RESULTS_DIR, mode), exist_ok=True)


def move_files_to_result_folder(mode, run_number):
    dest_dir = os.path.join(BASE_RESULTS_DIR, mode, f"data{run_number}")
    os.makedirs(dest_dir, exist_ok=True)

    for filename in FILES_TO_MOVE:
        src_path = os.path.join(SOURCE_BASE, filename)
        dest_path = os.path.join(dest_dir, os.path.basename(filename))

        if os.path.exists(src_path):
            shutil.move(src_path, dest_path)
            print(f"[OK] movido: {filename} → {os.path.relpath(dest_path)}")
        else:
            print(f"[AVISO] Arquivo não encontrado: {src_path}")


def get_gradle_command():
    if platform.system() == "Windows":
        return "gradlew.bat"
    else:
        return "./gradlew"


def run_soot(mode, run_number):
    print(f"[{datetime.now().strftime('%H:%M:%S')}] [{mode.upper()}] Execução {run_number} iniciada.")
    try:
        subprocess.run(
            [get_gradle_command(), "run", "-DmainClass=services.outputProcessors.soot.Main", f"--args=-{mode} -t 300"],
            check=True
        )
    except subprocess.CalledProcessError as e:
        print(f"[ERRO] Execução {run_number} ({mode}) falhou: {e}")
        return

    move_files_to_result_folder(mode, run_number)
    print(f"[{datetime.now().strftime('%H:%M:%S')}] Resultados copiados para results/{mode}/data{run_number}/")


def main():
    ensure_dirs()
    for mode in MODES:
        for i in range(1, RUNS_PER_MODE + 1):
            run_soot(mode, i)


if __name__ == "__main__":
    main()
