package br.unb.cic.analysis;

import br.unb.cic.analysis.df.Collector;
import br.unb.cic.analysis.df.DataFlowAnalysis;
import br.unb.cic.analysis.model.Pair;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    private DataFlowAnalysis analysisExpectingOneConflict;
    AbstractMergeConflictDefinition definition;

    public static void main(String args[]) {
        if(args.length != 1) {
            System.out.println("expecting a class path argument");
            System.exit(1);
        }

        Main m = new Main();
        m.loadDefinition();
        m.runAnalysis(args[0]);
        Collector.instance().getConflicts().stream().forEach(c -> System.out.println(c));
    }

    private void runAnalysis(String classPath) {
      PackManager.v().getPack("jtp").add(
        new Transform("jtp.df", new BodyTransformer() {
                    @Override
                    protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
          analysisExpectingOneConflict = new DataFlowAnalysis(new ExceptionalUnitGraph(body), definition);
         }
         }));
        soot.Main.main(new String[] {"-w", "-allow-phantom-refs", "-f", "J", "-keep-line-number", "-cp", classPath, targetClasses().stream().collect(Collectors.joining(" "))});
    }

    private List<String> targetClasses() {
        List<String> res = new ArrayList<>();
        res.add("com.orientechnologies.orient.core.cache.OLocalRecordCache");
        return res;
    }
    private void loadDefinition() {
        definition = new AbstractMergeConflictDefinition() {
            @Override
            protected List<Pair<String, List<Integer>>> sourceDefinitions() {
                List<Pair<String, List<Integer>>> res = new ArrayList<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(46);
                res.add(new Pair("com.orientechnologies.orient.core.cache.OLocalRecordCache", lines));
                return res;
            }

            @Override
            protected List<Pair<String, List<Integer>>> sinkDefinitions() {
                List<Pair<String, List<Integer>>> res = new ArrayList<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(48);
                res.add(new Pair("com.orientechnologies.orient.core.cache.OLocalRecordCache", lines));
                return res;
            }
        };
    }
}
