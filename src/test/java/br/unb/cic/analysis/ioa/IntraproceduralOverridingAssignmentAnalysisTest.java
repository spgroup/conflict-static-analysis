package br.unb.cic.analysis.ioa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.SootWrapper;
import br.unb.cic.analysis.model.Conflict;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Assert;
import org.junit.Test;
import soot.G;
import soot.PackManager;
import soot.Transform;

import java.io.FileWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class IntraproceduralOverridingAssignmentAnalysisTest {

    private final int depthLimit = 5;

    private void configureTest(InterproceduralOverrideAssignment analysis) {
        G.reset();

        List<String> testClasses = Collections.singletonList("target/test-classes/");

        SootWrapper.configureSootOptionsToRunInterproceduralOverrideAssignmentAnalysis(testClasses);

        analysis.configureEntryPoints();

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.analysis", analysis));
        SootWrapper.applyPackages();

        try {
            exportResults(analysis.getConflicts());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportResults(Set<Conflict> conflicts) throws Exception {
        final String out = "out.txt";
        final FileWriter fw = new FileWriter(out);

        if (conflicts.size() == 0) {
            System.out.println(" Analysis results");
            System.out.println("----------------------------");
            System.out.println(" No conflicts detected");
            System.out.println("----------------------------");
            return;
        }


        conflicts.forEach(c -> {
            try {
                fw.write(c + "\n\n");
            } catch (Exception e) {
                System.out.println("error exporting the results " + e.getMessage());
            }
        });
        fw.close();
        System.out.println(" Analysis results");
        System.out.println("----------------------------");
        System.out.println(" Number of conflicts: " + conflicts.size());
        System.out.println(" Results exported to " + out);
        System.out.println("----------------------------");
    }

    @Test
    public void arraysClassFieldConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentArraysClassFieldSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void arraysCompleteOverlayConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentArraysCompleteOverlaySample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{7, 13});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void arraysConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentArraysSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void arraysConflictSample2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentArraysSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{3, 12}, new int[]{10, 13});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void arraysOfObjectsConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentArraysOfObjectsSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{19, 21}, new int[]{22});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void arraysParameterConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentArraysParameterSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{6}, new int[]{8});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void localVariablesClassFieldConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentLocalVariablesClassFieldSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void localVariablesConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentLocalVariablesSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7, 10}, new int[]{8, 11});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void localVariablesParameterConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentLocalVariablesParameterSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{11});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void methodCallConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentMethodCallSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7, 10}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void objectOneFieldConditionalTwoConflictsSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentObjectOneFieldConditionalTwoConflictsSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11, 13}, new int[]{15, 16});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }

    @Test
    public void objectOneFieldConditionalNoConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentObjectOneFieldConditionalZeroConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{13});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void objectOneFieldConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentObjectOneFieldOneConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10, 13}, new int[]{11, 14});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void objectOneFieldTwoConflictsSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentObjectOneFieldTwoConflictsSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10, 13}, new int[]{11, 14});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }

    @Test
    public void objectOneFieldNoConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentObjectOneFieldZeroConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10, 14}, new int[]{11, 15});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void objectThreeFieldsConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentObjectThreeFieldsOneConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10, 13}, new int[]{11, 14});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void objectThreeFieldsTwoConflictsSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentArraysClassFieldSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10, 13}, new int[]{11, 14});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }

    @Test
    public void objectTwoFieldsConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentObjectTwoFieldsOneConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10, 13}, new int[]{11, 14});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void objectTwoFieldsTwoConflictsSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentObjectTwoFieldsTwoConflictsSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10, 13}, new int[]{11, 14});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }

    @Test
    public void sameReturnConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.oa.OverridingAssignmentSameReturnSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{8});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition, depthLimit, false);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

}
