package br.unb.cic.analysis.svfa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.soot.graph.Graph;
import br.unb.cic.soot.graph.Node;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scalax.collection.GraphBase;

import java.util.*;

public class SVFAConfluentAnalysisTest {

    private SVFAConfluentAnalysis analysis;
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
        analysis = new SVFAConfluentAnalysis(cp, definition);
    }

    @Test
    public void testSVFAConfluentAnalysisExpectingOneConflict() {
        analysis.execute();
        Set<ConfluenceConflict> conflicts = analysis.getConfluentConflicts();
        Assert.assertEquals(conflicts.size(), 1);
        ConfluenceConflict conflict = conflicts.iterator().next();

        Assert.assertEquals(conflict.toString(),
                "SOURCE=>BASE: (br.unb.cic.analysis.samples.DoubleSourceSample,<br.unb.cic.analysis.samples.DoubleSourceSample: void main(java.lang.String[])>,8) => (br.unb.cic.analysis.samples.DoubleSourceSample,<br.unb.cic.analysis.samples.DoubleSourceSample: void main(java.lang.String[])>,14)\n" +
                        "SINK=>BASE: (br.unb.cic.analysis.samples.DoubleSourceSample,<br.unb.cic.analysis.samples.DoubleSourceSample: void main(java.lang.String[])>,12) => (br.unb.cic.analysis.samples.DoubleSourceSample,<br.unb.cic.analysis.samples.DoubleSourceSample: void main(java.lang.String[])>,14)");
    }
}
