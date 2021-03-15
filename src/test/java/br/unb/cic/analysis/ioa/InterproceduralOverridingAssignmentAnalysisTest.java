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
        soot.options.Options.v().setPhaseOption("jb", "use-original-names:true");
        //PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");

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
                .definition(sampleClassPath, new int[]{9}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);

        // Not Conflict - Not implemented yet. You will need constant propagation.
        // Currently detected as conflict: [left, m():9] --> [right, foo():14]
        Assert.assertEquals(1, analysis.getConflicts().size());
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
    public void localArraysNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentArrayNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{6}, new int[]{7});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        System.out.println(analysis.getConflicts());
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void classFieldArraysConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentArraysClassFieldConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        System.out.println(analysis.getConflicts());
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void classFieldArraysNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentArraysClassFieldNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void classFieldArraysAliasingConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentArrayAliasingConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{11});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        System.out.println(analysis.getConflicts());
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void staticClassFieldNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentStaticClassFieldNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void staticClassFieldConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentStaticClassFieldConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        System.out.println(analysis.getConflicts());
        Assert.assertEquals(1, analysis.getConflicts().size());
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

    @Test
    public void threeDepthObjectsConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentObjectThreeFieldsOneConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{13, 16}, new int[]{14, 17});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void differentAttributeOnIdenticalClass() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentDifferentAttributeOnIdenticalClassNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{13}, new int[]{14});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void differentClassWithSameAttribute() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentDifferentClassWithSameAttributeNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{14}, new int[]{15});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void sameAttributeOnIdenticalClass() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentSameAttributeOnIdenticalClassConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{13}, new int[]{14});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void concatMethodsConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentConcatMethodsConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{8});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void ifBranchConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentIfBranchConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void sequenceConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentSequenceConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7, 9}, new int[]{8});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }

    @Test
    public void sequenceConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.OverridingAssignmentSequenceConflictInterProceduralSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{8, 9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }
}
