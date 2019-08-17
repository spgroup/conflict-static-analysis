# Semantic Merge Conflicts Soot Library

This project aims to implement a library of soot analysis to detect semantic merge conflicts.
Current supported algorithms:

   * Intraprocedural use-def conflicts: This algorithm identifies conflicts
    that occur when a contribution from "left" defines a variable
    that a contribution of "right" uses. Its implementation is mostly based on
    intraprocedural dataflow analysis.

   * Reachability conflicts: This algorithm identifies conflicts that occur
   when there is a interprocedural flow path from a left statement contribution from
   to a right statement contribution



## TODO

   * when computing the number of conflicts, consider that multiple JIMPLE statements might appear in the same line
   For this reason, we would rather use a data structure to avoid reporting multiple conflicts from the same pairs
   of source-sink lines.

## Usage

### Build

To build the project, you will need of Maven and Java 8 or higher. Run the command below:

```SHELL
mvn clean install -DskipTests
```

### Execute

To execute, you will need of a Java project to analyze and the set of lines changed in this project.
This set could be in a .csv file, but if the project utilize Git, you can use the DiffClass Analyzer,
for this you will need of will need of hash of merge commit to get the set of modified lines. See the CLI parameters below:


#### -cp
The path of the folder containing the .class files of the project to be analyzed.
Remember, you need to compile the project to be analyzed. This parameters is required.

#### -csv
The input csv files with the list of changes. This parameter or -commit must be provide.

#### -commit
the merge commit hash. This parameter or -csv must be provide.

#### -mode
Analysis mode: dataflow or reachability.

#### -repo
The folder path or URL of git project to be analyzed.

Usage example a Java project, with a .csv file.

```SHELL
mvn exec:java -Dexec.mainClass="br.unb.cic.analysis.Main" \
-Dexec.args="-csv /path/of/csv/file.csv -cp /path/of/class/files/folder"
```

Usage example with a local Java git project, without a .csv file.

```SHELL
mvn exec:java -Dexec.mainClass="br.unb.cic.analysis.Main" \
-Dexec.args="-repo /path/of/project -commit <hash-of-merge-commit> -cp /path/of/class/files/folder"
```

