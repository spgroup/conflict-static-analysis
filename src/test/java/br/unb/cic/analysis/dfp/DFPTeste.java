package br.unb.cic.analysis.dfp;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class DFPTeste {

    private DFPInterProcedural analysis;
    AbstractMergeConflictDefinition definition;
    public static String class_name = "br.unb.cic.analysis.samples.DFPMotivating";

    @Before
    public void configure() {
        definition = new AbstractMergeConflictDefinition(true) {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                addConfiguration(res, class_name, 18);
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                addConfiguration(res, class_name, 21, 22, 23, 24);
                return res;
            }

            private void addConfiguration(Map<String, List<Integer>> map, String className, Integer... lines) {
                List<Integer> lineList = new ArrayList<>();
                Collections.addAll(lineList, lines);
                map.put(className, lineList);
            }

        };

        String cp = "target/test-classes";

        analysis = new DFPInterProcedural(cp, definition);
    }

    @Test
    public void testDFPAnalysisExpectingOneMoreConflict() {

        analysis.setCallGraph("CHA");
        analysis.configureSoot();
        System.out.println("Call graph:"+ analysis.callGraph());

        analysis.setPrintDepthVisitedMethods(true);
        analysis.buildDFP();

        System.out.println(analysis.svg().reportConflicts().size());
        analysis.reportDFConflicts();
        System.out.println("Call graph:"+ analysis.callGraph());
        System.out.println(analysis.svgToDotModel());
        System.out.println(analysis.findSourceSinkPaths());
        System.out.println(analysis.svg().findConflictingPaths());
        Assert.assertTrue(analysis.svg().reportConflicts().size() >= 1);
    }
}
