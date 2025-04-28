package br.unb.cic.analysis;

import br.unb.cic.analysis.model.Statement;
import br.unb.cic.analysis.model.TraversedLine;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;

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
    private Set<SootMethod> entryMethods;
    private boolean recursive;
    private int omitExceptingUnitEdges; //1 - true and 2-false

    public AbstractMergeConflictDefinition() {
        this(false);
    }

    public AbstractMergeConflictDefinition(boolean recursive) {
        sourceStatements = new ArrayList<>();
        sinkStatements = new ArrayList<>();
        entryMethods = new HashSet<>();
        this.recursive = recursive;
    }

    public AbstractMergeConflictDefinition(int omitExceptingUnitEdges) {
        sourceStatements = new ArrayList<>();
        sinkStatements = new ArrayList<>();
        this.recursive = false;
        this.omitExceptingUnitEdges = omitExceptingUnitEdges;
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
                for(Unit u: body.getUnits()) {
                    if(definitions.get(className).contains(u.getJavaSourceStartLineNumber())) {
                        Statement stmt = createStatement(m, u, type);
                        statements.add(stmt);

                        List<TraversedLine> actual_traversedLine = new ArrayList<>();
                        TraversedLine traversedLine_from_invoked_method = new TraversedLine(m.getDeclaringClass(), m, u.getJavaSourceStartLineNumber());
                        // add an element from the current statement
                        actual_traversedLine.add(traversedLine_from_invoked_method);
                        stmt.setTraversedLine(actual_traversedLine);

                        if (stmt.isSource()) {
                            entryMethods.add(m);
                        }
                    }
                }
            }
        }
        if(recursive) {
            List<Statement> recursiveStatements = new ArrayList<>();
            List<SootMethod> traversedMethods = new ArrayList<>();

            for(Statement actual_stmt: statements) {
                if(actual_stmt.getUnit() instanceof Stmt) {
                    Stmt unit_from_stmt = (Stmt) actual_stmt.getUnit();

                    if(unit_from_stmt.containsInvokeExpr()) {

                        Statement statement = getGeneratedStatementFromUnitWithTraversedLine(actual_stmt, unit_from_stmt, type);

                        // add statement with TraversedLine list
                        recursiveStatements.add(statement);

                        SootMethod invoked_method = unit_from_stmt.getInvokeExpr().getMethod();

                        //call traverse passing currently travesed line list
                        recursiveStatements.addAll(traverse(invoked_method, traversedMethods, statement.getTraversedLine(), type, 1));
                    }
                }
            }
            statements.addAll(recursiveStatements);
        }
        return statements;
    }

    private Statement getGeneratedStatementFromUnitWithTraversedLine(Statement  actual_stmt, Stmt unit_from_stmt, Statement.Type type){
        SootMethod actual_method = actual_stmt.getSootMethod();
        Statement statement = createStatement(actual_method, unit_from_stmt, type);
        TraversedLine actual_traversed_line = new TraversedLine(actual_stmt.getSootMethod().getDeclaringClass(), actual_stmt.getSootMethod().method(), unit_from_stmt.getJavaSourceStartLineNumber());
        List<TraversedLine> traversedLine_list = new ArrayList<>();
        traversedLine_list.add(actual_traversed_line);
        statement.setTraversedLine(traversedLine_list);
        return statement;
    }

    public List<Statement> traverse(SootMethod sm, List<SootMethod> traversed, List<TraversedLine> traversedLine, Statement.Type type, int level) {
        Body body = retrieveActiveBodySafely(sm);
        if(traversed.contains(sm) || level > 5 || (!sm.getDeclaringClass().isApplicationClass()) || (body == null)) {
            return new ArrayList<>();
        }
        level++;
        traversed.add(sm);

        List<Statement> res = new ArrayList<>();

        for(Unit u: body.getUnits()) {
            if(u instanceof IdentityStmt) {
                continue;
            }

            // if the unit is is an AssignStmt, create a statement with your traversedLine list
            if(type.equals(Statement.Type.SOURCE) && u instanceof AssignStmt) {
                AssignStmt assignStmt = (AssignStmt) u;
                Statement actual_statement = createStatement(sm, u, type);

                actual_statement.setTraversedLine(traversedLine);

                res.add(actual_statement);

                // If it is an invokeExp, there is a new call to the method, so call a new traverse
                // passing your currently traversed line list
                if(assignStmt.containsInvokeExpr()) {

                    SootMethod invoked_method = assignStmt.getInvokeExpr().getMethod();
                    List<TraversedLine> actual_traversedLine = new ArrayList<>();

                    // add all elements from the current traverse line list
                    actual_traversedLine.addAll(traversedLine);

                    TraversedLine traversedLine_from_invoked_method = new TraversedLine(invoked_method.getDeclaringClass(), invoked_method.method(), assignStmt.getJavaSourceStartLineNumber());

                    // add an element from the current statement
                    actual_traversedLine.add(traversedLine_from_invoked_method);

                    //call traverse passing currently travesed line list
                    res.addAll(traverse(assignStmt.getInvokeExpr().getMethod(), traversed, actual_traversedLine, type, level));
                }
            }
            // if the unit is is a stmt and SINK, create a statement with your traversedLine list
            else if(type.equals(Statement.Type.SINK) && u.getUseBoxes().size() > 0) {
                Statement statement = createStatement(sm, u, type);

                statement.setTraversedLine(traversedLine);

                res.add(statement);

                if(u instanceof Stmt) {
                    Stmt stmt_from_unit = (Stmt) u;

                    // If it is an invokeExp, there is a new call to the method, so call a new traverse
                    // passing your currently traversed line list
                    if(stmt_from_unit.containsInvokeExpr()) {
                        SootMethod method_from_invoked_exp = stmt_from_unit.getInvokeExpr().getMethod();
                        List<TraversedLine> actual_traversedLine =new ArrayList<>();
                        // add all elements from the current traverse line list
                        actual_traversedLine.addAll(traversedLine);

                        TraversedLine aux_l = new TraversedLine(method_from_invoked_exp.getDeclaringClass(), method_from_invoked_exp.method(), stmt_from_unit.getJavaSourceStartLineNumber());
                        // add an element from the current statement
                        actual_traversedLine.add(aux_l);

                        //call traverse passing currently travesed line list
                        res.addAll(traverse(stmt_from_unit.getInvokeExpr().getMethod(), traversed, actual_traversedLine, type, level));
                    }
                }
            }
            // if the unit is an Invoke Stmt, create a statement with your traversedLine list
            else if(u instanceof InvokeStmt) {
                InvokeStmt invokeStmt = (InvokeStmt) u;
                SootMethod actual_method = invokeStmt.getInvokeExpr().getMethod();
                List<TraversedLine> traversedLine_list =new ArrayList<>();
                // add all elements from the current traverse line list
                traversedLine_list.addAll(traversedLine);

                TraversedLine traversedLine_from_invokeStmt = new TraversedLine(actual_method.getDeclaringClass(), actual_method.method(), invokeStmt.getJavaSourceStartLineNumber());

                // add an element from the current statement
                traversedLine_list.add(traversedLine_from_invokeStmt);

                //call traverse passing currently travesed line list
                res.addAll(traverse(invokeStmt.getInvokeExpr().getMethod(), traversed, traversedLine_list, type, level));
            }
        }
        return res;
    }

    public Statement createStatement(SootMethod sm, Unit u, Statement.Type type) {
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
    public Body retrieveActiveBodySafely(SootMethod method) {
        try {
            return method.retrieveActiveBody();
        } catch (RuntimeException e) {
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

    public void setRecursiveMode(boolean value) {
        this.recursive = value;
    }

    public void setOmitExceptingUnitEdges(int value) {
        this.omitExceptingUnitEdges = value;
    }

    public boolean isSourceStatement(Unit u) {
        return sourceStatements.stream().anyMatch(s -> s.getUnit().equals(u));
    }

    public boolean isSinkStatement(Unit u) {
        return sinkStatements.stream().anyMatch(s -> s.getUnit().equals(u));
    }

    public Set<SootMethod> getEntryMethods() {
        return entryMethods;
    }

    /**
     * Configures the entry points for the application based on the provided entry method names and statements.
     *
     * @param entryMethodNames a list of method names to be considered as entry points
     * @return a set of SootMethods representing the entry points
     */
    public Set<SootMethod> configureEntryPoints(List<String> entryMethodNames) throws NoSuchMethodException {
        Set<SootMethod> entryPoints = new HashSet<>();
        addMethodsToEntryPoints(entryPoints, entryMethodNames);
        return entryPoints;
    }

    /**
     * Auxiliary method to extract the SootClass from a method signature.
     * @param fullMethodSignature the full method signature
     * @return the SootClass extracted from the method signature
     */
    private SootClass extractSootClassFromMethodSignature(String fullMethodSignature) {
        int lastColonIndex = fullMethodSignature.lastIndexOf(':');
        if (lastColonIndex == -1) {
            throw new RuntimeException("Method signature does not contain a colon: " + fullMethodSignature);
        }

        String className = fullMethodSignature.substring(1, lastColonIndex).trim();
        Scene.v().loadClass(className, SootClass.BODIES);
        return Scene.v().getSootClass(className);
    }

    /**
     * Auxiliary method to extract the method name from a method signature.
     * @param fullMethodSignature the full method signature
     * @return the method name extracted from the method signature
     */
    private String extractMethodName(String fullMethodSignature) {
        int lastColonIndex = fullMethodSignature.lastIndexOf(':');
        if (lastColonIndex != -1) {
            String methodSignature = fullMethodSignature.substring(lastColonIndex + 1).trim();
            if (methodSignature.endsWith(">")) {
                methodSignature = methodSignature.substring(0, methodSignature.length() - 1);
            }
            return methodSignature;
        } else {
            return "";
        }
    }

    /**
     * Adds methods to the entry points set based on the provided method names and SootClass.
     *
     * @param entryPoints a set to which the methods will be added
     * @param methodSignatures a list of method names to be added as entry points
     */
    private void addMethodsToEntryPoints(Set<SootMethod> entryPoints, List<String> methodSignatures) throws NoSuchMethodException {
        for (String methodSignature : methodSignatures) {
            String methodName;
            try {
                methodName = extractMethodName(methodSignature);
            } catch (RuntimeException e) {
                throw new NoSuchMethodException("Method not found in method signature: " + methodSignature);
            }

            SootClass sootClass;
            try {
                sootClass = extractSootClassFromMethodSignature(methodSignature);
            } catch (RuntimeException e) {
                throw new NoSuchMethodException("Class not found in method signature: " + methodSignature);
            }

            try {
                SootMethod sootMethod = sootClass.getMethod(methodName);
                entryPoints.add(sootMethod);
            } catch (RuntimeException e) {
                throw new NoSuchMethodException("Method not found: " + methodName + " in class " + sootClass.getName());
            }
        }
    }

}
