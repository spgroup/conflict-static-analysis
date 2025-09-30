package br.unb.cic.analysis.model;

import br.unb.cic.analysis.Main;

public class OAAnalysisRecord {

    private int deepLimit;
    private Statement statement;
    private int callGraphEdgesSize;
    private final CallGraphType callGraphType;
    private Main.AnalysisType analysisType;

    public OAAnalysisRecord(
            int deepLimit,
            Statement statement,
            int callGraphEdgesSize,
            CallGraphType callGraphType,
            Main.AnalysisType analysisType
    ) {
        this.deepLimit = deepLimit;
        this.statement = statement;
        this.callGraphEdgesSize = callGraphEdgesSize;
        this.callGraphType = callGraphType;
        this.analysisType = analysisType;
    }

    public int getDeepLimit() {
        return deepLimit;
    }

    public Statement getStatement() {
        return statement;
    }

    public int getCallGraphEdgesSize() {
        return callGraphEdgesSize;
    }

    public CallGraphType getCallGraphType() {
        return callGraphType;
    }

    public Main.AnalysisType getAnalysisType() {
        return analysisType;
    }

    @Override
    public String toString() {
        return String.format(
                "[Limit: %d] %s.%s:%d → %s (%d impl.) | CallGraph: %s | Analysis: %s",
                deepLimit,
                statement.getSootClass().getName(),
                statement.getSootMethod().getName(),
                statement.getSourceCodeLineNumber(),
                statement.getUnit(),
                callGraphEdgesSize,
                callGraphType,
                analysisType
        );
    }

    public enum CallGraphType {
        CHA,
        SPARK
    }
}
