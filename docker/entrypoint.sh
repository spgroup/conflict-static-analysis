#!/bin/bash
set -e

echo "▶️ Performing experimental analysis..."
cd /home/mds/miningframework/ && python3 run_organize_monit_exp.py
cd /home/rds/miningframework/ && python3 run_organize_monit_exp.py

echo "✅ Process completed successfully."
