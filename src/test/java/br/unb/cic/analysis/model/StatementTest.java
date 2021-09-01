package br.unb.cic.analysis.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import soot.RefType;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.StringConstant;

public class StatementTest {

    InvokeStmt simpleInvoke;
    AssignStmt simpleAssign;
    AssignStmt assignWithInvoke;

    @Before
    public void configure() {
        Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.String");

        RefType typeString = RefType.v("java.lang.String");

        simpleAssign = Jimple.v().newAssignStmt(
                Jimple.v().newLocal("x", typeString),
                StringConstant.v("hello")
        );


        SootMethod toCall = typeString.getSootClass().getMethod("int hashCode()");
        simpleInvoke = Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(
                        Jimple.v().newLocal("x", typeString),
                        toCall.makeRef()
                )
        );

        assignWithInvoke = Jimple.v().newAssignStmt(
                Jimple.v().newLocal("y", RefType.v("int")),
                Jimple.v().newVirtualInvokeExpr(
                        Jimple.v().newLocal("x", typeString),
                        toCall.makeRef()

                )
        );

    }

    @Test
    public void testIsAssignWithLocal() {
        Statement stmt = Statement.builder().setUnit(simpleAssign).build();

        Assert.assertTrue(stmt.isAssign());
    }

    @Test
    public void testIsAssignWithoutAssign() {
        Statement stmt = Statement.builder().setUnit(simpleInvoke).build();

        Assert.assertFalse(stmt.isAssign());
    }

    @Test
    public void testIsAssignAssignmentFromMethodResult() {
        Statement stmt = Statement.builder().setUnit(assignWithInvoke).build();

        Assert.assertTrue(stmt.isAssign());
    }

    @Test
    public void testIsInvokeWithSimpleInvoke() {
        Statement stmt = Statement.builder().setUnit(simpleInvoke).build();

        Assert.assertTrue(stmt.isInvoke());
    }

    @Test
    public void testIsInvokeWithoutInvoke() {
        Statement stmt = Statement.builder().setUnit(simpleAssign).build();

        Assert.assertFalse(stmt.isInvoke());
    }

    @Test
    public void testIsInvokeAssignmentFromMethodResult() {
        Statement stmt = Statement.builder().setUnit(assignWithInvoke).build();

        Assert.assertTrue(stmt.isInvoke());
    }

}