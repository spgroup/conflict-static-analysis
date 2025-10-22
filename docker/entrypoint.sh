#!/bin/bash
set -e

echo "▶️ Performing experimental analysis..."
cd /home/mds/miningframework/ && python3 run_and_organize_ioa_experimental_results.py
cd /home/rds/miningframework/ && python3 run_and_organize_ioa_experimental_results.py

echo "▶️ Generating graphs and reports..."
cd /home/mds/miningframework/results && python3 run_all_mds.py
cd /home/rds/miningframework/results && python3 run_all_rds.py

echo "✅ Process completed successfully."
