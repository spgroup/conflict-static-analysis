package br.unb.cic.analysis.ioa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Assert;
import org.junit.Test;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.options.Options;

import java.util.*;

public class InterproceduralOverridingAssignmentAnalysisTest {
    private void configureTest(InterproceduralOverrideAssignment analysis) {
        G.reset();

        List<String> testClasses = Collections.singletonList("target/test-classes/");

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.analysis", analysis));

        configureSootOptions(testClasses);
        configurePhaseOption();

        // Scene.v().addBasicClass("java.util.ArrayList",BODIES);
        Scene.v().loadNecessaryClasses();

        //enableCHACallGraph();
        enableSparkCallGraph();

        analysis.configureEntryPoints();

        configurePackages().forEach(p -> PackManager.v().getPack(p).apply());
    }

    private void configurePhaseOption() {
        //Options.v().setPhaseOption("cg.spark", "on");
        //Options.v().setPhaseOption("cg.spark", "verbose:true");
        Options.v().setPhaseOption("cg.spark", "enabled:true");
        Options.v().setPhaseOption("jb", "use-original-names:true");
    }

    private List<String> configurePackages() {
        List<String> packages = new ArrayList<String>();
        packages.add("cg");
        packages.add("wjtp");
        return packages;
    }

    private List<String> getIncludeList() {
        List<String> stringList = new ArrayList<String>(Arrays.asList("java.lang.*", "java.util.*")); //java.util.HashMap
        return stringList;
    }

    private static void enableSparkCallGraph() {
        //Enable Spark
        HashMap<String, String> opt = new HashMap<String, String>();
        //opt.put("propagator","worklist");
        //opt.put("simple-edges-bidirectional","false");
        opt.put("on-fly-cg", "true");
        //opt.put("set-impl","double");
        //opt.put("double-set-old","hybrid");
        //opt.put("double-set-new","hybrid");
        //opt.put("pre_jimplify", "true");
        SparkTransformer.v().transform("", opt);
    }

    private static void enableCHACallGraph() {
        CHATransformer.v().transform();
    }

    private void configureSootOptions(List<String> testClasses) {
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_whole_program(true);
        Options.v().set_process_dir(testClasses);
        Options.v().set_full_resolver(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_prepend_classpath(false);
        Options.v().set_include(getIncludeList());
    }

    @Test
    public void callGraphTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.CallGraphSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{6, 9, 12}, new int[]{7, 10, 13});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }

    @Test
    public void ifWithInvokeConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.IfWithInvokeConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void containsInvokeExp() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ContainsInvokeExpConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void chainedMethodCallsConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChainedMethodCallsConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{13}, new int[]{12});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(550, analysis.getConflicts().size());
    }

    @Test
    public void bothMarkingConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.BothMarkingConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{8});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void classFieldConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentClassFieldConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void classFieldConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentClassFieldConflictInterProceduralSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void classFieldNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentClassFieldNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void classFieldNotConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentClassFieldNotConflictInterProceduralSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void classFieldWithParameterNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentClassFieldWithParameterNotConflictInterProceduralSample";
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
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentLocalVariablesNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{6}, new int[]{7});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void localVariablesNotConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentLocalVariablesNotConflictInterProceduralSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{6}, new int[]{7});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void localVariablesWithParameterNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentLocalVariablesWithParameterNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{6}, new int[]{7});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void additionToArrayConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentAdditionToArrayConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{12});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(57, analysis.getConflicts().size());
    }

    @Test
    public void hashmapConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentHashmapConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{12}, new int[]{13});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(511, analysis.getConflicts().size());
    }


    @Test
    public void changePublicAttributesConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentChangePublicAttributesConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7, 10}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void changeInstanceAttributeConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentChangeInstanceAttributeConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void differentMethodOnIdenticalClass() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentDifferentMethodOnIdenticalClassConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{12}, new int[]{13});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void localArraysNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentLocalArrayNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{6}, new int[]{7});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void classFieldArraysConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentArraysClassFieldConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void classFieldArraysNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentArraysClassFieldNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void classFieldArraysAliasingConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentArrayAliasingConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{11});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void arrayDiferentIndexNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ArrayDiferentIndexNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void arraySameIndexConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ArraySameIndexConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void staticClassFieldNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentStaticClassFieldNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void staticClassFieldConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentStaticClassFieldConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void objectFieldConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentObjectFieldConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void objectFieldNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentObjectFieldNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void threeDepthObjectsConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentObjectThreeFieldsOneConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{15, 18}, new int[]{16, 19});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void differentAttributeOnIdenticalClass() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentDifferentAttributeOnIdenticalClassNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{13}, new int[]{14});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void differentClassWithSameAttribute() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentDifferentClassWithSameAttributeNotConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{14}, new int[]{15});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void sameAttributeOnIdenticalClass() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentSameAttributeOnIdenticalClassConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{13}, new int[]{14});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void concatMethodsConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentConcatMethodsConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{8});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void ifBranchConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentIfBranchConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void sequenceConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentSequenceConflictInterProceduralSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7, 9}, new int[]{8});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void sequenceConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.OverridingAssignmentSequenceConflictInterProceduralSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{8, 9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void recursiveMockupConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.RecursiveMockupConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(3, analysis.getConflicts().size());
    }

    @Test
    public void recursiveMockupNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.RecursiveMockupNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }
}
