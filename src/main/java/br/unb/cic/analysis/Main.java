package br.unb.cic.analysis;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map.Entry;

import br.unb.cic.analysis.df.ReachDefinitionAnalysis;
import br.unb.cic.analysis.df.SourceSinkConfluenceAnalysis;
import br.unb.cic.analysis.df.TaintedAnalysis;
import br.unb.cic.analysis.svfa.SVFAAnalysis;
import org.apache.commons.cli.*;

import scala.collection.JavaConverters;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;

import br.unb.cic.analysis.io.DefaultReader;
import br.unb.cic.analysis.io.MergeConflictReader;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.analysis.reachability.ReachabilityAnalysis;
import br.unb.cic.diffclass.DiffClass;

public class Main {

    private Options options;
    private AbstractMergeConflictDefinition definition;
    private Set<String> targetClasses;
    private List<String> conflicts = new ArrayList<>();
    private ReachDefinitionAnalysis analysis;

    public static void main(String args[]) {
        Main m = new Main();
        try {
            m.createOptions();

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(m.options, args);
            
            String mode = "dataflow"; 
            
            if (cmd.hasOption("mode")) {
                mode = cmd.getOptionValue("mode");
            }
            if (cmd.hasOption("repo") && cmd.hasOption("commit")) {
                DiffClass module = new DiffClass();
                module.getGitRepository(cmd.getOptionValue("repo"));
                module.diffAnalysis(cmd.getOptionValue("commit"));
                m.loadDefinitionFromDiffAnalysis(module);
            } else {
                m.loadDefinition(cmd.getOptionValue("csv"));
            }
            m.runAnalysis(mode, m.parseClassPath(cmd.getOptionValue("cp")));
            
            m.exportResults();
            
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

    private String parseClassPath(String cp) {
        File f = new File(cp);
        String res = cp;
        if(f.exists() && f.isDirectory()) {
            for(File file : f.listFiles()) {
                if(file.getName().endsWith(".jar")) {
                    res += ":";
                    res += file.getAbsolutePath();
                }
            }
        }
        return res;
    }

    private void exportResults() throws Exception {
    	System.out.println(" Analysis results");
        System.out.println("----------------------------");
        
        if(conflicts.size() == 0) {
        	System.out.println(" No conflicts detected");
        	System.out.println("----------------------------");
        	return;
        }
    
        System.out.println(" Number of conflicts: " + conflicts.size());
        final String out = "out.txt"; 
        final FileWriter fw = new FileWriter(out);
        conflicts.forEach(c -> {
    		try { 
    			fw.write(c + "\n");
    		}
    		catch(Exception e) {
    			System.out.println("error exporting the results " + e.getMessage());
    		}
    	});
        fw.close();
        System.out.println(" Results exported to " + out);
        System.out.println("----------------------------");
    }
    
    private void createOptions() {
        options = new Options();
        Option classPathOption = Option.builder("cp").argName("class-path")
                .required().hasArg().desc("the classpath used in the analysis")
                .build();

        Option inputFileOption = Option.builder("csv").argName("csv")
                .hasArg().desc("the input csv files with the list of changes")
                .build();

        Option analysisOption = Option.builder("mode").argName("mode")
                .hasArg().desc("analysis mode [data-flow, tainted, reachability, svfa, confluence]")
                .build();

        Option repoOption = Option.builder("repo").argName("repo")
                .hasArg().desc("the path or url of git repository")
                .build();

        Option commitOption = Option.builder("commit").argName("commit")
                .hasArg().desc("the commit merge to analysis")
                .build();

        Option verbose = Option.builder("verbose").argName("verbose").desc("run in the verbose mode").build();

        options.addOption(classPathOption);
        options.addOption(inputFileOption);
        options.addOption(analysisOption);
        options.addOption(repoOption);
        options.addOption(commitOption);
        options.addOption(verbose);
    }

    
    private void runAnalysis(String mode, String classpath) {
    	switch(mode) {
            case "svfa"         : runSparseValueFlowAnalysis(classpath); break;
            case "reachability" : runReachabilityAnalysis(classpath); break;
            default             : runDataFlowAnalysis(classpath, mode);
    	}
    }

    private void runDataFlowAnalysis(String classpath, String mode) {
        PackManager.v().getPack("jtp").add(
                new Transform("jtp.analysis", new BodyTransformer() {
                    @Override
                    protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
                        switch(mode) {
                            case "dataflow"   : analysis = new ReachDefinitionAnalysis(body, definition); break;
                            case "tainted"    : analysis = new TaintedAnalysis(body, definition);
                            case "confluence" : analysis = new SourceSinkConfluenceAnalysis(body, definition); break;
                            default: {
                                System.out.println("Error: " + "invalid mode " + mode);
                                System.exit(-1);
                            }
                        }
                    }
                }));
        SootWrapper.builder()
                   .withClassPath(classpath)
                   .addClass(targetClasses.stream().collect(Collectors.joining(" ")))
                   .build()
                   .execute();
        if (analysis != null) {
            conflicts.addAll(analysis.getConflicts().stream().map(c -> c.toString()).collect(Collectors.toList()));
        }
    }

    /*
     * After discussing this algorithm with the researchers at
     * UFPE, we decided that we should not support this analysis
     * any more. It might lead to a huge number of false-positives.
     */
    @Deprecated
    private void runReachabilityAnalysis(String classpath) {
    	ReachabilityAnalysis analysis = new ReachabilityAnalysis(definition);
    	
    	PackManager.v().getPack("wjtp").add(new Transform("wjtp.analysis", analysis));
        soot.options.Options.v().setPhaseOption("cg.spark", "on");
        soot.options.Options.v().setPhaseOption("cg.spark", "verbose:true");

        SootWrapper.builder()
                .withClassPath(classpath)
                .addClass(targetClasses.stream().collect(Collectors.joining(" ")))
                .build()
                .execute();
        
        conflicts.addAll(analysis.getConflicts().stream().map(c -> c.toString()).collect(Collectors.toList()));
    }

    private void runSparseValueFlowAnalysis(String classpath) {
        SVFAAnalysis analysis = new SVFAAnalysis(classpath, definition);
        analysis.buildSparseValueFlowGraph();
        System.out.println(analysis.svgToDotModel());
        conflicts.addAll(JavaConverters.asJavaCollection(analysis.reportConflicts())
                .stream()
                .map(p -> p.toString())
                .collect(Collectors.toList()));
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

    private void loadDefinitionFromDiffAnalysis(DiffClass module) {
        ArrayList<Entry<String, Integer>> sourceClasses = module.getSourceModifiedClasses();
        ArrayList<Entry<String, Integer>> sinkClasses = module.getSinkModifiedClasses();
        Map<String, List<Integer>> sourceDefs = new HashMap<>();
        Map<String, List<Integer>> sinkDefs = new HashMap<>();
        targetClasses = new HashSet<>();
        for (Entry<String, Integer> change : sourceClasses) {
            addChangeFromDiffAnalysis(sourceDefs, change);
            targetClasses.add(change.getKey());
        }
        for (Entry<String, Integer> change : sinkClasses) {
            addChangeFromDiffAnalysis(sinkDefs, change);
            targetClasses.add(change.getKey());
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

    private void addChangeFromDiffAnalysis(Map<String, List<Integer>> map, Entry<String, Integer> change) {
        if(map.containsKey(change.getKey())) {
            map.get(change.getKey()).add(change.getValue());
        }
        else {
            List<Integer> lines = new ArrayList<>();
            lines.add(change.getValue());
            map.put(change.getKey(), lines);
        }
    }
}
