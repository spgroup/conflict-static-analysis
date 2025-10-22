import csv
from pathlib import Path
from statistics import mean, median, stdev

BASE_DIR = Path(".")
CSV_NAME = "soot-results.csv"
OUTPUT = "merged_results.csv"

# Blacklist of scenarios (project, class, method, merge_commit)
BLACKLIST = {
    ("antlr4", "org.antlr.v4.codegen.target.Python2Target", "python2Keywords",
     "69ff2669eec265e25721dbc27cb00f6c381d0b41"),
    ("antlr4", "org.antlr.v4.codegen.target.Python3Target", "python3Keywords",
     "69ff2669eec265e25721dbc27cb00f6c381d0b41"),
    ("swagger-maven-plugin", "com.github.kongchen.swagger.docgen.reader.AbstractReader",
     "hasValidAnnotations(List<Annotation>)", "e825a7fdc6ef688f1253b93d2cb236e710acfc56"),
    ("elasticsearch", "org.elasticsearch.common.settings.IndexScopedSettings", "BUILT_IN_INDEX_SETTINGS",
     "d896886973660785aac45275ddb110c1a6babc57"),
    ("elasticsearch", "org.elasticsearch.common.settings.ClusterSettings", "BUILT_IN_CLUSTER_SETTINGS",
     "0404db65e3497452886173957729c8e82cfd4a03"),
    ("cloud-slang", "io.cloudslang.lang.api.SlangImplTest", "ALL_EVENTS_SIZE",
     "20bac30d9bd76569aa6a4fa1e8261c1a9b5e6f76"),
    ("crawler4j", "edu.uci.ics.crawler4j.parser.Parser", "parse(Page, String)",
     "6fdb8f27b53c5d69b552341a459d0e1fa610f68d"),
}


def read_csvs(version: str, oa_field: str):
    """Reads all soot-results.csv files for the specified version and applies filters."""
    version_dir = BASE_DIR / version
    entries = {}
    execution_count = 0

    for subdir in sorted(version_dir.iterdir()):
        csv_path = subdir / CSV_NAME
        if not csv_path.exists():
            continue

        execution_count += 1
        with open(csv_path, newline='', encoding='utf-8') as f:
            reader = csv.DictReader(f, delimiter=';')
            for row in reader:
                base_key = (row['project'], row['class'], row['method'], row['merge commit'])

                # Filter: ignore "not-found" and blacklist
                if row.get(oa_field, "").strip().lower() == "not-found":
                    continue
                if base_key in BLACKLIST:
                    continue

                try:
                    time = round(float(row['Time']), 2)
                except ValueError:
                    continue

                key = base_key
                entries.setdefault(key, []).append(time)
                # Limit to 10 executions
                if len(entries[key]) > 10:
                    entries[key] = entries[key][:10]

    return entries, execution_count


def format_row(key, oa_inter_times, oa_inter_no_pa_times):
    row = list(key)

    row.extend([str(t) for t in oa_inter_times])
    row.append(str(round(float(mean(oa_inter_times)), 2)))
    row.append(str(round(float(median(oa_inter_times)), 2)))
    row.append(str(round(float(stdev(oa_inter_times)), 2) if len(oa_inter_times) > 1 else 0.0))

    row.extend([str(t) for t in oa_inter_no_pa_times])
    row.append(str(round(float(mean(oa_inter_no_pa_times)), 2)))
    row.append(str(round(float(median(oa_inter_no_pa_times)), 2)))
    row.append(str(round(float(stdev(oa_inter_no_pa_times)), 2) if len(oa_inter_no_pa_times) > 1 else 0.0))
    return row


def main():
    oa_inter_data, oa_inter_execs = read_csvs("ioa", "OA Inter")
    oa_inter_no_pa_data, oa_inter_no_pa_execs = read_csvs("ioa-without-pa", "OA Inter Without Pointer Analysis")

    if oa_inter_execs != oa_inter_no_pa_execs:
        print("⚠️ Mismatch in number of executions between 'OA Inter' and 'OA Inter Without Pointer Analysis'!")

    # Header
    header = [
                 "project", "class", "method", "merge_commit"
             ] + [f"oa_inter_exec_{i + 1}" for i in range(oa_inter_execs)] + ["oa_inter_mean", "oa_inter_median",
                                                                              "oa_inter_std"] \
             + [f"oa_inter_no_pa_exec_{i + 1}" for i in range(oa_inter_no_pa_execs)] + ["oa_inter_no_pa_mean",
                                                                                        "oa_inter_no_pa_median",
                                                                                        "oa_inter_no_pa_std"]

    with open(OUTPUT, "w", newline='', encoding='utf-8') as f_out:
        writer = csv.writer(f_out, delimiter=';', quoting=csv.QUOTE_MINIMAL)
        writer.writerow(header)

        # Write only keys present in both datasets and not filtered
        for key in sorted(oa_inter_data.keys()):
            if key in oa_inter_no_pa_data:
                oa_inter_times = oa_inter_data[key]
                oa_inter_no_pa_times = oa_inter_no_pa_data[key]
                writer.writerow(format_row(key, oa_inter_times, oa_inter_no_pa_times))

    print(f"✅ File successfully generated: {OUTPUT}")


if __name__ == "__main__":
    main()
