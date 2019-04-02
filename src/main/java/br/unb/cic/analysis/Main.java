package br.unb.cic.analysis;

import br.unb.cic.analysis.df.Collector;
import br.unb.cic.analysis.df.DataFlowAnalysis;
import br.unb.cic.analysis.io.DefaultReader;
import br.unb.cic.analysis.io.MergeConflictReader;
import br.unb.cic.analysis.model.Statement;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    DataFlowAnalysis analysis;
    private AbstractMergeConflictDefinition definition;
    private Set<String> targetClasses;

    public static void main(String args[]) {
        try {
            if (args.length != 2) {
                System.out.println("expecting a class path argument and a file with a list of changes");
                System.exit(1);
            }

            Main m = new Main();
            m.loadDefinition(args[1]);
            m.runAnalysis(args[0]);
            Collector.instance().getConflicts().stream().forEach(c -> System.out.println(c));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void runAnalysis(String classPath) {
      PackManager.v().getPack("jtp").add(
        new Transform("jtp.df", new BodyTransformer() {
                    @Override
                    protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
          analysis = new DataFlowAnalysis(new ExceptionalUnitGraph(body), definition);
         }
         }));
        soot.Main.main(new String[] {"-w", "-allow-phantom-refs", "-f", "J", "-keep-line-number", "-cp"
                , classPath, targetClasses.stream().collect(Collectors.joining(" "))});
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
