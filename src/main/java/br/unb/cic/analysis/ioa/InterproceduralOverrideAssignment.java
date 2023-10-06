package br.unb.cic.analysis.ioa;

import br.unb.cic.analysis.AbstractAnalysis;
import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.OAConflictReport;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.analysis.model.TraversedLine;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.Chain;

import java.util.*;
import java.util.stream.Collectors;

public class InterproceduralOverrideAssignment extends SceneTransformer implements AbstractAnalysis {
    private final int depthLimit;
    private final AbstractMergeConflictDefinition definition;
    private OAConflictReport oaConflictReport;
    private TraversedMethodsWrapper<SootMethod> traversedMethodsWrapper;
    private List<TraversedLine> stacktraceList;

    public InterproceduralOverrideAssignment(AbstractMergeConflictDefinition definition) {
        this.definition = definition;
        this.depthLimit = 5;

        initDefaultFields();
    }

    public InterproceduralOverrideAssignment(AbstractMergeConflictDefinition definition, int depthLimit) {
        this.definition = definition;
        this.depthLimit = depthLimit;

        initDefaultFields();
    }

    private void initDefaultFields() {
        this.oaConflictReport = new OAConflictReport();
        this.traversedMethodsWrapper = new TraversedMethodsWrapper<>();
        this.stacktraceList = new ArrayList<>();
    }

    @Override
    public void clear() {
        oaConflictReport.clear();
    }

    @Override
    public Set<Conflict> getConflicts() {
        return oaConflictReport.getConflicts();
    }

    @Override
    protected void internalTransform(String s, Map<String, String> map) {
        long startTime = System.currentTimeMillis();

        List<SootMethod> methods = Scene.v().getEntryPoints();
        methods.forEach(sootMethod -> traverse(new OverrideAssignmentAbstraction(), sootMethod, Statement.Type.IN_BETWEEN));

        long finalTime = System.currentTimeMillis();
        System.out.println("Runtime: " + ((finalTime - startTime) / 1000d) + "s");

        oaConflictReport.report();
    }

    public void configureEntryPoints() {
        List<SootMethod> entryPoints = new ArrayList<>();

        definition.loadSourceStatements();
        definition.loadSinkStatements();

        SootMethod sm = getTraversedMethod();

        if (sm != null) {
            entryPoints.add(sm);
        } else {
            definition.getSourceStatements().forEach(s -> {
                if (!entryPoints.contains(s.getSootMethod())) {
                    entryPoints.add(s.getSootMethod());
                }
            });
        }
        Scene.v().setEntryPoints(entryPoints);
    }

