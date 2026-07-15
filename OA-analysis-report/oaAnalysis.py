import json
import os
import sys

from constants import PERFORMANCE_SOOT_STATS_CSV
from processors import ConflictProcessor, PerformanceAggregator
from analyzers import ConflictAnalyzer, ScenarioAnalyzer, PerformanceAnalyzer, GroundTruthComparator
from utils import extract_dataset_label_from_path


def parse_args():
    plot_enabled = False
    input_jsons = []
    labels = None
    performance_data_path = None
    subset_non_timeouts = None
    mds_ground_truth = None
    soot_results_path = None
    perf_soot_path_arg = None

    for arg in sys.argv[1:]:
        key, _, value = arg.partition("=")
        key = key.lower()
        if key == "plot":
            plot_enabled = value.lower() == "true"
        elif key == "out.json":
            input_jsons.extend([p.strip() for p in value.split(",") if p.strip()])
        elif key == "out.json2":
            input_jsons.append(value.strip())
        elif key == "labels":
            labels = value
        elif key == "performancedata":
            performance_data_path = value
        elif key == "subsetofnontimeouts":
            subset_non_timeouts = [p.strip() for p in value.split(",") if p.strip()]
        elif key == "mdsgroundtruth":
            mds_ground_truth = value
        elif key == "sootresults":
            soot_results_path = value
        elif key == "perf_soot_path":
            perf_soot_path_arg = value

    return (
        plot_enabled, input_jsons, labels, performance_data_path,
        subset_non_timeouts, mds_ground_truth, soot_results_path, perf_soot_path_arg,
    )


def _process_json(json_path):
    """Load and process a single out.json file, returning (processor, output_dir)."""
    output_dir = os.path.dirname(os.path.abspath(json_path)) or "."
    processor = ConflictProcessor(output_dir)

    with open(json_path) as f:
        data = json.load(f)

    if data and isinstance(data, list):
        for idx, entry in enumerate(data):
            if isinstance(entry, dict) and "conflicts" in entry:
                processor.process_conflicts(entry.get("conflicts", []), start_idx=idx)
            else:
                raise Exception("Invalid conflict data format")

    for idx in processor.conflict_data["jar_map"]:
        if len(processor.conflict_data["jar_map"][idx]) > 1:
            raise Exception(
                "Multiple scenario jars found",
                processor.conflict_data["jar_map"][idx],
                idx,
            )

    processor.save_results()
    return processor, output_dir


def main():
    (
        plot_enabled, input_jsons, labels, performance_data_path,
        subset_non_timeouts, mds_ground_truth, soot_results_path, perf_soot_path_arg,
    ) = parse_args()

    if not any([input_jsons, performance_data_path, subset_non_timeouts,
                soot_results_path, perf_soot_path_arg]):
        print(
            "Error: At least one of out.json, performancedata, sootresults, "
            "perf_soot_path, or subsetOfNonTimeouts must be provided"
        )
        sys.exit(1)

    label_list = [l.strip() for l in labels.split(",")] if labels else []
    processors, output_dirs = [], []

    for json_path in input_jsons:
        processor, output_dir = _process_json(json_path)
        processors.append(processor)
        output_dirs.append(output_dir)

    performance_report_dir = None
    if performance_data_path:
        perf_aggregator = PerformanceAggregator(performance_data_path)
        performance_report_dir = perf_aggregator.aggregate()

    if processors:
        conflict_analyzer = ConflictAnalyzer()
        scenario_analyzer = ScenarioAnalyzer()

        while len(label_list) < len(output_dirs):
            label_list.append(extract_dataset_label_from_path(output_dirs[len(label_list)]))

        if len(processors) == 1:
            conflict_analyzer.analyze(plot=plot_enabled, output_dir=output_dirs[0])
            scenario_analyzer.analyze(plot=plot_enabled, output_dir=output_dirs[0])
        elif len(processors) == 2:
            conflict_analyzer.analyze_compare(
                plot=plot_enabled,
                output_dir=output_dirs[0], output_dir2=output_dirs[1],
                label1=label_list[0], label2=label_list[1],
            )
            scenario_analyzer.analyze_compare(
                plot=plot_enabled,
                output_dir=output_dirs[0], output_dir2=output_dirs[1],
                label1=label_list[0], label2=label_list[1],
            )
        else:
            conflict_analyzer.analyze_compare_multiple(
                plot=plot_enabled, output_dirs=output_dirs, labels=label_list,
            )
            scenario_analyzer.analyze_compare_multiple(
                plot=plot_enabled, output_dirs=output_dirs, labels=label_list,
            )

    if performance_report_dir:
        perf_analyzer = PerformanceAnalyzer()
        perf_analyzer.analyze(plot=plot_enabled, output_dir=performance_report_dir)

    if mds_ground_truth:
        if perf_soot_path_arg:
            perf_soot_path = perf_soot_path_arg
            gt_output_dir = os.path.dirname(os.path.abspath(perf_soot_path_arg))
        elif soot_results_path:
            perf_soot_path = soot_results_path
            gt_output_dir = os.path.dirname(os.path.abspath(soot_results_path))
        elif performance_report_dir:
            perf_soot_path = os.path.join(performance_report_dir, PERFORMANCE_SOOT_STATS_CSV)
            gt_output_dir = performance_report_dir
        else:
            perf_soot_path = None
            gt_output_dir = None

        if perf_soot_path and os.path.exists(perf_soot_path):
            gt_comparator = GroundTruthComparator()
            gt_comparator.compare(
                perf_soot_path=perf_soot_path,
                ground_truth_path=mds_ground_truth,
                output_dir=gt_output_dir,
            )
        elif perf_soot_path:
            print(f"Error: Soot results file not found at {perf_soot_path}")
        else:
            print("Note: Ground truth comparison requires perf_soot_path, sootresults, or performancedata")

    if subset_non_timeouts:
        perf_analyzer = PerformanceAnalyzer()
        perf_analyzer.analyze_subset_non_timeouts(subset_non_timeouts)


if __name__ == "__main__":
    main()
