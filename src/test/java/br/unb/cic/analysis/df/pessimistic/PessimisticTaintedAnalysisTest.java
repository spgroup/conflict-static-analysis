package br.unb.cic.analysis.df.pessimistic;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.SootWrapper;
import br.unb.cic.analysis.model.Conflict;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Test;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;

import java.util.Map;
import java.util.Set;

public class PessimisticTaintedAnalysisTest {

    public static final String CLASSPATH = "target/test-classes";

    public Set<Conflict> executeAnalysis(String targetClass, int[] leftChangedLines, int[] rightChangedLines) {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASSPATH, leftChangedLines, rightChangedLines);

        final Set<Conflict>[] res = new Set[]{null};

        PackManager.v().getPack("jtp").add(
                new Transform("jtp.test", new BodyTransformer() {
                    @Override
                    protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
                        res[0] = new PessimisticTaintedAnalysis(body, definition).getConflicts();
                    }
                }));

        SootWrapper.builder()
                .withClassPath(CLASSPATH)
                .addClass(targetClass)
                .build()
                .execute();

        return res[0];
    }

    @Test
    public void testSimpleDataflow() {
        executeAnalysis("br.unb.cic.analysis.samples.IntraproceduralDataFlow", new int[]{6}, new int[]{11});
    }


}