    private SootMethod getTraversedMethod() {
        try {
            SootClass sootClass = definition.getSourceStatements().get(0).getSootClass();
            return sootClass.getMethodByName("callRealisticRun");
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * This method captures the safe body of the current method and delegates the analysis function to units of (LEFT or RIGHT) or units of BASE.
     *
     * @param sootMethod    Current method to be traversed;
     * @param flowChangeTag This parameter identifies whether the unit under analysis is in the flow of any statement already marked.
     *                      Initially it receives the value IN_BETWEEN but changes if the call to the current method (sootMethod) has been marked as SOURCE or SINK.
     *                      The remaining statements of the current method that have no markup will be marked according to the flowChangeTag.
     * @return the result of applying the analysis considering the income abstraction (in) and the sootMethod
     */
    private OverrideAssignmentAbstraction traverse(OverrideAssignmentAbstraction in, SootMethod sootMethod, Statement.Type flowChangeTag) {

        if (shouldSkip(sootMethod)) {
            return in;
        }

        this.traversedMethodsWrapper.add(sootMethod);

        //System.out.println( sootMethod + " - " + this.traversedMethodsWrapper.size());
        Body body = definition.retrieveActiveBodySafely(sootMethod);

        if (body != null) {
            for (Unit unit : body.getUnits()) {
                TraversedLine traversedLine = new TraversedLine(sootMethod, unit.getJavaSourceStartLineNumber());
                Statement stmt = getStatementAssociatedWithUnit(sootMethod, unit, flowChangeTag);

                stacktraceList.add(traversedLine);
                in = runAnalysis(in, stmt);
                stacktraceList.remove(traversedLine);
            }
        }

        this.traversedMethodsWrapper.remove(sootMethod);
        return in;
    }

    private boolean shouldSkip(SootMethod sootMethod) {
        boolean hasRelativeBeenTraversed = this.traversedMethodsWrapper.hasRelativeBeenTraversed(sootMethod);
        boolean isSizeGreaterThanDepthLimit = this.traversedMethodsWrapper.size() >= this.depthLimit;
        boolean isPhantom = sootMethod.isPhantom();

        return hasRelativeBeenTraversed || isSizeGreaterThanDepthLimit || isPhantom;
    }

    private boolean isTagged(Statement.Type flowChangeTag, Unit unit) {
        return (isLeftUnit(unit) || isRightUnit(unit))
                || (isInLeftStatementFlow(flowChangeTag)
                || isInRightStatementFlow(flowChangeTag)
                || isInLeftAndRightStatementFlow(flowChangeTag));
    }


    private OverrideAssignmentAbstraction runAnalysis(OverrideAssignmentAbstraction in, Statement stmt) {
        /* Are there other possible cases? Yes, see follow links:
        https://soot-build.cs.uni-paderborn.de/public/origin/develop/soot/soot-develop/jdoc/soot/jimple/Stmt.html
        https://github.com/PAMunb/JimpleFramework/blob/d585caefa8d5f967bfdbeb877346e0ff316e0b5e/src/main/rascal/lang/jimple/core/Syntax.rsc#L77-L95
         */

        if (stmt.getUnit() instanceof AssignStmt) {
            /* Does AssignStmt check contain objects, arrays or other types?
             Yes, AssignStmt handles assignments, and they can be of any type as long as they follow the structure: variable = value
             */
            AssignStmt assignStmt = (AssignStmt) stmt.getUnit();

            if (assignStmt.containsInvokeExpr()) {
                return calculateMergedOverrideAssignment(in, stmt);
            }


            if (isTagged(stmt.getType(), stmt.getUnit())) {
                in = runAnalysisWithTaggedUnit(in, stmt);
            } else {
                in = runAnalysisWithBaseUnit(in, stmt);
            }

            /* Check case: x = foo() + bar()
            In this case, this condition will be executed for the call to the foo() method and then another call to the bar() method.
             */

            /* Check treatment in case 'for'
            - Jimple does not exist for. The command is done using the goto.
            - The variables of the force are marked as IN_BETWEEN, so they do not enter the abstraction.
            - The goto instructions have the following format "if i0> = 1 goto label2;" in this case,
            they are treated as "IfStmt" and do not enter either the "if(unit instanceof AssignStmt)" nor the "else if(unit instanceof InvokeStmt)".
             */

            /* InvokeStmt involves builder?
              Yes. InvokeStmt also involves builders. What changes is the corresponding InvokeExpression.
              For builders, InvokeExpression is an instance of InvokeSpecial */

        } else if (stmt.getUnit() instanceof InvokeStmt) {
            SootMethod sm = ((InvokeStmt) stmt.getUnit()).getInvokeExpr().getMethod();
            if (sm.isConstructor()) {
                handleConstructor(in, stmt);
            }
            return calculateMergedOverrideAssignment(in, stmt);
        }

        return in;
    }


    private OverrideAssignmentAbstraction runAnalysisWithTaggedUnit(OverrideAssignmentAbstraction in, Statement stmt) {
        stmt.setTraversedLine(new ArrayList<>(this.stacktraceList));
        gen(in, stmt);
        checkConflict(in, stmt);
        return in;
    }

    private OverrideAssignmentAbstraction runAnalysisWithBaseUnit(OverrideAssignmentAbstraction in, Statement stmt) {
        List<Statement> statementsToRemove = new ArrayList<>();
        List<List<Statement>> abstractions = in.getLists();

        abstractions.forEach(abstraction -> {
            addToRemovalListAndFindConflicts(abstraction, stmt, statementsToRemove, false);
            removeAll(in, statementsToRemove);
        });

        return in;
    }

    public void checkConflict(OverrideAssignmentAbstraction in, Statement stmt) {
        List<Statement> statementsToRemove = new ArrayList<>();
        if (stmt.isLefAndRightStatement()) {
            addConflict(stmt, stmt);
        } else if (stmt.isLeftStatement()) {
            addToRemovalListAndFindConflicts(in.getRightAbstraction(), stmt, statementsToRemove, true);
        } else if (stmt.isRightStatement()) {
            addToRemovalListAndFindConflicts(in.getLeftAbstraction(), stmt, statementsToRemove, true);
        }
        removeAll(in, statementsToRemove);
    }

    private void addToRemovalListAndFindConflicts(List<Statement> abstraction, Statement stmt, List<Statement> statementsToRemove, boolean addConflict) {
        abstraction.forEach(statement -> {
            if (isSameStateElement(statement, stmt)) {
                if (addConflict) {
                    addConflict(statement, stmt);
                }
                statementsToRemove.add(statement);
            }
        });
    }

    private void removeAll(OverrideAssignmentAbstraction in, List<Statement> statementsToRemove) {
        statementsToRemove.forEach(statement -> kill(in, statement));
    }

    private boolean isSameStateElement(Statement stmtInAbs, Statement stmtInFlow) {
        for (ValueBox defBoxInAbs : stmtInAbs.getUnit().getDefBoxes()) {
            for (ValueBox defBoxInFlow : stmtInFlow.getUnit().getDefBoxes()) {
                Value valueInAbs = defBoxInAbs.getValue();
                Value valueInFlow = defBoxInFlow.getValue();

                if (valueInAbs instanceof Local && valueInFlow instanceof Local) {
                    return isSameLocal(stmtInAbs, stmtInFlow, valueInAbs, valueInFlow);
                } else if (valueInAbs instanceof InstanceFieldRef && valueInFlow instanceof InstanceFieldRef) {
                    return isSameFieldRef(stmtInAbs, stmtInFlow, valueInAbs, valueInFlow);
                } else if (valueInAbs instanceof ArrayRef && valueInFlow instanceof ArrayRef) {
                    return isSameArrayRef(stmtInAbs, stmtInFlow, valueInAbs, valueInFlow);
                } else if (valueInAbs instanceof StaticFieldRef && valueInFlow instanceof StaticFieldRef) {
                    return isSameStaticFieldRef(valueInAbs, valueInFlow);
                }
            }
        }
        return false;
    }

    private boolean isSameLocal(Statement stmtInAbs, Statement stmtInFlow, Value valueInAbs, Value valueInFlow) {
        // Se as variaveis são locais, devem ser do mesmo metodo, se forem de metodos diferentes, não há interferencia.
        if (!stmtInAbs.getSootMethod().equals(stmtInFlow.getSootMethod())) {
            return false;
        }
        return valueInAbs.toString().equals(valueInFlow.toString());
    }

    private boolean isSameFieldRef(Statement stmtInAbs, Statement stmtInFlow, Value valueInAbs, Value valueInFlow) {
        InstanceFieldRef abstractFieldRef = (InstanceFieldRef) valueInAbs;
        InstanceFieldRef flowFieldRef = (InstanceFieldRef) valueInFlow;

        if (stmtInAbs.getPointTo() != null && stmtInFlow.getPointTo() == null) {
            getPointToFromBase(flowFieldRef.getBase(), stmtInFlow);
        }
        return stmtInAbs.getPointTo() != null
                && areFieldReferencesEqual(stmtInAbs, stmtInFlow, abstractFieldRef, flowFieldRef);
    }


    private static boolean areFieldReferencesEqual(Statement stmtInAbs, Statement stmtInFlow, InstanceFieldRef abstractFieldRef, InstanceFieldRef flowFieldRef) {
        boolean pointToIntersection = stmtInAbs.getPointTo().hasNonEmptyIntersection(stmtInFlow.getPointTo());
        boolean typesEqual = abstractFieldRef.getType().equals(flowFieldRef.getType());
        boolean fieldRefsEqual = abstractFieldRef.getFieldRef().equals(flowFieldRef.getFieldRef());

        return (pointToIntersection || typesEqual) && fieldRefsEqual;
    }


    private boolean isSameArrayRef(Statement stmtInAbs, Statement stmtInFlow, Value valueInAbs, Value valueInFlow) {
        if (stmtInAbs.getPointTo() != null) {
            if (stmtInFlow.getPointTo() == null) {
                getPointToFromBase(((ArrayRef) valueInFlow).getBase(), stmtInFlow);
            }
            return stmtInAbs.getPointTo().hasNonEmptyIntersection(stmtInFlow.getPointTo());
        }
        return false;
    }

    private boolean isSameStaticFieldRef(Value valueInAbs, Value valueInFlow) {
        return ((StaticFieldRef) valueInAbs).getFieldRef().getSignature().equals(((StaticFieldRef) valueInFlow).getFieldRef().getSignature());
    }

    private void addConflict(Statement left, Statement right) {
        Conflict conflict = new Conflict(left, right);
        if (!this.oaConflictReport.contains(conflict)) {
            this.oaConflictReport.addConflict(conflict);

        }
    }


    private void gen(OverrideAssignmentAbstraction in, Statement stmt) {
        Value value = stmt.getUnit().getDefBoxes().get(0).getValue();
        if (value instanceof InstanceFieldRef) {
            getPointToFromBase(((InstanceFieldRef) value).getBase(), stmt);
        } else if (value instanceof ArrayRef) {
            getPointToFromBase(((ArrayRef) value).getBase(), stmt);
        } else if (value instanceof StaticFieldRef) {
            getPointToFromStaticField(((StaticFieldRef) value).getField(), stmt);
        }
        in.add(stmt);
    }

    private static void getPointToFromBase(Value value, Statement stmt) {
        PointsToAnalysis pointsToAnalysis = Scene.v().getPointsToAnalysis();
        PointsToSet points = pointsToAnalysis.reachingObjects((Local) value);
        stmt.setPointTo(points);
    }

    private static void getPointToFromStaticField(SootField fieldRef, Statement stmt) {
        PointsToAnalysis pointsToAnalysis = Scene.v().getPointsToAnalysis();
        PointsToSet points = pointsToAnalysis.reachingObjects(fieldRef);
        stmt.setPointTo(points);
    }

    private void kill(OverrideAssignmentAbstraction in, Statement stmt) {
        in.remove(stmt);
    }

    private void handleConstructor(OverrideAssignmentAbstraction in, Statement stmt) {
        Chain<SootField> sootFieldsInClass = stmt.getSootMethod().getDeclaringClass().getFields();
        // Attributes declared as final in Java can only have a single assignment, which means that their value cannot be changed after they are defined during their initialization.
        List<SootField> nonFinalFields = filterNonFinalFieldsInClass(sootFieldsInClass);
        nonFinalFields.forEach(sootField -> transformFieldsIntoStatements(in, stmt, sootField));
    }

    private List<SootField> filterNonFinalFieldsInClass(Chain<SootField> sootFieldsInClass) {
        List<SootField> nonFinalFields = new ArrayList<>();
        for (SootField field : sootFieldsInClass) {
            if (!field.isFinal()) {
                nonFinalFields.add(field);
            }
        }
        return nonFinalFields;
    }

    private void transformFieldsIntoStatements(OverrideAssignmentAbstraction in, Statement statement, SootField sootField) {
        String declaringClassShortName = sootField.getDeclaringClass().getShortName();
        JimpleLocal base = new JimpleLocal(declaringClassShortName, RefType.v(sootField.getDeclaringClass()));
        SootFieldRef fieldRef = Scene.v().makeFieldRef(sootField.getDeclaringClass(), sootField.getName(), sootField.getType(), sootField.isStatic());

        Value value = createFieldValueReference(base, fieldRef);
        Unit unit = new JAssignStmt(value, NullConstant.v());

        Statement stmt = getStatementAssociatedWithUnit(statement.getSootMethod(), unit, statement.getType());
        if (isTagged(stmt.getType(), stmt.getUnit())) {
            runAnalysisWithTaggedUnit(in, stmt);
        } else {
            runAnalysisWithBaseUnit(in, stmt);
        }
    }

    private OverrideAssignmentAbstraction calculateMergedOverrideAssignment(OverrideAssignmentAbstraction inputAbstraction, Statement currentStatement) {
        CallGraph callGraph = Scene.v().getCallGraph();
        Iterator<Edge> edges = callGraph.edgesOutOf(currentStatement.getUnit());

        List<OverrideAssignmentAbstraction> flowSetList = new ArrayList<>();

        while (edges.hasNext()) {
            Edge edge = edges.next();
            SootMethod targetMethod = edge.getTgt().method();

            try {
                OverrideAssignmentAbstraction clonedAbstraction = (OverrideAssignmentAbstraction) inputAbstraction.clone();
                OverrideAssignmentAbstraction traverseResult = traverse(clonedAbstraction, targetMethod, currentStatement.getType());
                flowSetList.add(traverseResult);
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex);
            }
        }

        if (flowSetList.isEmpty()) {
            return inputAbstraction;
        }

        OverrideAssignmentAbstraction newOverrideAssignmentAbstraction = new OverrideAssignmentAbstraction();
        flowSetList.forEach(newOverrideAssignmentAbstraction::union);

        return newOverrideAssignmentAbstraction;
    }

    private Value createFieldValueReference(JimpleLocal base, SootFieldRef fieldRef) {
        Value value;
        if (fieldRef.isStatic()) {
            value = Jimple.v().newStaticFieldRef(fieldRef);
        } else {
            value = Jimple.v().newInstanceFieldRef(base, fieldRef);
        }
        return value;
    }

    private Statement getStatementAssociatedWithUnit(SootMethod sootMethod, Unit u, Statement.Type flowChangeTag) {
        if (isLeftAndRightUnit(u) || isInLeftAndRightStatementFlow(flowChangeTag) || isBothUnitOrBothStatementFlow(u, flowChangeTag)) {
            return definition.createStatement(sootMethod, u, Statement.Type.SOURCE_SINK);
        } else if (isLeftUnit(u)) {
            return findLeftStatement(u);
        } else if (isRightUnit(u)) {
            return findRightStatement(u);
        } else if (isInLeftStatementFlow(flowChangeTag)) {
            return definition.createStatement(sootMethod, u, flowChangeTag);
        } else if (isInRightStatementFlow(flowChangeTag)) {
            return definition.createStatement(sootMethod, u, flowChangeTag);
        }
        return definition.createStatement(sootMethod, u, Statement.Type.IN_BETWEEN);
    }

    private boolean isBothUnitOrBothStatementFlow(Unit u, Statement.Type flowChangeTag) {
        return (isRightUnit(u) && isInLeftStatementFlow(flowChangeTag)) || (isLeftUnit(u) && isInRightStatementFlow(flowChangeTag));
    }

    private boolean isLeftUnit(Unit u) {
        return definition.getSourceStatements().stream().map(Statement::getUnit).collect(Collectors.toList()).contains(u);
    }

    private boolean isRightUnit(Unit u) {
        return definition.getSinkStatements().stream().map(Statement::getUnit).collect(Collectors.toList()).contains(u);
    }

    private boolean isInRightStatementFlow(Statement.Type flowChangeTag) {
        return flowChangeTag.equals(Statement.Type.SINK);
    }

    private boolean isInLeftStatementFlow(Statement.Type flowChangeTag) {
        return flowChangeTag.equals(Statement.Type.SOURCE);
    }

    private boolean isInLeftAndRightStatementFlow(Statement.Type flowChangeTag) {
        return flowChangeTag.equals(Statement.Type.SOURCE_SINK);
    }

    private boolean isLeftAndRightUnit(Unit u) {
        return isLeftUnit(u) && isRightUnit(u);
    }

    private Statement findRightStatement(Unit u) {
        return definition.getSinkStatements().stream().filter(s -> s.getUnit().equals(u)).findFirst().get();
    }

    private Statement findLeftStatement(Unit u) {
        return definition.getSourceStatements().stream().filter(s -> s.getUnit().equals(u)).findFirst().get();
    }

    public int getVisitedMethods(){
        return this.traversedMethodsWrapper.getVisitedMethods();
    }
}
