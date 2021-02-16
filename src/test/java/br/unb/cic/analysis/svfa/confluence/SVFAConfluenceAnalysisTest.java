package br.unb.cic.analysis.svfa.confluence;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class SVFAConfluenceAnalysisTest {

    private SVFAConfluenceAnalysis analysis;
    @Before
    public void configure() {
        AbstractMergeConflictDefinition definition = new AbstractMergeConflictDefinition() {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(8);
                res.put("br.unb.cic.analysis.samples.DoubleSourceSample", lines);
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(12);
                res.put("br.unb.cic.analysis.samples.DoubleSourceSample", lines);
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
        Assert.assertEquals(2, conflicts.size());
        ConfluenceConflict conflict = conflicts.iterator().next();
//
//        Assert.assertEquals(
//                "SOURCE=>BASE: (br.unb.cic.analysis.samples.DoubleSourceSample,<br.unb.cic.analysis.samples.DoubleSourceSample: void main(java.lang.String[])>,8) => (br.unb.cic.analysis.samples.DoubleSourceSample,<br.unb.cic.analysis.samples.DoubleSourceSample: void main(java.lang.String[])>,14)\n" +
//                        "SINK=>BASE: (br.unb.cic.analysis.samples.DoubleSourceSample,<br.unb.cic.analysis.samples.DoubleSourceSample: void main(java.lang.String[])>,12) => (br.unb.cic.analysis.samples.DoubleSourceSample,<br.unb.cic.analysis.samples.DoubleSourceSample: void main(java.lang.String[])>,14)",
//                conflict.toString());
    }
}
