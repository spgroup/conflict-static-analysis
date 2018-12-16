package br.unb.cic.analysis.df;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import soot.*;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataFlowAnalysisZeroConflictTest {

    private DataFlowAnalysis analysisExpectingZeroConflict;

    @Before
    public void configure() {
        G.reset();
        Collector.instance().clear();

        AbstractMergeConflictDefinition definition = new AbstractMergeConflictDefinition() {
            @Override
            protected List<Pair<String, List<Integer>>> sourceDefinitions() {
                List<Pair<String, List<Integer>>> res = new ArrayList<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(19);
                res.add(new Pair("br.unb.cic.analysis.samples.IntraproceduralDataFlow", lines));
                return res;
            }

            @Override
            protected List<Pair<String, List<Integer>>> sinkDefinitions() {
                List<Pair<String, List<Integer>>> res = new ArrayList<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(26);
                res.add(new Pair("br.unb.cic.analysis.samples.IntraproceduralDataFlow", lines));
                return res;
            }
        };

        PackManager.v().getPack("jtp").add(
                new Transform("jtp.zeroConflict", new BodyTransformer() {
                    @Override
                    protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
                        analysisExpectingZeroConflict = new DataFlowAnalysis(new ExceptionalUnitGraph(body), definition);
                       }
                }));

        String cp = "target/test-classes";
        String targetClass = "br.unb.cic.analysis.samples.IntraproceduralDataFlow";
		Main.main(new String[] {"-w", "-allow-phantom-refs", "-f", "J", "-keep-line-number", "-cp", cp, targetClass});
    }

    @Test
    public void testDataFlowAnalysisExpectingZeroConflict() {
        Assert.assertNotNull(analysisExpectingZeroConflict);
        Assert.assertNotNull(analysisExpectingZeroConflict.getConflicts());
        Assert.assertEquals(0, analysisExpectingZeroConflict.getConflicts().size());
    }

}
