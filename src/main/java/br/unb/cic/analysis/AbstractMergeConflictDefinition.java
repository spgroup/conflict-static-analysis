package br.unb.cic.analysis;

import br.unb.cic.analysis.model.Pair;
import br.unb.cic.analysis.model.Statement;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractMergeConflictDefinition {
    protected List<Statement> sourceStatements;
    protected List<Statement> sinkStatements;

    public AbstractMergeConflictDefinition() {
        sourceStatements = new ArrayList<>();
        sinkStatements = new ArrayList<>();
    }

    public void loadSourceStatements() {
        sourceStatements = loadStatements(sourceDefinitions(), Statement.Type.SOURCE);
    }

    public void loadSinkStatements() {
        sinkStatements = loadStatements(sinkDefinitions(), Statement.Type.SINK);
    }

    public List<Statement> getSourceStatements() {
        return sourceStatements;
    }

    public List<Statement> getSinkStatements() {
        return sinkStatements;
    }

    /**
     * This method should return a list of pairs, where the
     * first element is the full qualified name of
     * a class and the second element is a list of integers
     * stating the lines of code where does exist a "source"
     * statement.
     */
    protected abstract List<Pair<String, List<Integer>>> sourceDefinitions();

    /**
     * This method should return a list of pairs, where the
     * first element is the the full qualified name of
     * a class and the second element is a list of integers
     * stating the lines of code where does exist a "sink"
     * statement.
     */
    protected abstract List<Pair<String, List<Integer>>> sinkDefinitions();


    /*
     * just an auxiliary method to load the statements
     * related to either the source elements or sink
     * elements. this method only exists because it
     * avoids some duplicated code that might arise on
     * loadSourceStatements and loadSinkStatements.
     */
    private List<Statement> loadStatements(List<Pair<String, List<Integer>>> definitions, Statement.Type type) {
        List<Statement> statements = new ArrayList<>();
        Set<Integer> setOfStatementLines = new HashSet<>();
        for(Pair<String, List<Integer>> pair: definitions) {
            SootClass c = Scene.v().getSootClass(pair.getFirst());
            if(c == null) continue;
            for(SootMethod m: c.getMethods()) {
                for(Unit u: m.getActiveBody().getUnits()) {
                    if(pair.getSecond().contains(u.getJavaSourceStartLineNumber()) && !setOfStatementLines.contains(u.getJavaSourceStartLineNumber())) {
                        Statement stmt = Statement.builder().setClass(c).setMethod(m).setUnit(u).setType(type).build();
                        statements.add(stmt);
                        setOfStatementLines.add(u.getJavaSourceStartLineNumber());
                    }
                }
            }
        }
        return statements;
    }

    public Statement getExistingSinkNode(Statement s) {
        for(Statement sink: sinkStatements) {
            if(sink.getUnit().equals(s.getUnit())) {
                return sink;
            }
        }
        return s;
    }
}
