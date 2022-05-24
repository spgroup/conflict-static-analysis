//package br.unb.cic.analysis.cda;
//
//import br.unb.cic.analysis.AbstractMergeConflictDefinition;
//import br.unc.cic.analysis.test.DefinitionFactory;
//import org.junit.Assert;
//import org.junit.Test;
//
//public class CDTest {
//
//    public static final String CLASS_NAME = "br.unb.cic.analysis.samples.CDASample";
//
//    public br.unb.cic.analysis.cda.CDAnalysis configureIntraTest(String classpath, int[] leftchangedlines, int[] rightchangedlines) {
//        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(classpath, leftchangedlines, rightchangedlines);
//        String cp = "target/test-classes";
//        return new br.unb.cic.analysis.cda.CDAInterProcedural(cp, definition);
//    }
//
//    @Test
//    public void testSVFAnalysisIntraProceduralSameMethod() {
//        br.unb.cic.analysis.cda.CDAnalysis analysis = configureIntraTest(CLASS_NAME, new int[]{13, 15}, new int[]{5, 8, 18});
//        analysis.buildCDA();
//
//        Assert.assertEquals(2, analysis.reportConflicts().size());
//    }
//
//}
