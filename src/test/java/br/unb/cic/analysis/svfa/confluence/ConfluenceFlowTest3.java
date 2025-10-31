package br.unb.cic.analysis.svfa.confluence;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class ConfluenceFlowTest3 {

    private DFPConfluenceAnalysis analysis;
    @Before
    public void configure() {
        AbstractMergeConflictDefinition definition = new AbstractMergeConflictDefinition() {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(9);
                res.put("br.unb.cic.analysis.samples.ConfluenceFlowSample3", lines);
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(10);
                res.put("br.unb.cic.analysis.samples.ConfluenceFlowSample3", lines);
                return res;
            }
        };

        String cp = "target/test-classes";
        analysis = new DFPConfluenceAnalysis(cp, definition, true);
    }

    @Test
    public void testSVFAConfluentAnalysisExpectingOneConflict() {
        analysis.execute(false, "SPARK");
        Set<ConfluenceConflict> conflicts = analysis.getConfluentConflicts();
        Assert.assertTrue(conflicts.size() >= 1);
        System.out.println("Conflicts: "+conflicts.size());
        analysis.reportConflictsConfluence();
    }

}
