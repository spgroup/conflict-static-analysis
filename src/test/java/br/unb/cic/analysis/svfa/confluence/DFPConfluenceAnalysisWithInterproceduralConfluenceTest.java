package br.unb.cic.analysis.svfa.confluence;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;

import java.util.*;

public class DFPConfluenceAnalysisWithInterproceduralConfluenceTest {

    private DFPConfluenceAnalysis analysis;
    @Before
    public void configure() {
        AbstractMergeConflictDefinition definition = new AbstractMergeConflictDefinition() {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(10);
                res.put("br.unb.cic.analysis.samples.InterproceduralConfluenceSample", lines);
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(12);
                res.put("br.unb.cic.analysis.samples.InterproceduralConfluenceSample", lines);
                return res;
            }
        };

        String cp = "target/test-classes";
        analysis = new DFPConfluenceAnalysis(cp, definition, true);
    }

    @Ignore
    public void testSVFAConfluentAnalysisExpectingOneConflict() {
        analysis.execute(false);
        Set<ConfluenceConflict> conflicts = analysis.getConfluentConflicts();
        Assert.assertEquals(conflicts.size(), 1);
    }
}
