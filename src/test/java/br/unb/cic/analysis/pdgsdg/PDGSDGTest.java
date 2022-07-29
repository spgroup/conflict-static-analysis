package br.unb.cic.analysis.pdgsdg;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.cd.CDAnalysis;
import br.unb.cic.analysis.cd.CDInterProcedural;
import br.unb.cic.analysis.dfp.DFPAnalysis;
import br.unb.cic.analysis.dfp.DFPInterProcedural;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Assert;
import org.junit.Test;

public class PDGSDGTest {

    public static final String CLASS_NAME = "br.unb.cic.analysis.samples.PDGSDGSample";

    public PDGSDGAnalysis configureIntraTest(String classpath, int[] leftchangedlines, int[] rightchangedlines) {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(classpath, leftchangedlines, rightchangedlines);
        String cp = "target/test-classes";
        return new PDGSDGInterProcedural(cp, definition);
    }

    public CDAnalysis configureIntraTestCD(String classpath, int[] leftchangedlines, int[] rightchangedlines) {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(classpath, leftchangedlines, rightchangedlines);
        String cp = "target/test-classes";
        return new CDInterProcedural(cp, definition);
    }

    public DFPAnalysis configureIntraTestDFP(String classpath, int[] leftchangedlines, int[] rightchangedlines) {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(classpath, leftchangedlines, rightchangedlines);
        String cp = "target/test-classes";
        return new DFPInterProcedural(cp, definition);
    }

    @Test
    public void testSVFAnalysisIntraProceduralSameMethod() {
        PDGSDGAnalysis analysis = configureIntraTest(CLASS_NAME, new int[]{13, 15}, new int[]{5, 8, 18});
        CDAnalysis cd = configureIntraTestCD(CLASS_NAME, new int[]{13, 15}, new int[]{5, 8, 18});
        DFPAnalysis dfp = configureIntraTestDFP(CLASS_NAME, new int[]{13, 15}, new int[]{5, 8, 18});

        analysis.buildPDG(cd, dfp);

        Assert.assertEquals(2, analysis.pdg().reportConflicts().size());
    }

}
