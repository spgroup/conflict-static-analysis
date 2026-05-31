import os


def extract_dataset_label_from_path(path):
    """Extract a human-readable dataset label from a result directory path.

    Recognises 'RefDataset' / 'MergeDataset' keywords; falls back to the
    immediate parent directory name.
    """
    for segment in [path, os.path.basename(os.path.dirname(path))]:
        if "RefDataset" in segment:
            return "Ref Dataset"
        if "MergeDataset" in segment:
            return "Merge Dataset"
    return os.path.basename(os.path.dirname(path))
