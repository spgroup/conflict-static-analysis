package br.unb.cic.analysis.ioa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import soot.G;
import soot.PackManager;
import soot.Transform;

@Ignore
public class InterproceduralOverridingAssignmentAnalysisTest {


    private void configureTest(InterproceduralOverrideAssignment analysis) {
        G.reset();

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.analysis", analysis));
        soot.options.Options.v().setPhaseOption("cg.spark", "on");
        soot.options.Options.v().setPhaseOption("cg.spark", "verbose:true");
        //	PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");

        String testClasses = "target/test-classes/";
        soot.Main.main(new String[]{"-w", "-allow-phantom-refs", "-f", "J", "-keep-line-number", "-process-dir", testClasses});
    }

    @Test
    public void classFieldConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentClassFieldConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void classFieldConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentClassFieldConflictInterProceduralSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void classFieldNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentClassFieldNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void classFieldNotConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentClassFieldNotConflictInterProceduralSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void classFieldWithParameterNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentClassFieldWithParameterNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void localVariablesNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentLocalVariablesNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{6}, new int[]{7});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void localVariablesNotConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentLocalVariablesNotConflictInterProceduralSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{6}, new int[]{7});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void localVariablesWithParameterNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentLocalVariablesWithParameterNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{6}, new int[]{7});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void objectFieldConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentObjectFieldConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void objectFieldNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentObjectFieldNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }


}
