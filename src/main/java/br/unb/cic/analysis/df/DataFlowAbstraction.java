package br.unb.cic.analysis.df;

import br.unb.cic.analysis.model.Statement;
import soot.Local;
import soot.Value;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeStmt;
import soot.jimple.StaticFieldRef;

import java.util.*;

/**
 * Information wee keep while traversing
 * the control flow.
 */
public class DataFlowAbstraction {

    private Local local;
    private InstanceFieldRef localField;
    private StaticFieldRef localStaticRef;
    private InvokeStmt methodCall;
    private Statement stmt;
    private Value value;

    public DataFlowAbstraction(InvokeStmt methodCall, Statement stmt){
        this.methodCall = methodCall;
        this.stmt = stmt;
    }

    public DataFlowAbstraction(Value value, Statement stmt) {
        this.value = value;
        this.stmt = stmt;
    }

    public DataFlowAbstraction(Local local, Statement stmt) {
        this.local = local;
        this.stmt = stmt;
    }

    public DataFlowAbstraction(InstanceFieldRef localField, Statement stmt) {
        this.localField = localField;
        this.stmt = stmt;
    }

    public DataFlowAbstraction(StaticFieldRef localStaticRef, Statement stmt) {
        this.localStaticRef = localStaticRef;
        this.stmt = stmt;
    }

    public InvokeStmt getMethodCall() {
        return methodCall;
    }

    public void setMethodCall(InvokeStmt methodCall) {
        this.methodCall = methodCall;
    }

    public Local getLocal() {
        return local;
    }

    public StaticFieldRef getLocalStaticRef() {
        return localStaticRef;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public InstanceFieldRef getFieldRef() {
        return localField;
    }
    public Statement getStmt() {
        return stmt;
    }

    public Boolean containsLeftStatement(){
        return getStmt().getType().equals(Statement.Type.SOURCE);
    }

    public Boolean containsRightStatement(){
        return getStmt().getType().equals(Statement.Type.SINK);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataFlowAbstraction that = (DataFlowAbstraction) o;
        return Objects.equals(local, that.local) &&
                Objects.equals(stmt, that.stmt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(local, stmt);
    }
}
