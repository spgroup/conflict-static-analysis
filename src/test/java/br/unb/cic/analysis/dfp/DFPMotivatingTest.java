package br.unb.cic.analysis.dfp;
import java.util.Collections;
import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.SootWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scala.collection.JavaConverters;
import soot.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DFPMotivatingTest {

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

        PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");
        SootWrapper.builder().withClassPath(cp).addClass(class_name).build().execute();
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
