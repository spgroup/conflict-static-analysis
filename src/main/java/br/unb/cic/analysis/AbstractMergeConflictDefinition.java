package br.unb.cic.analysis;

import br.unb.cic.analysis.model.Statement;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import java.util.*;

/**
 * This abstract class works as a contract. Whenever we
 * want to use a specific merge conflict analysis, we must
 * instantiate a concrete subclass, implementing the methods
 * source and sink definitions.
 */
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
     * stating the lines of code where exists a "source"
     * statement.
     */
    protected abstract Map<String, List<Integer>> sourceDefinitions();

    /**
     * This method should return a list of pairs, where the
     * first element is the the full qualified name of
     * a class and the second element is a list of integers
     * stating the lines of code where does exist a "sink"
     * statement.
     */
    protected abstract Map<String, List<Integer>> sinkDefinitions();


    /*
     * just an auxiliary method to load the statements
     * related to either the source elements or sink
     * elements. this method only exists because it
     * avoids some duplicated code that might arise on
     * loadSourceStatements and loadSinkStatements.
     */
    private List<Statement> loadStatements(Map<String, List<Integer>> definitions, Statement.Type type) {
        List<Statement> statements = new ArrayList<>();
        for(String className: definitions.keySet()) {
            SootClass c = Scene.v().getSootClass(className);
            if(c == null || c.resolvingLevel() != SootClass.BODIES) continue;
            for(SootMethod m: c.getMethods()) {
                for(Unit u: m.retrieveActiveBody().getUnits()) {
                    if(definitions.get(className).contains(u.getJavaSourceStartLineNumber())) {
                        Statement stmt = Statement.builder().setClass(c).setMethod(m)
                                .setUnit(u).setType(type).setSourceCodeLineNumber(u.getJavaSourceStartLineNumber())
                                .build();
                        statements.add(stmt);
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

    public boolean isSourceStatement(Unit u) {
        return sourceStatements.stream().anyMatch(s -> s.getUnit().equals(u));
    }
}
