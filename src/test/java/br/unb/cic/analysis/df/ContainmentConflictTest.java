package br.unb.cic.analysis.df;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.dfp.DFPAnalysisSemanticConflicts;
import br.unb.cic.analysis.dfp.DFPInterProcedural;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import soot.G;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainmentConflictTest {

    private DFPAnalysisSemanticConflicts analysis;

    @Before
    public void configure() {
        G.reset();
        Collector.instance().clear();

        AbstractMergeConflictDefinition definition = new AbstractMergeConflictDefinition() {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(7);
                res.put("br.unb.cic.analysis.samples.ioa.ContainmentSample", lines);
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(9);
                res.put("br.unb.cic.analysis.samples.ioa.ContainmentSample", lines);
                return res;
            }
        };

        String cp = "target/test-classes";

        analysis = new DFPInterProcedural(cp, definition);
    }

    @Test
    public void testDataFlowAnalysisExpectingOneConflict() {
        analysis.configureSoot();
        analysis.setPrintDepthVisitedMethods(true);

        analysis.buildDFP();

        System.out.println(analysis.svg().reportConflicts().size());
        System.out.println(analysis.svgToDotModel());
        System.out.println(analysis.findSourceSinkPaths());
        System.out.println(analysis.svg().findConflictingPaths());
        Assert.assertTrue(analysis.svg().reportConflicts().size() >= 1);
    }
}
