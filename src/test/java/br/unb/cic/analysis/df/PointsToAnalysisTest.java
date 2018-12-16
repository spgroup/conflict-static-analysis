package br.unb.cic.analysis.df;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Pair;
import br.unb.cic.analysis.model.Statement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import soot.*;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.util.*;

public class PointsToAnalysisTest {

    private PointsToAnalysis analysis;

    @Before
    public void configure() {
        G.reset();
        Collector.instance().clear();
        AbstractMergeConflictDefinition definition = new AbstractMergeConflictDefinition() {
            @Override
            protected List<Pair<String, List<Integer>>> sourceDefinitions() {
                List<Pair<String, List<Integer>>> res = new ArrayList<>();
                List<Integer> lines = Arrays.asList(new Integer[]{17});
                res.add(new Pair("br.unb.cic.analysis.samples.InterproceduralPointsTo", lines));
                return res;
            }

            @Override
            protected List<Pair<String, List<Integer>>> sinkDefinitions() {
                List<Pair<String, List<Integer>>> res = new ArrayList<>();
                List<Integer> lines = Arrays.asList(new Integer[]{23});
                res.add(new Pair("br.unb.cic.analysis.samples.InterproceduralPointsTo", lines));
                return res;
            }
        };

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new SceneTransformer() {
                    @Override
                    protected void internalTransform(String phaseName, Map<String, String> options) {
                        if(!Scene.v().hasPointsToAnalysis()) {
                            throw new RuntimeException("No points to analysis available");
                        }
                        for(SootClass c: Scene.v().getApplicationClasses()) {
                            for(SootMethod m: c.getMethods()) {
                                analysis = new PointsToAnalysis(new ExceptionalUnitGraph(m.getActiveBody()), definition);
                            }
                        }
                    }
                })
        );
        Set<SootMethod> entryPoints = new HashSet<>();

        for(Statement s: definition.getSourceStatements()) {
            entryPoints.add(s.getSootMethod());
            Scene.v().forceResolve(s.getSootMethod().getDeclaringClass().getName(), SootClass.BODIES);
        }
        Scene.v().setEntryPoints(new ArrayList<>(entryPoints));
        String cp = "target/test-classes";
        String targetClass = "br.unb.cic.analysis.samples.InterproceduralPointsTo";
		Main.main(new String[] {"-w", "-allow-phantom-refs", "-f", "J", "-keep-line-number",  "-p", "cg.spark", "on", "use-original-names:true", "-cp", cp,  targetClass});
    }

    @Test
    public void testDataFlowAnalysis() {
        Assert.assertNotNull(analysis);
        Assert.assertNotNull(analysis.getConflicts());
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

}
