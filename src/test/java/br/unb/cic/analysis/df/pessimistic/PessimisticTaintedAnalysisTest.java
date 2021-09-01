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

        return conflicts;
    }

    @Test
    public void testSimpleDataflow() {
        Set<Conflict> conflicts = executeAnalysis("br.unb.cic.analysis.samples.IntraproceduralDataFlow", new int[]{6}, new int[]{11});
        Assert.assertTrue(conflicts.size() == 1);
    }

    @Test
    public void testSimpleFieldDataflow() {
        Set<Conflict> conflicts = executeAnalysis("br.unb.cic.analysis.samples.IntraproceduralDataflowField", new int[]{8}, new int[]{10});
        System.out.println(conflicts);
        Assert.assertTrue(conflicts.size() == 1);
    }

    @Test
    public void testInterproceduralDataflowUsingInterface() {
        Set<Conflict> conflicts = executeAnalysis("br.unb.cic.analysis.samples.InterproceduralDataflowUsingInterface", new int[]{11}, new int[]{12});
        System.out.println(conflicts);
        Assert.assertTrue(conflicts.size() == 1);
    }

    @Test
    public void testInterproceduralDataflowSameClass() {
        // InterproceduralDataflowField
        Set<Conflict> conflicts = executeAnalysis("br.unb.cic.analysis.samples.InterproceduralDataflowField", new int[]{8}, new int[]{10});
        System.out.println(conflicts);
        Assert.assertTrue(conflicts.size() == 1);
    }

}