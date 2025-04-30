package br.unb.cic.analysis.oa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.SootWrapper;
import br.unb.cic.analysis.model.Conflict;
import br.unc.cic.analysis.test.DefinitionFactory;
import br.unc.cic.analysis.test.MarkingClass;
import com.google.common.base.Stopwatch;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import soot.options.Options;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static br.unb.cic.analysis.SootWrapper.*;

public class OAInterWithoutPointerAnalysisTest {
    public static Stopwatch stopwatch;

    private void configureTest(OverrideAssignment analysis) {
        stopwatch = Stopwatch.createStarted();
        G.reset();

        SootWrapper.configureSootOptionsToRunInterproceduralOverrideAssignmentAnalysis("target/test-classes/", false);


        PackManager.v().getPack("wjtp").add(new Transform("wjtp.analysis", analysis));
        saveExecutionTime("Configure Soot OA Inter");

        analysis.configureEntryPoints();
        saveExecutionTime("Configure Entrypoints OA Inter");


        SootWrapper.applyPackages();

        try {
            exportResults(analysis.getConflicts());
        } catch (Exception e) {
            e.printStackTrace();
        }
        saveExecutionTime("Time to perform OA Inter");
    }

    public void saveExecutionTime(String description) {

        NumberFormat formatter = new DecimalFormat("#0.00000");

        long time = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        try {
            FileWriter myWriter = new FileWriter("time.txt", true);
            myWriter.write(description + ";" + formatter.format(time / 1000d) + "\n");
            System.out.println(description + " " + formatter.format(time / 1000d));
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
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
    public void localConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.LocalTestConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void fieldRefConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.FieldRefTestConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void loggingConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.LoggingConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{11});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void defaultConstructor() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.DefaultConstructorConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Ignore
    @Test
    public void StringArray() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.StringArraySample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{20});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void stacktraceConflictSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.StacktraceConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{11});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }

    @Test
    public void baseConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.BaseConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{15}, new int[]{17});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void baseNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.BaseNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{14}, new int[]{16});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void subclassWithConditionalNotConflict() {

        MarkingClass subclassWithConditionalNotConflictSample = new MarkingClass("br.unb.cic.analysis.samples.ioa" +
                ".SubclassWithConditionalNotConflictSample", new int[]{7}, new int[]{});
        MarkingClass c = new MarkingClass("br.unb.cic.analysis.samples.ioa.C", new int[]{27}, new int[]{});
        MarkingClass d = new MarkingClass("br.unb.cic.analysis.samples.ioa.D", new int[]{}, new int[]{36});

        List<MarkingClass> markingClassList = Arrays.asList(subclassWithConditionalNotConflictSample, c,
                d);

        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(markingClassList, false);

        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void subclassNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.SubclassConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{8});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void arrayConstant() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ArrayConstantSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{13});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void callGraph() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.CallGraphSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10, 13, 16}, new int[]{11, 14, 17});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(5, analysis.getConflicts().size());
    }

    @Ignore
    @Test
    public void ifWithInvokeConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.IfWithInvokeConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{12});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void containsInvokeExp() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ContainsInvokeExpConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{12});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Ignore
    @Test
    public void chainedMethodCallsConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChainedMethodCallsConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{12}, new int[]{13});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }

    @Test
    public void bothMarkingConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.BothMarkingConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{13});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void classFieldConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ClassFieldConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{11});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void classFieldConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ClassFieldConflictSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{11});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void classFieldNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ClassFieldNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{12});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void classFieldNotConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ClassFieldNotConflictSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{12});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Ignore
    @Test
    public void classFieldWithParameterNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ClassFieldWithParameterNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{13});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);

        // Not Conflict - Not implemented yet. You will need constant propagation.
        // Currently detected as conflict: [left, m():11] --> [right, foo():116]
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void localVariablesNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.LocalVariablesNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void localVariablesNotConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.LocalVariablesNotConflictSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void localVariablesWithParameterNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.LocalVariablesWithParameterNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    /**
     * in this case, we add java.util to the list of packages included in soot to be able to detect conflicts in the Hashmap class
     */
    @Test
    public void additionToArrayWithJavaUtilConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.AdditionToArrayConflictSample";
        List<String> stringList = new ArrayList<String>(Arrays.asList("java.util.*")); // java.util.* java.util.HashMap

        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{13});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);

        G.reset();

        String classpath = "target/test-classes/";
        List<String> testClasses = Collections.singletonList(classpath);

        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(soot.options.Options.output_format_jimple);
        Options.v().set_whole_program(true);
        Options.v().set_process_dir(testClasses);
        Options.v().set_full_resolver(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_include(stringList);

        // JAVA 8
        if (getJavaVersion() < 9) {
            Options.v().set_prepend_classpath(true);
            Options.v().set_soot_classpath(classpath + File.pathSeparator + pathToJCE() + File.pathSeparator + pathToRT());
        }
        // JAVA VERSION 9 && IS A CLASSPATH PROJECT
        else if (getJavaVersion() >= 9) {
            Options.v().set_soot_classpath("VIRTUAL_FS_FOR_JDK" + File.pathSeparator + classpath);
        }

        Options.v().setPhaseOption("jb.ls", "off"); // remove x = 1; x#2 = 2
        Options.v().setPhaseOption("jb", "use-original-names:true");

        enableCallGraph();

        Scene.v().loadNecessaryClasses();

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.analysis", analysis));
        analysis.configureEntryPoints();
        SootWrapper.applyPackages();


        try {
            exportResults(analysis.getConflicts());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals(6, analysis.getConflicts().size());
    }

    @Test
    public void additionToArrayWithoutJavaUtilNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.AdditionToArrayConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{13});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void hashmapWithJavaUtilConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.HashmapConflictSample";
        List<String> stringList = new ArrayList<String>(Arrays.asList("java.util.HashMap")); // java.util.* java.util.HashMap

        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{12});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        G.reset();

        String classpath = "target/test-classes/";
        List<String> testClasses = Collections.singletonList(classpath);

        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_whole_program(true);
        Options.v().set_process_dir(testClasses);
        Options.v().set_full_resolver(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_include(stringList);

        // JAVA 8
        if (getJavaVersion() < 9) {
            Options.v().set_prepend_classpath(true);
            Options.v().set_soot_classpath(classpath + File.pathSeparator + pathToJCE() + File.pathSeparator + pathToRT());
        }
        // JAVA VERSION 9 && IS A CLASSPATH PROJECT
        else if (getJavaVersion() >= 9) {
            Options.v().set_soot_classpath("VIRTUAL_FS_FOR_JDK" + File.pathSeparator + classpath);
        }


        Options.v().setPhaseOption("jb.ls", "off"); // remove x = 1; x#2 = 2
        Options.v().setPhaseOption("jb", "use-original-names:true");

        enableCallGraph(false);

        Scene.v().loadNecessaryClasses();

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.analysis", analysis));
        analysis.configureEntryPoints();
        SootWrapper.applyPackages();

        try {
            exportResults(analysis.getConflicts());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertEquals(107, analysis.getConflicts().size());
    }

    @Test
    public void hashmapWithoutJavaUtilNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.HashmapConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{12});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        //with java.util.*
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void changePublicAttributesConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangePublicAttributesConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7, 10}, new int[]{9});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }

    @Test
    public void PointsToDifferentMethods() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToDifferentMethodsSample.Cx";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{12}, new int[]{14});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void pointsToSameArray() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameArraySample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void pointsToSameArrayIndex() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameArrayDifferentIndexSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void pointsToSameArrayIndexSample() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameArrayIndexSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void pointsToOnlyOneObjectFromParametersWithMain() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToOnlyOneObjectFromParametersWithMainSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{12}, new int[]{14});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void pointsToConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameObjectSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }


    @Test
    public void pointsToNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToDifferentObjectSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void pointsToDifferentObjectFromParametersNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToDifferentObjectFromParametersSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void pointsToDifferentObjectFromParametersWithMainNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToDifferentObjectFromParametersWithMainSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{13});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void pointsToSameObjectFromParametersWithMain() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameObjectFromParametersWithMainSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{13});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }


    @Test
    public void pointsToSameObjectFromParameters() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameObjectFromParametersSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void pointsToSameObjectFromParametersNotConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameObjectFromParametersSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void pointsToSameObjectFromParametersNotConflict3() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameObjectFromParametersSample3";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void pointsToSameObjectFromParameters4() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.PointsToSameObjectFromParametersSample4";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void changeInstanceAttributeConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangeInstanceAttributeConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{12});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }

    @Test
    public void differentMethodOnIdenticalClass() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.DifferentMethodOnIdenticalClassConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{12}, new int[]{13});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Ignore
    @Test
    public void localArraysNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.LocalArrayNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Ignore
    @Test
    public void localArraysRecursiveNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.LocalArrayRecursiveNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void classFieldArraysConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ArraysClassFieldConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{13}, new int[]{14});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void classFieldArraysNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ArraysClassFieldNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void arraysAliasingConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ArrayAliasingConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{12}, new int[]{13});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void arrayDiferentIndexNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ArrayDiferentIndexNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{9});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void arraySameIndexConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ArraySameIndexConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{11});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void staticClassFieldNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.StaticClassFieldNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void staticClassFieldConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.StaticClassFieldConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{10});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void objectFieldConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ObjectFieldConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void objectFieldNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ObjectFieldNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void threeDepthObjectsConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ObjectThreeFieldsOneConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{15, 18}, new int[]{16, 19});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void differentAttributeOnIdenticalClassNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.DifferentAttributeOnIdenticalClassNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{16}, new int[]{17});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void differentClassWithSameAttributeNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.DifferentClassWithSameAttributeNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{16}, new int[]{17});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void sameAttributeOnIdenticalClass() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.SameAttributeOnIdenticalClassConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{10});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void concatMethodsConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ConcatMethodsConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{10});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void ifBranchConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.IfBranchConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{11});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void sequenceConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.SequenceConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7, 9}, new int[]{8});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void sequenceConflict2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.SequenceConflictSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{8, 9});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void recursiveCallConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.RecursiveCallConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{8}, new int[]{15});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(2, analysis.getConflicts().size());
    }

    @Test
    public void recursiveMockupConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.RecursiveMockupConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{10});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Ignore
    @Test
    public void recursiveMockupNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.RecursiveMockupNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{12});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Ignore
    @Test
    public void changeObjectPropagatinsField() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangeObjectPropagatinsFieldSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{11});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Ignore
    @Test
    public void changeObjectPropagatinsField2() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangeObjectPropagatinsFieldSample2";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{11});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Ignore
    @Test
    public void changeObjectPropagatinsFieldNotConflict3() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangeObjectPropagatinsFieldSample3";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{11});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Ignore
    @Test
    public void changeObjectPropagatinsField4() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangeObjectPropagatinsFieldSample4";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{11});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Ignore
    @Test
    public void changeObjectPropagatinsField5() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangeObjectPropagatinsFieldSample5";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{11});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Ignore
    @Test
    public void changeObjectPropagatinsField6() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangeObjectPropagatinsFieldSample6";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{10}, new int[]{13});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Ignore
    @Test
    public void changeObjectPropagatinsField7() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.ChangeObjectPropagatinsFieldSample7";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{9}, new int[]{11});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void innerClassRecursiveNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.InnerClassRecursiveNotConflictSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{24}, new int[]{5});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void twoSameObjectNotConflict() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.TwoSameObjectSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{7}, new int[]{9});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(0, analysis.getConflicts().size());
    }

    @Test
    public void baseConflictOneEntrypointsTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.BaseConflictOneEntrypointsSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11}, new int[]{17});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void callGraphFromMainTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.CallGraphFromMainSample.Text";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{13}, new int[]{15});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void pointerAnalisysReflectionTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.CallGraphFromMainSample.TextReflect";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{15}, new int[]{17});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(4, analysis.getConflicts().size());
    }

    @Test
    public void pointerAnalisysProxyTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.CallGraphFromMainSample.TextProxy";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{29}, new int[]{31});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());
    }

    @Test
    public void retrofitToyTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.retrofit.RestAdapter";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{34}, new int[]{36});
        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition);
        configureTest(analysis);
        Assert.assertEquals(4, analysis.getConflicts().size());
    }

    @Test
    public void baseConflictTwoEntrypointsTest() {
        String sampleClassPath = "br.unb.cic.analysis.samples.ioa.BaseConflictTwoEntrypointsSample";
        AbstractMergeConflictDefinition definition = DefinitionFactory
                .definition(sampleClassPath, new int[]{11, 16}, new int[]{18});

        List<String> entrypoints = new ArrayList<>();
        entrypoints.add("void main(java.lang.String[])");

        OverrideAssignment analysis = new OverrideAssignmentWithoutPointerAnalysis(definition, 5, true, entrypoints);

        stopwatch = Stopwatch.createStarted();
        G.reset();


        SootWrapper.configureSootOptionsToRunInterproceduralOverrideAssignmentAnalysis("target/test-classes/");


        PackManager.v().getPack("wjtp").add(new Transform("wjtp.analysis", analysis));
        saveExecutionTime("Configure Soot OA Inter");

        analysis.configureEntryPoints();
        saveExecutionTime("Configure Entrypoints OA Inter");


        SootWrapper.applyPackages();

        try {
            exportResults(analysis.getConflicts());
        } catch (Exception e) {
            e.printStackTrace();
        }
        saveExecutionTime("Time to perform OA Inter");

        //configureTest(analysis);
        Assert.assertEquals(1, analysis.getConflicts().size());


    }
}
