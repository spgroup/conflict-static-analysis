package br.unb.cic.analysis.dfp;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Assert;
import org.junit.Test;

public class DFPWithoutPointerAnalysisTest {

    private DFPInterProcedural analysis;
    String cp = "target/test-classes";

    public void configureTest(AbstractMergeConflictDefinition definition) {
        analysis = new DFPInterProcedural(cp, definition);

        analysis.setCallGraph("SPARK");
        analysis.configureSoot();
        analysis.setPrintDepthVisitedMethods(true);
        analysis.buildDFP();

        System.out.println(analysis.svg().reportConflicts().size());
        analysis.reportDFConflicts();
        System.out.println("Call graph:" + analysis.callGraph());
        System.out.println(analysis.svgToDotModel());
        System.out.println(analysis.findSourceSinkPaths());
        //System.out.println(analysis.svg().findConflictingPaths());

    }

    @Test
    public void testDFPAnalysisExpectingOneMoreConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.dfp.CallGraphFromMainSample.Text";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{13}, true);
        configureTest(definition);
        Assert.assertTrue(analysis.svg().reportConflicts().size() >= 1);
    }
}