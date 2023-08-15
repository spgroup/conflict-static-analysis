package br.unb.cic.analysis.dfp;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.Main;
import br.unb.cic.analysis.SootWrapper;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.analysis.model.TraversedLine;
import br.unb.cic.analysis.svfa.confluence.ConfluenceConflict;
import br.unb.cic.analysis.svfa.confluence.DFPConfluenceAnalysis;
import br.unb.cic.soot.graph.StatementNode;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class DFPAnalysisBaseTest {

    private DFPAnalysisSemanticConflicts analysis;
    AbstractMergeConflictDefinition definition;
    @Before
    public void configure() {
        definition = new AbstractMergeConflictDefinition(true) {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(11);
                res.put("br.unb.cic.analysis.samples.DFPBaseSample", lines);
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(13);
                res.put("br.unb.cic.analysis.samples.DFPBaseSample", lines);
                return res;
            }
        };

        String cp = "target/test-classes";
        analysis = new DFPInterProcedural(cp, definition);
    }

    @Test
    public void testDFPAnalysisExpectingOneMoreConflict() {
        analysis.configureSoot();

        analysis.buildDFP();
        analysis.generateDFPReportConflict(definition);
        System.out.println(analysis.svg().reportConflicts().size());
        System.out.println(analysis.svgToDotModel());
        System.out.println(analysis.findSourceSinkPaths());
        System.out.println(analysis.svg().findConflictingPaths());
        Assert.assertTrue(analysis.svg().reportConflicts().size() >= 1);
    }
}
