package br.unb.cic.analysis.df.pessimistic;

import br.unb.cic.analysis.model.Statement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Jimple;

public class PessimisticTaintedAnalysisAbstractionTest {

    PessimisticTaintedAnalysisAbstraction instance = null;

    Local simpleLocal;
    InstanceFieldRef simpleField;
    Statement emptyStatement;

    @Before
    public void configure() {
        Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.String");

        instance = new PessimisticTaintedAnalysisAbstraction();

        RefType mockType = RefType.v("java.lang.String");
        simpleLocal = Jimple.v().newLocal(
                "x", mockType
        );
        emptyStatement = Statement.builder().build();

        simpleField = Jimple.v().newInstanceFieldRef(
                simpleLocal, mockType.getSootClass().getFieldByName("value").makeRef()
        );
    }

    @Test
    public void testSimpleMarkAndCheckLocal() {
        instance.mark(simpleLocal, emptyStatement);
        Assert.assertTrue(instance.isMarked(simpleLocal));
        Assert.assertFalse(instance.isMarked(simpleField));
    }

    @Test
    public void testSimpleMarkAndCheckField() {
        instance.mark(simpleField, emptyStatement);
        Assert.assertTrue(instance.isMarked(simpleField));
        Assert.assertFalse(instance.isMarked(simpleLocal));
    }
}