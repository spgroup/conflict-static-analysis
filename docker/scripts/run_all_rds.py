import os
import subprocess
import sys


def run_command(command, description):
    print(f"\n=== Running: {description} ===")
    result = subprocess.run(command, shell=True)
    if result.returncode != 0:
        print(f"❌ Error running: {description}")
        sys.exit(result.returncode)
    else:
        print(f"✅ Done: {description}")


# Step 1: Install dependencies
if os.path.exists("requirements.txt"):
    run_command("pip install -r requirements.txt", "Install requirements")
else:
    print("⚠️ requirements.txt not found. Skipping dependency installation.")

# Step 2: Run merge script
run_command("python3 merge_soot_results_rds.py", "Merge Soot Results")

# Step 3: Run plotting script
run_command("python3 raincloud_plot.py", "Generate Raincloud Plot")

# Step 4: Run statistical analysis
run_command("python3 statistical_report.py", "Generate Statistical Report")
