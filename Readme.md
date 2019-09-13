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

To build the project, you will need Maven and Java 8 (or higher). Run the command below:

```SHELL
mvn clean install -DskipTests
```

You can also create an executable jar file using: 

```SHELL
mvn compile assemly:single
```
### Execute

To execute, you need a Java project to analyze and a set of lines changed in this project.
This set could be in a .csv file, but if the project uses Git, you can use the DiffClass Analyzer project.
In this case, tyou need the hash of a merge commit to get the set of modified lines. See the CLI parameters below:


#### -cp
The path of the folder containing the .class files of the project to be analyzed.
Remember, you need to compile the project under analysis. This parameters is required.

#### -csv
The input csv files with the list of changes. You must provider either this parameter or the ```-commit``` parameter.

#### -commit
the merge commit hash. See the comment above regarding the -csv parameter.

#### -mode
Analysis mode: dataflow or reachability.

#### -repo
The folder path or URL of the git project under analysis.

Usage example of a Java project, with a .csv file.

```SHELL
mvn exec:java -Dexec.mainClass="br.unb.cic.analysis.Main" \
-Dexec.args="-csv /path/of/csv/file.csv -cp /path/of/class/files/folder"
```

Usage example with of a local Java git project, without a .csv file.

```SHELL
mvn exec:java -Dexec.mainClass="br.unb.cic.analysis.Main" \
-Dexec.args="-repo /path/of/project -commit <hash-of-merge-commit> -cp /path/of/class/files/folder"
```

