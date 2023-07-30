package br.unb.cic.analysis.dfp;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.SootWrapper;
import br.unb.cic.analysis.svfa.confluence.ConfluenceConflict;
import br.unb.cic.analysis.svfa.confluence.DFPConfluenceAnalysis;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class DFPAnalysisBaseTest {

    private DFPAnalysisSemanticConflicts analysis;

    @Before
    public void configure() {
        AbstractMergeConflictDefinition definition = new AbstractMergeConflictDefinition(true) {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(10);
                res.put("br.unb.cic.analysis.samples.DFPBaseSample", lines);
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(12);
                res.put("br.unb.cic.analysis.samples.DFPBaseSample", lines);
                return res;
            }
        };

        String cp = "target/test-classes";
        analysis = new DFPInterProcedural(cp, definition);
    }

    @Test
    public void testDFPAnalysisExpectingOneConflict() {
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
