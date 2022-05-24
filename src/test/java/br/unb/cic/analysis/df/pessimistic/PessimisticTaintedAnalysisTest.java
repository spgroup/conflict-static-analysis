package br.unb.cic.analysis.df.pessimistic;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.SootWrapper;
import br.unb.cic.analysis.model.Conflict;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Assert;
import org.junit.Test;
import soot.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PessimisticTaintedAnalysisTest {

    private static final String CLASSPATH = "target/test-classes";
    private static final String SAMPLES_PACKAGE = "br.unb.cic.analysis.samples.";

    private static final String INTRAPROCEDURAL_DATAFLOW = getSampleFullName("IntraproceduralDataFlow");
    private static final String INTRAPROCEDURAL_DATAFLOW_FIELD = getSampleFullName("IntraproceduralDataflowField");
    private static final String INTERPROCEDURAL_DATAFLOW_INTERFACE = getSampleFullName("InterproceduralDataflowUsingInterface");
    private static final String INTERPROCEDURAL_DATAFLOW_FIELD = getSampleFullName("InterproceduralDataflowField");
    private static final String INTRAPROCEDURAL_INDIRECT_DATAFLOW = getSampleFullName("IntraproceduralIndirectSource");
    private static final String INTRAPROCEDURAL_DATAFLOW_SPECIAL_CASES = getSampleFullName("IntraproceduralDataflowFieldSpecialCases");

    static String getSampleFullName(String className) {
        return SAMPLES_PACKAGE + className;
    }

    public Set<Conflict> executeAnalysis(String targetClass, int[] leftChangedLines, int[] rightChangedLines) {
        G.reset();
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(targetClass, leftChangedLines, rightChangedLines);

        Set<Conflict> conflicts = new HashSet<>();

        PackManager.v().getPack("jtp").add(
                new Transform("jtp.test", new BodyTransformer() {
                    @Override
                    protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
                        conflicts.addAll(new PessimisticTaintedAnalysis(body, definition).getConflicts());
                    }
                }));

        SootWrapper.builder()
                .withClassPath(CLASSPATH)
                .addClass(targetClass)
                .build()
                .execute();
        System.out.println(conflicts);
        return conflicts;
    }

    @Test
    public void testSimpleDataflow() {
        Set<Conflict> conflicts = executeAnalysis(INTRAPROCEDURAL_DATAFLOW, new int[]{6}, new int[]{11});
        Assert.assertTrue(conflicts.size() == 1);
    }

    @Test
    public void testCanceledDataflow() {
        Set<Conflict> conflicts = executeAnalysis(INTRAPROCEDURAL_DATAFLOW, new int[]{19}, new int[]{26});
        Assert.assertTrue(conflicts.size() == 0);
    }

    @Test
    public void testSimpleFieldDataflow() {
        Set<Conflict> conflicts = executeAnalysis(INTRAPROCEDURAL_DATAFLOW_FIELD, new int[]{8}, new int[]{10});
        Assert.assertTrue(conflicts.size() == 2);
    }

    @Test
    public void testFieldCanceledDataflow() {
        Set<Conflict> conflicts = executeAnalysis(INTRAPROCEDURAL_DATAFLOW_FIELD, new int[]{14}, new int[]{16});
        Assert.assertTrue(conflicts.size() == 0);
    }

    @Test
    public void testMethodCallCanceledFieldDataflow() {
        Set<Conflict> conflicts = executeAnalysis(INTRAPROCEDURAL_DATAFLOW_FIELD, new int[]{22}, new int[]{26});
        Assert.assertTrue(conflicts.size() == 0);
    }

    @Test
    public void testFieldCanceledAndMarkedAgain() {
        Set<Conflict> conflicts = executeAnalysis(INTRAPROCEDURAL_DATAFLOW_FIELD, new int[]{30, 34}, new int[]{36});
        Assert.assertTrue(conflicts.size() == 2);
    }

    @Test
    public void testInterproceduralDataflowSameClass() {
        Set<Conflict> conflicts = executeAnalysis(INTERPROCEDURAL_DATAFLOW_FIELD, new int[]{8}, new int[]{10});
        Assert.assertTrue(conflicts.size() == 1);
    }

    @Test
    public void testInterproceduralDataflowUsingInterface() {
        Set<Conflict> conflicts = executeAnalysis(INTERPROCEDURAL_DATAFLOW_INTERFACE, new int[]{11}, new int[]{12});
        Assert.assertTrue(conflicts.size() == 1);
    }

    @Test
    public void testIntraproceduralIndirectSource() {
        Set<Conflict> conflicts = executeAnalysis(INTRAPROCEDURAL_INDIRECT_DATAFLOW, new int[]{7}, new int[]{12});
        Assert.assertTrue(conflicts.size() == 1);
    }

    @Test
    public void testIntraproceduralIndirectSourceMethodCall() {
        Set<Conflict> conflicts = executeAnalysis(INTRAPROCEDURAL_INDIRECT_DATAFLOW, new int[]{22}, new int[]{26});
        Assert.assertTrue(conflicts.size() == 1);
    }

    @Test
    public void testIntraproceduralDataflowBothCallMethodField() {
        Set<Conflict> conflicts = executeAnalysis(INTRAPROCEDURAL_DATAFLOW_SPECIAL_CASES, new int[]{12}, new int[]{14});
        Assert.assertTrue(conflicts.size() == 1);
    }

    @Test
    public void testIntraproceduralDataflowOneAssignAndOtherCallMethodField() {
        Set<Conflict> conflicts = executeAnalysis(INTRAPROCEDURAL_DATAFLOW_SPECIAL_CASES, new int[]{18}, new int[]{20});
        Assert.assertTrue(conflicts.size() >= 1);
    }

    @Test
    public void testIntraproceduralDataflowBothCallMethodFieldIndirect() {
        Set<Conflict> conflicts = executeAnalysis(INTRAPROCEDURAL_DATAFLOW_SPECIAL_CASES, new int[]{24}, new int[]{26});
        Assert.assertTrue(conflicts.size() >= 1);
    }

}