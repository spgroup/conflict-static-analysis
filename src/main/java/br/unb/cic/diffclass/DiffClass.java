package br.unb.cic.diffclass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.FileUtils;

public class DiffClass {
  private Repository repository = null;
  // private ArrayList<Entry<String, Integer>> modifiedFiles = new ArrayList<>();
  private ArrayList<Entry<String, Integer>> modifiedClasses = new ArrayList<>();
  private ArrayList<Entry<String, Integer>> sourceModifiedClasses = new ArrayList<>();
  private ArrayList<Entry<String, Integer>> sinkModifiedClasses = new ArrayList<>();
  private static String chunckHeaderRegex = "@@ \\-(\\d*),(\\d*) \\+(\\d*),(\\d*) @@\\n";

  public DiffClass() {}

  public void getLocalGitRepository(String path) {
    try {
      if (path.endsWith("/")) {
          Git openComamand = Git.open(new File(path + ".git"));
          this.repository = openComamand.getRepository();
      } else {
          Git openComamand = Git.open(new File(path + "/.git"));
          this.repository = openComamand.getRepository();
      }
      // FileRepositoryBuilder builder = new FileRepositoryBuilder();
      // this.repository = builder.setGitDir(new File(path)).readEnvironment().findGitDir().build();
    } catch (IOException e) {
      // call here the getRemoteGitRepository()
      e.printStackTrace();
    }
  }

