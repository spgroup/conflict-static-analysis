package br.unb.cic.analysis;

import br.unb.cic.analysis.df.DataFlowAnalysis;
import br.unb.cic.analysis.io.DefaultReader;
import br.unb.cic.analysis.io.MergeConflictReader;
import br.unb.cic.analysis.model.Statement;
import org.apache.commons.cli.*;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private Options options;
    private AbstractMergeConflictDefinition definition;
    private Set<String> targetClasses;
    private List<String> conflicts = new ArrayList<>();
    DataFlowAnalysis analysis;

    public static void main(String args[]) {
        Main m = new Main();
        try {
            m.createOptions();

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(m.options, args);

            if (cmd.hasOption("mode")) {
                // TODO: we should work with different modes!
            }

            m.loadDefinition(cmd.getOptionValue("csv"));
            m.runAnalysis(cmd.getOptionValue("cp"), m.conflicts);
            m.conflicts.stream().forEach(c -> System.out.println(c));
        }
        catch(ParseException e) {
            System.out.println("Error: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "java Main", m.options );
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void createOptions() {
        options = new Options();
        Option classPathOption = Option.builder("cp").argName("class-path")
                .required().hasArg().desc("the classpath used in the analysis")
                .build();

        Option inputFileOption = Option.builder("csv").argName("csv")
                .required().hasArg().desc("the input csv files with the list of changes")
                .build();

        Option analysisOption = Option.builder("mode").argName("mode")
                .hasArg().desc("analysis mode [dataflow, reachability]")
                .build();

        options.addOption(classPathOption);
        options.addOption(inputFileOption);
        options.addOption(analysisOption);
    }
    private void runAnalysis(String classPath, List<String> conflicts) {

      PackManager.v().getPack("jtp").add(
        new Transform("jtp.df", new BodyTransformer() {
           @Override
            protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
                analysis = new DataFlowAnalysis(new ExceptionalUnitGraph(body), definition);
            }
         }));
        soot.Main.main(new String[] {"-w", "-allow-phantom-refs", "-f", "J", "-keep-line-number", "-cp"
                , classPath, targetClasses.stream().collect(Collectors.joining(" "))});
        conflicts.addAll(analysis.getConflicts().stream().map(c -> c.toString()).collect(Collectors.toList()));
    }


    private void loadDefinition(String filePath) throws Exception {
        MergeConflictReader reader = new DefaultReader(filePath);
        List<ClassChangeDefinition> changes = reader.read();
        Map<String, List<Integer>> sourceDefs = new HashMap<>();
        Map<String, List<Integer>> sinkDefs = new HashMap<>();
        targetClasses = new HashSet<>();
        for(ClassChangeDefinition change : changes) {
            if(change.getType().equals(Statement.Type.SOURCE)) {
                addChange(sourceDefs, change);
            }
            else {
                addChange(sinkDefs, change);
            }
            targetClasses.add(change.getClassName());
        }
        definition = new AbstractMergeConflictDefinition() {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                return sourceDefs;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                return sinkDefs;
            }
        };
    }

    private void addChange(Map<String, List<Integer>> map, ClassChangeDefinition change) {
        if(map.containsKey(change.getClassName())) {
            map.get(change.getClassName()).add(change.getLineNumber());
        }
        else {
            List<Integer> lines = new ArrayList<>();
            lines.add(change.getLineNumber());
            map.put(change.getClassName(), lines);
        }
    }
}