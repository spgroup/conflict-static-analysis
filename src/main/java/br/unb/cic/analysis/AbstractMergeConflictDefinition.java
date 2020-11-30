package br.unb.cic.analysis;

import br.unb.cic.analysis.model.Statement;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;

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
    private boolean recursive;

    public AbstractMergeConflictDefinition() {
        this(false);
    }

    public AbstractMergeConflictDefinition(boolean recursive) {
        sourceStatements = new ArrayList<>();
        sinkStatements = new ArrayList<>();
        this.recursive = recursive;
    }

    public void loadSourceStatements() {
        Map<String, List<Integer>> sourceDefinitions = sourceDefinitions();
        sourceStatements = loadStatements(sourceDefinitions, Statement.Type.SOURCE);
    }

    public void loadSinkStatements() {
        Map<String, List<Integer>> sinkDefinitions = sinkDefinitions();
        sinkStatements = loadStatements(sinkDefinitions, Statement.Type.SINK);
    }

    private List<Statement> filterIsInDefinitionsList(List<Statement> statements, Map<String, List<Integer>> definitions) {
        return statements.stream().filter(statement -> {
            String className = statement.getSootClass().getName();
            Integer lineNumber = statement.getSourceCodeLineNumber();

            return definitions.get(className).contains(lineNumber);
        }).collect(Collectors.toList());
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
                for(Unit u: body.getUnits()) {
                    if(definitions.get(className).contains(u.getJavaSourceStartLineNumber())) {
                        Statement stmt = createStatement(m, u, type);
                        statements.add(stmt);
                    }
                }
            }
            if(recursive) {
                List<Statement> recursiveStatements = new ArrayList<>();
                List<SootMethod> traversedMethods = new ArrayList<>();
                for(Statement s: statements) {
                    if(s.getUnit() instanceof Stmt) {
                        Stmt stmt = (Stmt)s.getUnit();
                        if(stmt.containsInvokeExpr()) {
                            SootMethod sm = stmt.getInvokeExpr().getMethod();
                            recursiveStatements.addAll(traverse(sm, traversedMethods, type, 1));
                        }
                    }
                }
                statements.addAll(recursiveStatements);
            }
        }
        return statements;
    }

    public List<Statement> traverse(SootMethod sm, List<SootMethod> traversed, Statement.Type type, int level) {
        Body body = retrieveActiveBodySafely(sm);
        if(traversed.contains(sm) || level > 5 || (!sm.getDeclaringClass().isApplicationClass()) || (body == null)) {
            return new ArrayList<>();
        }
        level++;
        traversed.add(sm);

        List<Statement> res = new ArrayList<>();

        for(Unit u: body.getUnits()) {
            if(type.equals(Statement.Type.SOURCE) && u instanceof AssignStmt) {
                AssignStmt assignStmt = (AssignStmt) u;
                res.add(createStatement(sm, u, type));

                if(assignStmt.containsInvokeExpr()) {
                    res.addAll(traverse(assignStmt.getInvokeExpr().getMethod(), traversed, type, level));
                }
            }
            else if(type.equals(Statement.Type.SINK) && u.getUseBoxes().size() > 0) {
                res.add(createStatement(sm, u, type));
            }
            else if(u instanceof InvokeStmt) {
                InvokeStmt invokeStmt = (InvokeStmt)u;
                res.addAll(traverse(invokeStmt.getInvokeExpr().getMethod(), traversed, type, level));
            }
        }
        return res;
    }

    private Statement createStatement(SootMethod sm, Unit u,  Statement.Type type) {
        return Statement.builder().setClass(sm.getDeclaringClass()).setMethod(sm)
                .setUnit(u).setType(type).setSourceCodeLineNumber(u.getJavaSourceStartLineNumber())
                .build();
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
