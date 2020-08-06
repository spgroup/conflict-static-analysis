# Static Analyses Algorithms for Detecting Semantic Conflicts

This project aims to implement a library of soot analysis to detect semantic merge conflicts.
Current supported algorithms:

   * Intraprocedural def-use conflicts: This algorithm identifies conflicts
       that occur when a contribution from "left" defines a variable
           that a contribution from "right" uses. Its implementation is mostly based on
	       intraprocedural dataflow analysis, and is quite similar to the
	           reach definition analysis
		   
   * Tainted analysis conflicts: This algorithm is similar to the intraprocedural def-use algorithm, but also supports "transitive" conflicts, that occur when a contribution from "left" defines a variable that is used to define another variable that is used by a contribution from "right" 

   * Source Sink Confluence conflicts: This algorithm is similar to the intraprocedural def-use algorithm, but it indicates a conflicts if it identifies data-flow from 
    source and sink definition to a common non annotated use

   * Reachability conflicts: This algorithm identifies conflicts that occur
      when there is a interprocedural flow from a left statement contribution from
         to a right statement contribution

   * SVFA: This algorithm identifies data flows from source and sink statements
      using a interpreocedura sparse value flow graph. It is too exeperimental yet.

## Usage

### Build

To build the project, you will need Maven and Java 8 (or higher).

You will need create a token on [GitHub](https://docs.github.com/pt/github/authenticating-to-github/creating-a-personal-access-token) to authenticate the use of the dependencies. Check the token's permissions.

Create a file called settings.xml in /home/.m2 and insert your GIT_HUB_USER and GIT_HUB_TOKEN into the following file.

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>

  <profiles>
    <profile>
      <id>github</id>
      <repositories>
        <repository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </repository>
        <repository>
          <id>spg</id>
          <name>spgroup</name>
          <url>https://maven.pkg.github.com/spgroup/soot</url>
        </repository>
	<repository>
          <id>svfa</id>
          <name>svfa repository</name>
          <url>https://maven.pkg.github.com/rbonifacio/svfa-scala</url>
        </repository>

      </repositories>
    </profile>
  </profiles>

  <servers>
    <server>
      <id>spg</id>
      <username>GIT_HUB_USER</username>
      <password>GIT_HUB_TOKEN</password>
    </server>
     <server>
      <id>svfa</id>
      <username>GIT_HUB_USER</username>
      <password>GIT_HUB_TOKEN</password>
    </server>
  </servers>
</settings>
```

Clone the repository and than run the following commmand. 

```SHELL
mvn clean install -DskipTests
```

You can also create an executable jar file using:

```SHELL
mvn compile assembly:single
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
Analysis mode: dataflow, reachability, svfa or tainted.

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

