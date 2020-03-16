package br.unb.cic.analysis.svfa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
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

        scalax.collection.mutable.Graph g = analysis.svg();

        System.out.println(g.nodes().size());
    }
}
