package br.unb.cic.analysis.ioa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.SootWrapper;
import br.unb.cic.analysis.model.Conflict;
import br.unc.cic.analysis.test.DefinitionFactory;
import br.unc.cic.analysis.test.MarkingClass;
import org.junit.Assert;
import org.junit.Test;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Transform;

import java.io.FileWriter;
import java.util.*;

import static br.unb.cic.analysis.SootWrapper.enableSparkCallGraph;

public class InterproceduralOverridingAssignmentAnalysisTest {
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
    public void loggingTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.LoggingConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{13}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void defaultConstructorTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.DefaultConstructorConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }


    @Test
    public void StringArrayTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.StringArraySample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{20});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void stacktraceConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.StacktraceConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{11});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }

    @Test
    public void baseConflictTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.BaseConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{15}, new int[]{17});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void baseNotConflictTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.BaseNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{14}, new int[]{16});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void subclassWithConditionalTeste() {

        MarkingClass subclassWithConditionalNotConflictSample = new MarkingClass("br.unb.cic.analysis.samples.ioa" +
                ".SubclassWithConditionalNotConflictSample", new int[]{7}, new int[]{});
        MarkingClass c = new MarkingClass("br.unb.cic.analysis.samples.ioa.C", new int[]{27}, new int[]{});
        MarkingClass d = new MarkingClass("br.unb.cic.analysis.samples.ioa.D", new int[]{}, new int[]{36});

        List<MarkingClass> markingClassList = Arrays.asList(subclassWithConditionalNotConflictSample, c,
                d);

        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(markingClassList, false);

        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void subclassTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.SubclassConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{8});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void arrayConstantTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ArrayConstantSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{12});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void callGraphTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.CallGraphSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10, 13, 16}, new int[]{11, 14, 17});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }

    @Test
    public void ifWithInvokeConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.IfWithInvokeConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{12});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void containsInvokeExp() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ContainsInvokeExpConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{12});
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
        Assert.assertEquals(3, analysis.getConflicts().size());
    }

    @Test
    public void bothMarkingConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.BothMarkingConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{13});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void classFieldConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ClassFieldConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{11});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void classFieldConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ClassFieldConflictSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{11});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void classFieldNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ClassFieldNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{12});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void classFieldNotConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ClassFieldNotConflictSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{12});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void classFieldWithParameterNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ClassFieldWithParameterNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{13});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);

        // Not Conflict - Not implemented yet. You will need constant propagation.
        // Currently detected as conflict: [left, m():11] --> [right, foo():116]
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void localVariablesNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.LocalVariablesNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void localVariablesNotConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.LocalVariablesNotConflictSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void localVariablesWithParameterNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.LocalVariablesWithParameterNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void additionToArrayWithJavaUtilConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.AdditionToArrayConflictSample";
        List<String> stringList = new ArrayList<String>(Arrays.asList("java.util.*")); // java.util.* java.util.HashMap

        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{13});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);

        G.reset();

        List<String> testClasses = Collections.singletonList("target/test-classes/");

        soot.options.Options.v().set_no_bodies_for_excluded(true);
        soot.options.Options.v().set_allow_phantom_refs(true);
        soot.options.Options.v().set_output_format(soot.options.Options.output_format_jimple);
        soot.options.Options.v().set_whole_program(true);
        soot.options.Options.v().set_process_dir(testClasses);
        soot.options.Options.v().set_full_resolver(true);
        soot.options.Options.v().set_keep_line_number(true);
        soot.options.Options.v().set_prepend_classpath(false);
        soot.options.Options.v().set_include(stringList);
        //Options.v().setPhaseOption("cg.spark", "on");
        //Options.v().setPhaseOption("cg.spark", "verbose:true");
        soot.options.Options.v().setPhaseOption("cg.spark", "enabled:true");
        soot.options.Options.v().setPhaseOption("jb", "use-original-names:true");

        Scene.v().loadNecessaryClasses();

        enableSparkCallGraph();

        analysis.configureEntryPoints();

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.analysis", analysis));
        SootWrapper.applyPackages();

        try {
            exportResults(analysis.getConflicts());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals(28, analysis.getConflicts().size());
    }

    @Test
    public void additionToArrayWithoutJavaUtilNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.AdditionToArrayConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{13});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void hashmapWithJavaUtilConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.HashmapConflictSample";
        List<String> stringList = new ArrayList<String>(Arrays.asList("java.util.HashMap")); // java.util.* java.util.HashMap

        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{12});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        G.reset();

        List<String> testClasses = Collections.singletonList("target/test-classes/");

        soot.options.Options.v().set_no_bodies_for_excluded(true);
        soot.options.Options.v().set_allow_phantom_refs(true);
        soot.options.Options.v().set_output_format(soot.options.Options.output_format_jimple);
        soot.options.Options.v().set_whole_program(true);
        soot.options.Options.v().set_process_dir(testClasses);
        soot.options.Options.v().set_full_resolver(true);
        soot.options.Options.v().set_keep_line_number(true);
        soot.options.Options.v().set_prepend_classpath(false);
        soot.options.Options.v().set_include(stringList);
        //Options.v().setPhaseOption("cg.spark", "on");
        //Options.v().setPhaseOption("cg.spark", "verbose:true");
        soot.options.Options.v().setPhaseOption("cg.spark", "enabled:true");
        soot.options.Options.v().setPhaseOption("jb", "use-original-names:true");

        Scene.v().loadNecessaryClasses();

        enableSparkCallGraph();

        analysis.configureEntryPoints();

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.analysis", analysis));
        SootWrapper.applyPackages();

        try {
            exportResults(analysis.getConflicts());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertEquals(274, analysis.getConflicts().size());
    }

    @Test
    public void hashmapWithoutJavaUtilNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.HashmapConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{12});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        //with java.util.*
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void changePublicAttributesConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangePublicAttributesConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7, 10}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void pointsToSameArray() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameArraySample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void pointsToSameArrayIndex() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameArrayDifferentIndexSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void pointsToSameArrayIndexSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameArrayIndexSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void pointsToOnlyOneObjectFromParametersWithMain() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToOnlyOneObjectFromParametersWithMainSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{12}, new int[]{14});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }

    @Test
    public void pointsToConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameObjectSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }


    @Test
    public void pointsToNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToDifferentObjectSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }


    @Test
    public void pointsToDifferentObjectFromParameters() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToDifferentObjectFromParametersSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }


    @Test
    public void pointsToDifferentObjectFromParametersWithMain() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToDifferentObjectFromParametersWithMainSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{13});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void pointsToSameObjectFromParametersWithMain() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameObjectFromParametersWithMainSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{13});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }


    @Test
    public void pointsToSameObjectFromParameters() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameObjectFromParametersSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(14, analysis.getConflicts().size());
    }


    @Test
    public void pointsToSameObjectFromParameters2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameObjectFromParametersSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }


    @Test
    public void pointsToSameObjectFromParameters3() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameObjectFromParametersSample3";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }


    @Test
    public void pointsToSameObjectFromParameters4() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameObjectFromParametersSample4";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(13, analysis.getConflicts().size());
    }

    @Test
    public void changeInstanceAttributeConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangeInstanceAttributeConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{12});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void differentMethodOnIdenticalClass() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.DifferentMethodOnIdenticalClassConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{12}, new int[]{13});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void localArraysNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.LocalArrayNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void localArraysRecursiveNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.LocalArrayRecursiveNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void classFieldArraysConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ArraysClassFieldConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{13}, new int[]{14});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void classFieldArraysNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ArraysClassFieldNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void arraysAliasingConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ArrayAliasingConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{12}, new int[]{13});
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
                .definition(sampleClassPath, new int[]{10}, new int[]{11});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void staticClassFieldNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.StaticClassFieldNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void staticClassFieldConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.StaticClassFieldConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void objectFieldConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ObjectFieldConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void objectFieldNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ObjectFieldNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void threeDepthObjectsConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ObjectThreeFieldsOneConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{15, 18}, new int[]{16, 19});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void differentAttributeOnIdenticalClass() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.DifferentAttributeOnIdenticalClassNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{16}, new int[]{17});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void differentClassWithSameAttribute() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.DifferentClassWithSameAttributeNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{16}, new int[]{17});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void sameAttributeOnIdenticalClass() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.SameAttributeOnIdenticalClassConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void concatMethodsConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ConcatMethodsConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void ifBranchConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.IfBranchConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{11});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void sequenceConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.SequenceConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7, 9}, new int[]{8});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void sequenceConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.SequenceConflictSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{8, 9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void recursiveCallConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.RecursiveCallConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{15});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }

    @Test
    public void recursiveMockupConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.RecursiveMockupConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{10});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(3, analysis.getConflicts().size());
    }

    @Test
    public void recursiveMockupNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.RecursiveMockupNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{12});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void changeObjectPropagatinsField() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangeObjectPropagatinsFieldSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{11});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void changeObjectPropagatinsField2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangeObjectPropagatinsFieldSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{11});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void changeObjectPropagatinsField3() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangeObjectPropagatinsFieldSample3";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{11});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void changeObjectPropagatinsField4() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangeObjectPropagatinsFieldSample4";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{11});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void changeObjectPropagatinsField5() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangeObjectPropagatinsFieldSample5";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{11});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void changeObjectPropagatinsField6() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangeObjectPropagatinsFieldSample6";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{13});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void innerClassRecursiveTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.InnerClassRecursiveNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{24}, new int[]{5});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void twoSameObjectTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.TwoSameObjectSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        InterproceduralOverrideAssignment analysis = new InterproceduralOverrideAssignment(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }
}
