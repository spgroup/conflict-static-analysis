package br.unb.cic.analysis.dfp;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DFPAnalysisBaseTest {

    private DFPAnalysisSemanticConflicts analysis;
    public static String class_name = "br.unb.cic.analysis.samples.DFPBaseSample";

    @Before
    public void configure() {
        AbstractMergeConflictDefinition definition = new AbstractMergeConflictDefinition() {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(11);
                res.put(class_name, lines);
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(13);
                res.put(class_name, lines);
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
        System.out.println(analysis.svg().reportConflicts().size());
        analysis.reportDFConflicts();
        System.out.println(analysis.callGraph());
        System.out.println(analysis.findSourceSinkPaths());
        System.out.println(analysis.svg().findConflictingPaths());
        Assert.assertTrue(analysis.svg().reportConflicts().size() >= 1);
    }
}
