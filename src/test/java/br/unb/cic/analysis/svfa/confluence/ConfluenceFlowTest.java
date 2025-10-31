package br.unb.cic.analysis.svfa.confluence;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class ConfluenceFlowTest {

    private DFPConfluenceAnalysis analysis;
    @Before
    public void configure() {
        AbstractMergeConflictDefinition definition = new AbstractMergeConflictDefinition() {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(9);
                res.put("br.unb.cic.analysis.samples.ConfluenceFlowSample", lines);
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(13);
                res.put("br.unb.cic.analysis.samples.ConfluenceFlowSample", lines);
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
        Assert.assertEquals(1, conflicts.size());
        System.out.println("Conflicts: "+conflicts.size());
        analysis.reportConflictsConfluence();
    }

}
