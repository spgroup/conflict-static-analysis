package br.unb.cic.analysis.ioa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import soot.G;
import soot.PackManager;
import soot.Transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Ignore
public class LocalVariablesWithParameterNotConflictAnalysisTest {

    private InterproceduralOverrideAssignment analysis;

    @Before
    public void configure() {
        G.reset();

        AbstractMergeConflictDefinition definition = new AbstractMergeConflictDefinition() {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(6);
                res.put("br.unb.cic.analysis.samples.OverridingAssignmentLocalVariablesWithParameterNotConflictInterProceduralSample", lines);
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(7);
                res.put("br.unb.cic.analysis.samples.OverridingAssignmentLocalVariablesWithParameterNotConflictInterProceduralSample", lines);
                return res;
            }
        };

        analysis = new InterproceduralOverrideAssignment(definition);

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.analysis", analysis));
        soot.options.Options.v().setPhaseOption("cg.spark", "on");
        soot.options.Options.v().setPhaseOption("cg.spark", "verbose:true");
        //	PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");

        String testClasses = "target/test-classes/";
        soot.Main.main(new String[]{"-w", "-allow-phantom-refs", "-f", "J", "-keep-line-number", "-process-dir", testClasses});
    }

    @Test
    public void interproceduralOverridingAssignmentTest() {
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

}
