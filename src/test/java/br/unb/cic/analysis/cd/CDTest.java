package br.unb.cic.analysis.cd;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Assert;
import org.junit.Test;

public class CDTest {

    public static final String CLASS_NAME = "br.unb.cic.analysis.samples.CDSample";

    public CDAnalysisSemanticConflicts configureIntraTestCD(String classpath, int[] leftchangedlines, int[] rightchangedlines) {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(classpath, leftchangedlines, rightchangedlines);
        String cp = "target/test-classes";
        return new CDIntraProcedural(cp, definition);
    }

    @Test
    public void testCDAnalysisIntraProceduralWithExceptionEdge() {
        CDAnalysisSemanticConflicts analysis = configureIntraTestCD(CLASS_NAME, new int[]{10}, new int[]{16});
        analysis.setOmitExceptingUnitEdges(true);
        analysis.configureSoot();
        analysis.buildCD();

        System.out.println(analysis.cd().toDotModel());
        Assert.assertEquals(0, analysis.cd().reportConflicts().size());
    }

    @Test
    public void testCDAnalysisIntraProceduralWithoutExceptionEdge() {
        CDAnalysisSemanticConflicts analysis = configureIntraTestCD(CLASS_NAME, new int[]{10}, new int[]{16});
        analysis.setOmitExceptingUnitEdges(false);
        analysis.configureSoot();
        analysis.buildCD();

        System.out.println(analysis.cd().toDotModel());
        Assert.assertEquals(1, analysis.cd().reportConflicts().size());
    }


}
