package br.unb.cic.analysis.svfa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.soot.graph.Graph;
import br.unb.cic.soot.graph.Node;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scalax.collection.GraphBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SVFATest {

    private SVFAAnalysis analysis;
    @Before
    public void configure() {
        AbstractMergeConflictDefinition definition = new AbstractMergeConflictDefinition() {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(6);
                res.put("br.unb.cic.analysis.samples.IntraproceduralDataFlow", lines);
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(11);
                res.put("br.unb.cic.analysis.samples.IntraproceduralDataFlow", lines);
                return res;
            }
        };

        String cp = "target/test-classes";
        analysis = new SVFAAnalysis(cp, definition);
    }

    @Test
    public void testSVFAnalysisExpectingOneConflict() {
        analysis.buildSparseValueFlowGraph();
        Graph<Node> g = analysis.svg();
        Assert.assertEquals(9, g.nodes().size());
        Assert.assertEquals(1, analysis.reportConflicts().size());
//        Assert.assertEquals(1, analysis.findSourceSinkPaths().size());
    }
}
