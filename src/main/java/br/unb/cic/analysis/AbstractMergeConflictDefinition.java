package br.unb.cic.analysis;

import br.unb.cic.analysis.model.Statement;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import javax.swing.plaf.nimbus.State;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This abstract class works as a contract. Whenever we
 * want to use a specific merge conflict analysis, we must
 * instantiate a concrete subclass, implementing the methods
 * source and sink definitions.
 */
public abstract class AbstractMergeConflictDefinition {
    protected List<Statement> sourceStatements;
    protected List<Statement> sinkStatements;
    protected List<Statement> inBetweenStatements;

    public AbstractMergeConflictDefinition() {
        sourceStatements = new ArrayList<>();
        sinkStatements = new ArrayList<>();
        inBetweenStatements = new ArrayList<>();
    }

    public void loadSourceStatements() {
        Map<String, List<Integer>> sourceDefinitions = sourceDefinitions();
        List<Statement> statements = loadStatements(sourceDefinitions.keySet(), Statement.Type.SOURCE);

        sourceStatements = filterIsInDefinitionsList(statements, sourceDefinitions);
    }

    public void loadSinkStatements() {
        Map<String, List<Integer>> sinkDefinitions = sinkDefinitions();
        List<Statement> statements = loadStatements(sinkDefinitions.keySet(), Statement.Type.SINK);

        sinkStatements = filterIsInDefinitionsList(statements, sinkDefinitions);
    }

    private List<Statement> filterIsInDefinitionsList(List<Statement> statements, Map<String, List<Integer>> definitions) {
        return statements.stream().filter(statement -> {
            String className = statement.getSootClass().getName();
            Integer lineNumber = statement.getSourceCodeLineNumber();

            return definitions.get(className).contains(lineNumber);
        }).collect(Collectors.toList());
    }

    public void loadInBetweenStatements() {
        Map<String, List<Integer>> sourceAndSinkDefinitions = sourceAndSinkDefinitions();

        List<Statement> statements = loadStatements(sourceAndSinkDefinitions.keySet(), Statement.Type.IN_BETWEEN);

        inBetweenStatements = statements.stream().filter(statement -> {
            String className = statement.getSootClass().getName();
            Integer lineNumber = statement.getSourceCodeLineNumber();

            return !sourceAndSinkDefinitions.containsKey(className)
                    || !sourceAndSinkDefinitions.get(className).contains(lineNumber);
        }).collect(Collectors.toList());
    }

    public List<Statement> getSourceStatements() {
        return sourceStatements;
    }

    public List<Statement> getSinkStatements() {
        return sinkStatements;
    }

    public List<Statement> getInBetweenStatements() { return inBetweenStatements; }

    public Map<String, List<Integer>> sourceAndSinkDefinitions() {
        Map<String, List<Integer>> sourceAndSyncDefinitions = new HashMap<>();
        Map<String, List<Integer>> sourceDefinitions = sourceDefinitions();
        Map<String, List<Integer>> sinkDefinitions = sinkDefinitions();

        for (String className : sourceDefinitions.keySet()) {
            List<Integer> lineList = sourceDefinitions.get(className);
            if (sinkDefinitions.containsKey(className)) {
                lineList.addAll(sinkDefinitions.get(className));
            }
            sourceAndSyncDefinitions.put(className, lineList);
        }

        for(String className : sinkDefinitions.keySet()) {
            if (!sourceAndSyncDefinitions.containsKey(className)) {
                sourceAndSyncDefinitions.put(className, sinkDefinitions.get(className));
            }
        }

        return sourceAndSyncDefinitions;
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
    private List<Statement> loadStatements(Set<String> classList, Statement.Type type) {
        List<Statement> statements = new ArrayList<>();
        for(String className: classList) {
            SootClass c = Scene.v().getSootClass(className);
            if(c == null || c.resolvingLevel() != SootClass.BODIES) continue;
            for(SootMethod m: c.getMethods()) {
                for(Unit u: m.retrieveActiveBody().getUnits()) {
                    Statement stmt = Statement.builder().setClass(c).setMethod(m)
                            .setUnit(u).setType(type).setSourceCodeLineNumber(u.getJavaSourceStartLineNumber())
                            .build();
                    statements.add(stmt);
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
