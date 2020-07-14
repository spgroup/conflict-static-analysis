package br.unb.cic.analysis.df;

import br.unb.cic.analysis.model.Statement;
import soot.Local;
import soot.Value;
import soot.ValueBox;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JInstanceFieldRef;

import java.util.Objects;

/**
 * Information wee keep while traversing
 * the control flow.
 */
public class DataFlowAbstraction {

    private Local local;
    private JInstanceFieldRef localInstanceField;
    private JArrayRef localJArrayRef;
    private StaticFieldRef localStaticFieldRef;
    private Statement stmt;

    public DataFlowAbstraction(Value value, Statement stmt) {
        if (value instanceof Local) {
            this.local = (Local) value;
            this.stmt = stmt;
        } else if (value instanceof StaticFieldRef) {
            this.localStaticFieldRef = (StaticFieldRef) value;
            this.stmt = stmt;
        } else if (value instanceof JArrayRef) {
            this.localJArrayRef = (JArrayRef) value;
            this.stmt = stmt;
        }
    }

    public DataFlowAbstraction(Local local, Statement stmt) {
        this.local = local;
        this.stmt = stmt;
    }

    public DataFlowAbstraction(JInstanceFieldRef localInstanceField, Statement stmt) {
        this.localInstanceField = localInstanceField;
        this.stmt = stmt;
    }

    public DataFlowAbstraction(JArrayRef localJArrayRef, Statement stmt) {
        this.localJArrayRef = localJArrayRef;
        this.stmt = stmt;
    }

    public DataFlowAbstraction(StaticFieldRef localStaticFieldRef, Statement stmt) {
        this.localStaticFieldRef = localStaticFieldRef;
        this.stmt = stmt;
    }

    public Local getLocal() {
        return local;
    }

    public JArrayRef getLocalJArrayRef() {
        return localJArrayRef;
    }

    public StaticFieldRef getLocalStaticFieldRef() {
        return localStaticFieldRef;
    }

    public JInstanceFieldRef getJInstanceFieldRef() {
        return localInstanceField;
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