  public void getRemoteGitRepository(String url) {
    try {
      File repoDir = new File("git-repo");
      if (repoDir.exists()) FileUtils.delete(repoDir, 1);
      Git cloneComamand = Git.cloneRepository().setURI(url).setDirectory(repoDir).call();

      this.repository = cloneComamand.getRepository();

    } catch (GitAPIException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void getGitRepository(String pathOrUrl) {
      if (pathOrUrl.startsWith("http")) {
          getRemoteGitRepository(pathOrUrl);
      } else {
          getLocalGitRepository(pathOrUrl);
      }
  }

  public AbstractTreeIterator getTree(String hash) {
    CanonicalTreeParser treeParser = null;
    try (RevWalk walk = new RevWalk(this.repository)) {

      RevCommit commit = walk.parseCommit(this.repository.resolve(hash));
      RevTree tree = walk.parseTree(commit.getTree().getId());

      treeParser = new CanonicalTreeParser();
      try (ObjectReader reader = this.repository.newObjectReader()) {
        treeParser.reset(reader, tree.getId());
      }

      walk.dispose();

      return treeParser;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return treeParser;
  }

  public boolean repositoryWasCloned() {
    return this.repository != null;
  }

  public ArrayList<Entry<String, Integer>> getCommitsDifference(String hash1, String hash2) {
    ArrayList<Entry<String, Integer>> modifiedClasses = new ArrayList<>();
    try {
      Git git = new Git(this.repository);

      List<DiffEntry> diffs =
          git.diff().setOldTree(this.getTree(hash2)).setNewTree(this.getTree(hash1)).call();

      ArrayList<Entry<String, Integer>> modifiedFiles = new ArrayList<>();

      System.out.println("\nFound: " + diffs.size() + " differences");
      for (DiffEntry diff : diffs) {
        System.out.println(diff);
        DiffFormatter formatter = new DiffFormatter(System.out);
        formatter.setRepository(this.repository);
        formatter.format(diff);
        // the chunck analysis start here
        this.diffOutputAnalysis(diff, modifiedFiles);
        formatter.close();
      }

      modifiedClasses = this.getClassNames(modifiedFiles);
      git.close();

    } catch (GitAPIException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return modifiedClasses;
  }

  public void diffOutputAnalysis(DiffEntry diff, ArrayList<Entry<String, Integer>> modifiedFiles) {
    String modifiedFileRegex = "DiffEntry\\[MODIFY (.*)\\]";

    try {
      if (diff.toString().matches(modifiedFileRegex)) {
        String filename = diff.toString().replaceAll(modifiedFileRegex, "$1");

        // Transform the stream to a string
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DiffFormatter formatter = new DiffFormatter(output);
        formatter.setRepository(this.repository);
        formatter.format(diff);

        // Analysis of diff output
        String diffOutput = output.toString();
        String[] chuncks = diffOutput.split(chunckHeaderRegex);

        Pattern chunckHeaderPattern = Pattern.compile(chunckHeaderRegex);
        Matcher chunckHeaderMatcher = chunckHeaderPattern.matcher(diffOutput);
        int chunckIndex = 1;

        while (chunckHeaderMatcher.find()) {
          String chunckHeader = chunckHeaderMatcher.group();

          this.chunckAnalysis(filename, chunckHeader, chuncks[chunckIndex], modifiedFiles);

          chunckIndex++;
        }

        formatter.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void chunckAnalysis(
      String filename,
      String header,
      String body,
      ArrayList<Entry<String, Integer>> modifiedFiles) {
    int currentLineNumber = Integer.parseInt(header.replaceAll(chunckHeaderRegex, "$1"));

    String[] chunckLines = body.split("\\r?\\n");

    int lhsLineCounter = currentLineNumber;
    int rhsLineCounter = currentLineNumber;
    ArrayList<Integer> addedLines = new ArrayList<Integer>();
    ArrayList<Integer> removedLines = new ArrayList<Integer>();

    for (String line : chunckLines) {
      if (line.startsWith("-")) {
        // removedLines.add(currentLineNumber);
        removedLines.add(lhsLineCounter);
        lhsLineCounter++;
      } else if (line.startsWith("+")) {
        // addedLines.add(currentLineNumber);
        addedLines.add(rhsLineCounter);
        rhsLineCounter++;
      } else {
        if (!addedLines.isEmpty() && !removedLines.isEmpty()) {
          // get modified lines
          for (int lineNumber : removedLines) {
            if (addedLines.contains(lineNumber)) {
              modifiedFiles.add(new SimpleEntry<>(filename, lineNumber));
              addedLines.remove(addedLines.indexOf(lineNumber));
              currentLineNumber++;
            }
          }
          // get added lines
          for (int lineNumber : addedLines) {
            modifiedFiles.add(new SimpleEntry<>(filename, lineNumber));
            currentLineNumber++;
          }
        } else if (!addedLines.isEmpty()) {
          // get added lines
          for (int lineNumber : addedLines) {
            modifiedFiles.add(new SimpleEntry<>(filename, lineNumber));
            currentLineNumber++;
          }
        }
        removedLines.clear();
        addedLines.clear();
        currentLineNumber++;
        lhsLineCounter = currentLineNumber;
        rhsLineCounter = currentLineNumber;
      }
    }
  }

  public ArrayList<Entry<String, Integer>> getClassNames(
      ArrayList<Entry<String, Integer>> modifiedFiles) {
    ArrayList<Entry<String, Integer>> modifiedClasses = new ArrayList<>();
    if (!modifiedFiles.isEmpty()) {
      String packageRegex = ".*package (?:^\\w+|\\w+\\.\\w+)+;.*";
      String simplePackageRegex = ".*package (.*);.*";
      String classHeaderRegex = ".*class\\s+(\\w+)\\s+";

      for (Entry<String, Integer> change : modifiedFiles) {
        Path filePath = Paths.get("git-repo/" + change.getKey());

        try {
          String fileContent = new String(Files.readAllBytes(filePath));
          String[] fileLines = fileContent.split("\\r?\\n");

          String linesAbove = "";
          for (int i = 0; i < change.getValue(); i++) {
            linesAbove = linesAbove.concat(fileLines[i] + "\n");
          }

          Pattern classPattern = Pattern.compile(classHeaderRegex);
          Matcher classMatcher = classPattern.matcher(linesAbove);
          String className = "";
          while (classMatcher.find()) {
            className = classMatcher.group();
          }
          className = className.replaceAll(classHeaderRegex, "$1");

          Pattern packagePattern = Pattern.compile(packageRegex);
          Matcher packageMatcher = packagePattern.matcher(linesAbove);
          packageMatcher.find();
          String packageName = packageMatcher.group();
          packageName = packageName.replaceAll(simplePackageRegex, "$1");

          className = packageName + "." + className;
          modifiedClasses.add(new SimpleEntry<>(className, change.getValue()));

        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return modifiedClasses;
  }

  public void printClassesModified(ArrayList<Entry<String, Integer>> modifiedClasses) {
    if (!modifiedClasses.isEmpty()) {
      System.out.println("Modified Classes:");
      for (Entry<String, Integer> classMod : modifiedClasses) {
        System.out.println(classMod.getKey() + ": " + classMod.getValue());
      }
    }
  }

  public void resultOfDiffInCSV() {
    try {
      ArrayList<String> classNames = new ArrayList<>();
      if (!this.sourceModifiedClasses.isEmpty()) {
        for (Entry<String, Integer> classMod : this.sourceModifiedClasses) {
          String statement = classMod.getKey() + ",source," + classMod.getValue();
          classNames.add(statement);
        }
      }

      if (!this.sinkModifiedClasses.isEmpty()) {
        for (Entry<String, Integer> classMod : this.sinkModifiedClasses) {
          String statement = classMod.getKey() + ",sink," + classMod.getValue();
          classNames.add(statement);
        }
      }
      if (!classNames.isEmpty()) {
        Path file = Paths.get("./diff-output.csv");
        Files.write(file, classNames, Charset.forName("UTF-8"));
      } else {
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public ArrayList<String> getMergeCommitParents(String hash) {
    ArrayList<String> mergeCommitParents = new ArrayList<>();
    try (RevWalk walk = new RevWalk(this.repository)) {
      RevCommit commit = walk.parseCommit(this.repository.resolve(hash));
      for (RevCommit parentCommit : commit.getParents()) {
        parentCommit = walk.parseCommit(parentCommit.getId());
        mergeCommitParents.add(parentCommit.getName());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return mergeCommitParents;
  }

  public String getMergeBaseCommit(String lhsCommitHash, String rhsCommitHash) {
    try (RevWalk walk = new RevWalk(this.repository)) {
      walk.setRevFilter(RevFilter.MERGE_BASE);
      RevCommit lhsCommit = walk.parseCommit(this.repository.resolve(lhsCommitHash));
      RevCommit rhsCommit = walk.parseCommit(this.repository.resolve(rhsCommitHash));
      walk.markStart(lhsCommit);
      walk.markStart(rhsCommit);
      RevCommit mergeBaseCommit = walk.next();
      mergeBaseCommit = walk.parseCommit(mergeBaseCommit.getId());
      return mergeBaseCommit.getName();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public void diffAnalysis(String hashOfMergeCommit) {
    ArrayList<String> hashOfMergeCommitParents = null;
    hashOfMergeCommitParents = this.getMergeCommitParents(hashOfMergeCommit);

    if (!hashOfMergeCommitParents.isEmpty()) {
      String hashOfSourceCommit = hashOfMergeCommitParents.get(0);
      String hashOfSinkCommit = hashOfMergeCommitParents.get(1);
      String hashOfMergeBaseCommit = null;
      hashOfMergeBaseCommit = this.getMergeBaseCommit(hashOfSourceCommit, hashOfSinkCommit);

      if (hashOfMergeBaseCommit != null) {
        this.sourceModifiedClasses =
            this.getCommitsDifference(hashOfSourceCommit, hashOfMergeBaseCommit);
        this.sinkModifiedClasses =
            this.getCommitsDifference(hashOfSinkCommit, hashOfMergeBaseCommit);

        // Verify and Solve Merge Conflit
        ArrayList<Entry<String, Integer>> conflictModifieds = this.verifyMergeConflicts();
        if (!conflictModifieds.isEmpty()) {
          this.solveMergeConflicts(
              conflictModifieds, hashOfMergeCommit, hashOfSourceCommit, hashOfSinkCommit);
        }

        // System.out.println("\n --- Source Side --- ");
        // this.printClassesModified(this.sourceModifiedClasses);
        // System.out.println("\n --- Sink Side --- ");
        // this.printClassesModified(this.sinkModifiedClasses);
      }
    }
  }

  public ArrayList<Entry<String, Integer>> verifyMergeConflicts() {
    ArrayList<Entry<String, Integer>> conflictModifieds = new ArrayList<>();
    if (!this.sourceModifiedClasses.isEmpty() && !this.sinkModifiedClasses.isEmpty()) {
      // System.out.println("\n --- Merge Conflicts ---");
      for (Entry<String, Integer> change : this.sourceModifiedClasses) {
        if (this.sinkModifiedClasses.contains(change)) {
          conflictModifieds.add(new SimpleEntry<>(change.getKey(), change.getValue()));
        }
      }
    }
    return conflictModifieds;
  }

  public void solveMergeConflicts(
      ArrayList<Entry<String, Integer>> detectedConflicts,
      String hashOfMergeCommit,
      String hashOfSourceCommit,
      String hashOfSinkCommit) {

    ArrayList<Entry<String, Integer>> sourceSideMergeResult = new ArrayList<>();
    ArrayList<Entry<String, Integer>> sinkSideMergeResult = new ArrayList<>();

    sourceSideMergeResult = this.getCommitsDifference(hashOfSourceCommit, hashOfMergeCommit);
    if (!sourceSideMergeResult.isEmpty()) {
      for (Entry<String, Integer> change : sourceSideMergeResult) {
        if (this.sourceModifiedClasses.contains(change)) {
          int indexOfConflict = this.sourceModifiedClasses.indexOf(change);
          this.sourceModifiedClasses.remove(indexOfConflict);
        }
      }
    }

    sinkSideMergeResult = this.getCommitsDifference(hashOfSinkCommit, hashOfMergeCommit);
    if (!sinkSideMergeResult.isEmpty()) {
      for (Entry<String, Integer> change : sinkSideMergeResult) {
        if (this.sinkModifiedClasses.contains(change)) {
          int indexOfConflict = this.sinkModifiedClasses.indexOf(change);
          this.sinkModifiedClasses.remove(indexOfConflict);
        }
      }
    }

    // System.out.println("\n --- Source Side Merge Conflict Result --- ");
    // this.printClassesModified(sourceSideMergeResult);
    // System.out.println("\n --- Sink Side Merge Conflict Result --- ");
    // this.printClassesModified(sinkSideMergeResult);

    // this.resultOfDiffInCSV();
  }

    public ArrayList<Entry<String, Integer>> getSourceModifiedClasses() {
        return this.sourceModifiedClasses;
    }

    public ArrayList<Entry<String, Integer>> getSinkModifiedClasses() {
        return this.sinkModifiedClasses;
    }
}
