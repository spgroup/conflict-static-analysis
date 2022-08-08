package br.unb.cic.analysis.pdg;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.cd.CDAnalysisSemanticConflicts;
import br.unb.cic.analysis.cd.CDIntraProcedural;
import br.unb.cic.analysis.dfp.DFPAnalysisSemanticConflicts;
import br.unb.cic.analysis.dfp.DFPIntraProcedural;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Assert;
import org.junit.Test;

public class PDGTest {

    public static final String CLASS_NAME = "br.unb.cic.analysis.samples.PDGSample";

    public PDGAnalysisSemanticConflicts configureIntraTest(String classpath, int[] leftchangedlines, int[] rightchangedlines) {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(classpath, leftchangedlines, rightchangedlines);
        String cp = "target/test-classes";
        return new PDGIntraProcedural(cp, definition);
    }

    public CDAnalysisSemanticConflicts configureIntraTestCD(String classpath, int[] leftchangedlines, int[] rightchangedlines) {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(classpath, leftchangedlines, rightchangedlines);
        String cp = "target/test-classes";
        return new CDIntraProcedural(cp, definition);
    }

    public DFPAnalysisSemanticConflicts configureIntraTestDFP(String classpath, int[] leftchangedlines, int[] rightchangedlines) {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(classpath, leftchangedlines, rightchangedlines);
        String cp = "target/test-classes";
        return new DFPIntraProcedural(cp, definition);
    }

    @Test
    public void testSVFAnalysisIntraProceduralSameMethod() {
        PDGAnalysisSemanticConflicts analysis = configureIntraTest(CLASS_NAME, new int[]{13, 15}, new int[]{5, 8, 18});
        CDAnalysisSemanticConflicts cd = configureIntraTestCD(CLASS_NAME, new int[]{13, 15}, new int[]{5, 8, 18});
        DFPAnalysisSemanticConflicts dfp = configureIntraTestDFP(CLASS_NAME, new int[]{13, 15}, new int[]{5, 8, 18});

        analysis.buildPDG(cd, dfp);

        System.out.println(analysis.pdgToDotModel());
        Assert.assertEquals(2, analysis.pdg().reportConflicts().size());
    }

}
