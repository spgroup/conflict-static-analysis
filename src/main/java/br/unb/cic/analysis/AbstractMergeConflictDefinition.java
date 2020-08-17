package br.unb.cic.analysis;

import br.unb.cic.analysis.model.Statement;
import soot.*;

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

    public AbstractMergeConflictDefinition() {
        sourceStatements = new ArrayList<>();
        sinkStatements = new ArrayList<>();
    }

    public void loadSourceStatements() {
        Map<String, List<Integer>> sourceDefinitions = sourceDefinitions();
        sourceStatements = loadStatements(sourceDefinitions, Statement.Type.SOURCE);
    }

    public void loadSinkStatements() {
        Map<String, List<Integer>> sinkDefinitions = sinkDefinitions();
        sinkStatements = loadStatements(sinkDefinitions, Statement.Type.SINK);
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
        List<SootClass> classes = listSootClasses();
        for(SootClass aClass: classes) {
            if(aClass.resolvingLevel() != SootClass.BODIES || aClass.isPhantomClass()) continue;
            String className = retriveClassNameInDefinitions(aClass, definitions);
            if(className == null) continue;
            for(SootMethod m: aClass.getMethods()) {
                Body body = retrieveActiveBodySafely(m);
                if(body == null) continue;
                HashMap<Integer, Unit> lineUnitMapping = defineLineRange(body);
                for(Integer def : definitions.get(className)) {
                    if(lineUnitMapping.containsKey(def)) {
                        Unit u = lineUnitMapping.get(def);
                        Statement stmt = Statement.builder().setClass(aClass).setMethod(m)
                                .setUnit(u).setType(type).setSourceCodeLineNumber(def)
                                .build();
                        statements.add(stmt);
                    }
                }
            }
        }
        return statements;
    }

    private HashMap<Integer, Unit>  defineLineRange(Body body) {
        HashMap<Integer, Unit> lineUnitMapping = new HashMap<>();
        List<Integer> lines = new ArrayList<>();

        for(Unit unit: body.getUnits()) {
            Integer line = unit.getJavaSourceStartLineNumber();
            if(line < 0) continue;
            lines.add(line);
            lineUnitMapping.put(line, unit);
        }
        Collections.sort(lines);

        for(int i = 0; i < lines.size()-1; i++) {
            int current = lines.get(i);
            int next = lines.get(i + 1);
            Unit unit = lineUnitMapping.get(current);
            for(int j = current + 1; j < next; j++) {
                lineUnitMapping.put(j, unit);
            }
        }
        return lineUnitMapping;
    }

    /*
     * It returns a class name in the definitions set or
     * return null. It also searches in the outer class of
     * aClass.
     */
    private String retriveClassNameInDefinitions(SootClass aClass, Map<String, List<Integer>> definitions) {
        if(definitions.containsKey(aClass.getName())) {
            return aClass.getName();
        }
        SootClass outerClass = retrieveOuterClass(aClass);
        if(outerClass != null && definitions.containsKey(outerClass.getName())) {
            return outerClass.getName();
        }
        return null;
    }

    /*
     * Retrieves the outer class. Either using the
     * getOuterClass method (if the isInnerClass returns true),
     * or finding the name of the outer class via substring.
     */
    private SootClass retrieveOuterClass(SootClass aClass) {
        if(aClass.isInnerClass()) {
            return aClass.getOuterClass();
        }
        if(aClass.getName().contains("$")) {
            int idx = aClass.getName().indexOf("$");
            String outer = aClass.getName().substring(0, idx);
            return Scene.v().getSootClass(outer); // note: this method getSootClass might throw a RuntimeException.
        }
        return null;
    }


    /*
     * Retrieves the active body of an method, if any. There is
     * no simple way to check whether we can retrieve an active method
     * or not. So, in this implementation, we first try to retrieve
     * one. If an exception is thrown, we just return null.
     */
    private Body retrieveActiveBodySafely(SootMethod method) {
        try {
            return method.retrieveActiveBody();
        }
        catch(RuntimeException e) {
            return null;
        }
    }

    private List<SootClass> listSootClasses() {
        List<SootClass> classes = new ArrayList<>();
        for(SootClass c: Scene.v().getApplicationClasses()) {
            Scene.v().loadClass(c.getName(), SootClass.BODIES);
            classes.add(c);
        }
        return classes;
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
