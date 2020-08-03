package br.unb.cic.analysis.svfa.confluence;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class SVFAConfluenceWithTransitivityAnalysisTest {

    private SVFAConfluenceAnalysis analysis;
    @Before
    public void configure() {
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

        String cp = "target/test-classes";
        analysis = new SVFAConfluenceAnalysis(cp, definition, true);
    }

    @Test
    public void testSVFAConfluentAnalysisExpectingOneConflict() {
        analysis.execute();
        Set<ConfluenceConflict> conflicts = analysis.getConfluentConflicts();
        Assert.assertEquals(conflicts.size(), 1);
        ConfluenceConflict conflict = conflicts.iterator().next();

        Assert.assertEquals(conflict.toString(),
                "SOURCE=>BASE: (br.unb.cic.analysis.samples.ConfluenceWithTransitivitySample,<br.unb.cic.analysis.samples.ConfluenceWithTransitivitySample: void foo()>,9) => (br.unb.cic.analysis.samples.ConfluenceWithTransitivitySample,<br.unb.cic.analysis.samples.ConfluenceWithTransitivitySample: void foo()>,11) => (br.unb.cic.analysis.samples.ConfluenceWithTransitivitySample,<br.unb.cic.analysis.samples.ConfluenceWithTransitivitySample: void foo()>,16)\n" +
                        "SINK=>BASE: (br.unb.cic.analysis.samples.ConfluenceWithTransitivitySample,<br.unb.cic.analysis.samples.ConfluenceWithTransitivitySample: void foo()>,13) => (br.unb.cic.analysis.samples.ConfluenceWithTransitivitySample,<br.unb.cic.analysis.samples.ConfluenceWithTransitivitySample: void foo()>,16)"
        );
    }
}
