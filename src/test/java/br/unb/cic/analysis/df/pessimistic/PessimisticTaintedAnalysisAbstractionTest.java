package br.unb.cic.analysis.df.pessimistic;

import br.unb.cic.analysis.model.Statement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import soot.G;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Jimple;
import soot.jimple.StringConstant;

public class PessimisticTaintedAnalysisAbstractionTest {

    PessimisticTaintedAnalysisAbstraction instance = null;

    Local local;
    Local otherLocal;
    InstanceFieldRef field;
    InstanceFieldRef otherField;
    AssignStmt assignUnit;

    Statement emptyStatement;
    Statement assignStatement;

    @Before
    public void configure() {
        G.reset();
        Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.String");

        instance = new PessimisticTaintedAnalysisAbstraction();

        RefType mockType = RefType.v("java.lang.String");
        local = Jimple.v().newLocal(
                "x", mockType
        );

        otherLocal = Jimple.v().newLocal(
                "y", mockType
        );

        field = Jimple.v().newInstanceFieldRef(
                local, mockType.getSootClass().getFieldByName("value").makeRef()
        );

        otherField = Jimple.v().newInstanceFieldRef(
                local, mockType.getSootClass().getFieldByName("hash").makeRef()
        );

        assignUnit = Jimple.v().newAssignStmt(
                local,
                Jimple.v().newAddExpr(
                        otherLocal,
                        StringConstant.v("hello")
                )
        );

        assignStatement = Statement.builder().setUnit(assignUnit).build();
        emptyStatement = Statement.builder().build();
    }

    @Test
    public void testMarkAndIsMarked() {
        instance.mark(local, emptyStatement);
        // marks the whole value, with its fields
        Assert.assertTrue(instance.isMarked(local));
        Assert.assertTrue(instance.isMarked(field));
        Assert.assertFalse(instance.isMarked(otherLocal));
    }

    @Test
    public void testMarkFieldAndIsMarked() {
        instance.mark(field, emptyStatement);
        // marks only the field and not its base and other fields
        Assert.assertTrue(instance.isMarked(field));
        Assert.assertFalse(instance.isMarked(local));
        Assert.assertFalse(instance.isMarked(otherField));
    }

    @Test
    public void testMarkFieldsAndIsMarked() {
        instance.markFields(local, emptyStatement);
        // marks all the fields and not the instance
        Assert.assertFalse(instance.isMarked(local));
        Assert.assertTrue(instance.isMarked(field));
        Assert.assertTrue(instance.isMarked(otherField));
    }

    @Test
    public void testMarkFieldsAndHasMarkedFields() {
        instance.markFields(local, emptyStatement);
        // when all fields are marked it should return that has marked fields
        Assert.assertTrue(instance.hasMarkedFields(local));
    }

    @Test
    public void testMarkFieldAndHasMarkedFields() {
        instance.mark(field, emptyStatement);

        // mark an individual field and check that the hasMarkedFields
        // returns true
        Assert.assertTrue(instance.hasMarkedFields(local));
    }

    @Test
    public void testMarkBaseAndHasMarkedFields() {
        instance.mark(local, emptyStatement);
        // Marks the whole object and check that the fields are marked as well
        Assert.assertTrue(instance.hasMarkedFields(local));
    }

    @Test
    public void testCopy() {
        instance.mark(local, emptyStatement);
        instance.markFields(otherLocal, emptyStatement);
        PessimisticTaintedAnalysisAbstraction target = new PessimisticTaintedAnalysisAbstraction();

        instance.copy(target);

        Assert.assertEquals(instance, target);
        Assert.assertTrue(target.isMarked(local));
    }

    @Test
    public void testUnionWithTarget() {
        PessimisticTaintedAnalysisAbstraction instance2 = new PessimisticTaintedAnalysisAbstraction();
        PessimisticTaintedAnalysisAbstraction target = new PessimisticTaintedAnalysisAbstraction();

        instance.mark(local, emptyStatement);
        instance2.markFields(otherLocal, emptyStatement);

        instance.merge(instance2, target);

        Assert.assertTrue(target.isMarked(local));
        Assert.assertTrue(target.hasMarkedFields(otherLocal));
    }

    @Test
    public void markAndUnmark() {
        instance.mark(local, emptyStatement);

        Assert.assertTrue(instance.isMarked(local));
        Assert.assertTrue(instance.hasMarkedFields(local));

        instance.unmark(local);

        Assert.assertFalse(instance.isMarked(local));
        Assert.assertFalse(instance.hasMarkedFields(local));
    }

    @Test
    public void markFieldsAndUnmarkSpecificField() {
        instance.markFields(local, emptyStatement);

        Assert.assertTrue(instance.isMarked(field));

        instance.unmark(field);

        Assert.assertFalse(instance.isMarked(field));
        Assert.assertTrue(instance.hasMarkedFields(local));
    }

    @Test
    public void markObjectAndUnmarkField() {
        instance.mark(local, emptyStatement);

        Assert.assertTrue(instance.isMarked(local));
        Assert.assertTrue(instance.isMarked(field));
        Assert.assertTrue(instance.hasMarkedFields(local));

        instance.unmark(field);

        Assert.assertTrue(instance.isMarked(local));
        Assert.assertFalse(instance.isMarked(field));
        Assert.assertTrue(instance.hasMarkedFields(local));
    }

    @Test
    public void markUnmarkAndMarkAgain() {
        instance.mark(local, emptyStatement);

        Assert.assertTrue(instance.isMarked(local));
        Assert.assertTrue(instance.hasMarkedFields(local));

        instance.unmark(local);

        Assert.assertFalse(instance.isMarked(local));
        Assert.assertFalse(instance.hasMarkedFields(local));

        instance.mark(local, emptyStatement);

        Assert.assertTrue(instance.isMarked(local));
        Assert.assertTrue(instance.hasMarkedFields(local));
    }

    @Test
    public void markObjectUnmarkFieldAndMarkFields() {
        instance.mark(local, emptyStatement);

        Assert.assertTrue(instance.isMarked(local));
        Assert.assertTrue(instance.hasMarkedFields(local));
        Assert.assertTrue(instance.isMarked(field));

        instance.unmark(field);

        Assert.assertTrue(instance.isMarked(local));
        Assert.assertTrue(instance.hasMarkedFields(local));
        Assert.assertFalse(instance.isMarked(field));

        instance.markFields(local, emptyStatement);

        Assert.assertTrue(instance.isMarked(local));
        Assert.assertTrue(instance.hasMarkedFields(local));
        Assert.assertTrue(instance.isMarked(field));
    }

    @Test
    public void markLocalAndCheckIfStatementsUsesAMarkedValue() {
        instance.mark(otherLocal, emptyStatement);

        Assert.assertTrue(instance.usesMarkedValue(assignStatement));
    }

    @Test
    public void markLocalThatIsNotUsedAndCheckIfUsesAMarkedValue() {
        instance.mark(local, emptyStatement);

        Assert.assertFalse(instance.usesMarkedValue(assignStatement));
    }

}