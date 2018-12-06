package br.unb.cic.df.analysis.model;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

public class StatementBuilder {
    private SootClass sootClass;
    private SootMethod sootMethod;
    private Unit unit;
    private Statement.Type type = Statement.Type.IN_BETWEEN;

    public StatementBuilder setClass(SootClass c) {
        sootClass = c;
        return this;
    }

    public StatementBuilder setMethod(SootMethod m) {
        sootMethod = m;
        return this;
    }

    public StatementBuilder setUnit(Unit u) {
        unit = u;
        return this;
    }

    public StatementBuilder setType(Statement.Type t) {
        type = t;
        return this;
    }

    public Statement build() {
        Statement s = new Statement(sootClass, sootMethod, unit, type);

        sootClass = null;
        sootMethod = null;
        unit = null;

        type = Statement.Type.IN_BETWEEN;

        return s;
    }
}