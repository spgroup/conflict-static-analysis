#!/bin/bash
set -e

echo "▶️ Executando análise experimental..."
cd /home/mds/miningframework/ && python3 run_and_organize_ioa_experimental_results.py
cd /home/rds/miningframework/ && python3 run_and_organize_ioa_experimental_results.py

echo "▶️ Gerando gráficos e relatórios..."
cd /home/mds/miningframework/results && python3 run_all_mds.py
cd /home/rds/miningframework/results && python3 run_all_rds.py

echo "✅ Processo finalizado com sucesso."
