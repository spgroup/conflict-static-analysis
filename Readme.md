# Semantic Merge Conflicts Soot Library

This project aims to implement a library of soot analysis to detect semantic merge conflicts.
Current supported algorithms:

   * Intraprocedural def-use conflicts: This algorithm identifies conflicts
    that occur when a contribution from "left" defines a variable
    that a contribution from "right" uses. Its implementation is mostly based on
    intraprocedural dataflow analysis, and is quite similar to the 
    reach definition analysis. 

   * Reachability conflicts: This algorithm identifies conflicts that occur
   when there is a interprocedural flow from a left statement contribution from
   to a right statement contribution

   * SVFA: This algorithm identifies data flows from source and sink statements
   using a interpreocedura sparse value flow graph. It is too exeperimental yet.

## Usage

### Build

To build the project, you will need Maven and Java 8 (or higher).

First, build and install the [svfa-soot](https://github.com/rbonifacio/svfa-scala) implementation. 

After that, run the following commands:

```SHELL
mvn clean install -DskipTests
```

You can also create an executable jar file using: 

```SHELL
mvn compile assemly:single
```
### Execute

To execute, you need to specify the Java project to analyze and a set of lines changed in that project.
This set could be specified using a .csv file, but if the project uses Git, you can use the DiffClass Analyzer project.
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

