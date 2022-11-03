package br.unb.cic.analysis.dfp;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.SootWrapper;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Assert;
import org.junit.Test;
import scala.collection.JavaConverters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DFPTest{

    public static final String CLASS_NAME = "br.unb.cic.analysis.samples.DFPSample";

    public DFPAnalysisSemanticConflicts configureInterTestDFP(String classpath, int[] leftchangedlines, int[] rightchangedlines) {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(classpath, leftchangedlines, rightchangedlines, true);
        String cp = "target/test-classes";
        List<String> classes = Collections.singletonList(cp);
        SootWrapper.configureSootOptionsToRunInterproceduralOverrideAssignmentAnalysis(classes);

        return new DFPIntraProcedural(cp, definition);
    }
    private List<String> conflicts = new ArrayList<>();

    @Test
    public void testDFPAnalysisInterProcedural() {
        DFPAnalysisSemanticConflicts analysis = configureInterTestDFP(CLASS_NAME, new int[]{10}, new int[]{12});

        analysis.buildDFP();

        System.out.println(analysis.svgToDotModel());
        System.out.println(analysis.findSourceSinkPaths());
        System.out.println(analysis.svg().findConflictingPaths());

        Assert.assertTrue(analysis.svg().reportConflicts().size() > 1);
    }

}
