package br.unb.cic.analysis.df;

import br.unb.cic.analysis.model.Statement;
import soot.Local;

/**
 * Information wee keep while traversing
 * the control flow.
 */
public class DataFlowAbstraction {

    private Local local;
    private Statement stmt;

    public DataFlowAbstraction(Local local, Statement stmt) {
        this.local = local;
        this.stmt = stmt;
    }

    public Local getLocal() {
        return local;
    }

    public Statement getStmt() {
        return stmt;
    }
}
