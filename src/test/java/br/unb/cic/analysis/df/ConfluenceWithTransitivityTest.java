package br.unb.cic.analysis.df;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.SootWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import soot.*;
import soot.options.Options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfluenceWithTransitivityTest {

    private ConfluentTaintedAnalysis analysis;

    @Before
    public void configure() {
        G.reset();
        Collector.instance().clear();

        AbstractMergeConflictDefinition definition = new AbstractMergeConflictDefinition() {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(9);
                res.put("br.unb.cic.analysis.samples.ConfluenceWithTransitivitySample", lines);
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(13);
                res.put("br.unb.cic.analysis.samples.ConfluenceWithTransitivitySample", lines);
                return res;
            }
        };

        PackManager.v().getPack("jtp").add(
		    new Transform("jtp.zeroConflict", new BodyTransformer() {
				@Override
				protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
					analysis = new ConfluentTaintedAnalysis(body, definition);
				}
            		    }));
        String cp = "target/test-classes";
        String targetClass = "br.unb.cic.analysis.samples.ConfluenceWithTransitivitySample";

        PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");

        SootWrapper.builder().withClassPath(cp).addClass(targetClass).build().execute();
    }

    @Test
    public void testDataFlowAnalysisExpectingOneConflict() {
        Assert.assertEquals(1, analysis.getConflicts().size());
    }
}
