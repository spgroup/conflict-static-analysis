package br.unb.cic.analysis.dfp;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.cd.CDAnalysisSemanticConflicts;
import br.unb.cic.analysis.cd.CDIntraProcedural;
import br.unb.cic.analysis.dfp.DFPAnalysisSemanticConflicts;
import br.unb.cic.analysis.dfp.DFPIntraProcedural;
import br.unb.cic.analysis.pdg.PDGAnalysisSemanticConflicts;
import br.unb.cic.analysis.pdg.PDGIntraProcedural;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Assert;
import org.junit.Test;

public class DFPTest {

    public static final String CLASS_NAME = "br.unb.cic.analysis.samples.DFPSample";

    public DFPAnalysisSemanticConflicts configureIntraTestDFP(String classpath, int[] leftchangedlines, int[] rightchangedlines) {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(classpath, leftchangedlines, rightchangedlines);
        String cp = "target/test-classes";
        return new DFPIntraProcedural(cp, definition);
    }

    @Test
    public void testDFPAnalysisIntraProcedural() {
        DFPAnalysisSemanticConflicts analysis = configureIntraTestDFP(CLASS_NAME, new int[]{9}, new int[]{11, 13, 16});

        analysis.buildDFP();

        System.out.println(analysis.svgToDotModel());
        Assert.assertEquals(4, analysis.svg().reportConflicts().size());
    }

}
