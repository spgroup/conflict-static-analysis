package br.unb.cic.analysis.oa;

import br.unb.cic.analysis.AbstractAnalysis;
import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.StatementsUtil;
import br.unb.cic.analysis.model.*;
import scala.collection.JavaConverters;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.Chain;

import java.util.*;
import java.util.stream.Collectors;

public abstract class OverrideAssignment extends SceneTransformer implements AbstractAnalysis {
    private int depthLimit;
    private final Boolean interprocedural;
    private OAConflictReport oaConflictReport;
    private TraversedMethodsWrapper<SootMethod> traversedMethodsWrapper;
    private List<TraversedLine> stacktraceList;
    private StatementsUtil statementsUtils;

    protected abstract void gen(OverrideAssignmentAbstraction in, Statement stmt);

    protected abstract boolean isSameStateElement(Statement stmtInAbs, Statement stmtInFlow);

    public OverrideAssignment(AbstractMergeConflictDefinition definition, int depthLimit, Boolean interprocedural, List<String> entrypoints) {
        this.depthLimit = depthLimit;
        this.interprocedural = interprocedural;
        this.statementsUtils = new StatementsUtil(definition, entrypoints);

        initDefaultFields();
    }

    public OverrideAssignment(AbstractMergeConflictDefinition definition, int depthLimit, Boolean interprocedural) {
        this(definition, depthLimit, interprocedural, new ArrayList<>());
    }

    public OverrideAssignment(AbstractMergeConflictDefinition definition) {
        this(definition, 5, true);
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

    public Set<Conflict> getFilteredConflicts() {
        return oaConflictReport.filterConflictsWithSameRoot(oaConflictReport.getConflicts());
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
        scala.collection.immutable.List<SootMethod> scalaList = this.statementsUtils.getEntryPoints();
        List<SootMethod> entryPoints = new ArrayList<>(JavaConverters.seqAsJavaList(scalaList));

        Scene.v().setEntryPoints(entryPoints);
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

        //System.out.println(sootMethod + " - " + this.traversedMethodsWrapper.size());
        Body body = this.statementsUtils.getDefinition().retrieveActiveBodySafely(sootMethod);

        if (body != null) {
            for (Unit unit : body.getUnits()) {
                TraversedLine traversedLine = new TraversedLine(sootMethod, unit.getJavaSourceStartLineNumber());
                Statement stmt = getStatementAssociatedWithUnit(sootMethod, unit, flowChangeTag);

                this.stacktraceList.add(traversedLine);
                in = runAnalysis(in, stmt);
                this.stacktraceList.remove(traversedLine);
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

            if (this.interprocedural && assignStmt.containsInvokeExpr()) {
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

        } else if (this.interprocedural && stmt.getUnit() instanceof InvokeStmt) {
            SootMethod sm = ((InvokeStmt) stmt.getUnit()).getInvokeExpr().getMethod();
            if (sm.isConstructor()) {
                handleConstructor(in, sm, stmt.getType());
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

    protected boolean isSameLocal(Statement stmtInAbs, Statement stmtInFlow, Value valueInAbs, Value valueInFlow) {
        // Se as variaveis são locais, devem ser do mesmo metodo, se forem de metodos diferentes, não há interferencia.
        if (!stmtInAbs.getSootMethod().equals(stmtInFlow.getSootMethod())) {
            return false;
        }
        return valueInAbs.toString().equals(valueInFlow.toString());
    }

    protected boolean isSameFieldRef(Statement stmtInAbs, Statement stmtInFlow, Value valueInAbs, Value valueInFlow) {
        InstanceFieldRef abstractFieldRef = (InstanceFieldRef) valueInAbs;
        InstanceFieldRef flowFieldRef = (InstanceFieldRef) valueInFlow;

        if (stmtInAbs.getPointsTo() != null && stmtInFlow.getPointsTo() == null) {
            getPointToFromBase(flowFieldRef.getBase(), stmtInFlow);
        }
        return stmtInAbs.getPointsTo() != null
                && areFieldReferencesEqual(stmtInAbs, stmtInFlow, abstractFieldRef, flowFieldRef);
    }

    private static boolean areFieldReferencesEqual(Statement stmtInAbs, Statement stmtInFlow, InstanceFieldRef abstractFieldRef, InstanceFieldRef flowFieldRef) {
        boolean pointToIntersection = stmtInAbs.getPointsTo().hasNonEmptyIntersection(stmtInFlow.getPointsTo());
        boolean typesEqual = abstractFieldRef.getType().equals(flowFieldRef.getType());
        boolean fieldRefsEqual = abstractFieldRef.getFieldRef().equals(flowFieldRef.getFieldRef());

        return (pointToIntersection || typesEqual) && fieldRefsEqual;
    }

    protected boolean isSameArrayRef(Statement stmtInAbs, Statement stmtInFlow, Value valueInAbs, Value valueInFlow) {
        if (stmtInAbs.getPointsTo() != null) {
            if (stmtInFlow.getPointsTo() == null) {
                getPointToFromBase(((ArrayRef) valueInFlow).getBase(), stmtInFlow);
            }
            if (stmtInAbs.getPointsTo().isEmpty() && stmtInFlow.getPointsTo().isEmpty()) {
                return valueInAbs.toString().equals(valueInFlow.toString());
            }
            return stmtInAbs.getPointsTo().hasNonEmptyIntersection(stmtInFlow.getPointsTo());
        }
        return false;
    }

    protected boolean isSameStaticFieldRef(Value valueInAbs, Value valueInFlow) {
        return ((StaticFieldRef) valueInAbs).getFieldRef().getSignature().equals(((StaticFieldRef) valueInFlow).getFieldRef().getSignature());
    }

    private void addConflict(Statement left, Statement right) {
        Conflict conflict = new OAConflict(left, right, this.interprocedural);
        if (!this.oaConflictReport.contains(conflict)) {
            this.oaConflictReport.addConflict(conflict);

        }
    }

    protected static void getPointToFromBase(Value value, Statement stmt) {
        PointsToAnalysis pointsToAnalysis = Scene.v().getPointsToAnalysis();
        PointsToSet points = pointsToAnalysis.reachingObjects((Local) value);
        stmt.setPointsTo(points);
    }

    protected static void getPointToFromStaticField(SootField fieldRef, Statement stmt) {
        PointsToAnalysis pointsToAnalysis = Scene.v().getPointsToAnalysis();
        PointsToSet points = pointsToAnalysis.reachingObjects(fieldRef);
        stmt.setPointsTo(points);
    }

    private void kill(OverrideAssignmentAbstraction in, Statement stmt) {
        in.remove(stmt);
    }

    private void handleConstructor(OverrideAssignmentAbstraction in, SootMethod sm, Statement.Type type) {
        Chain<SootField> sootFieldsInClass = sm.getDeclaringClass().getFields();
        //Chain<SootField> sootFieldsInClass = stmt.getSootMethod().getDeclaringClass().getFields();
        // Attributes declared as final in Java can only have a single assignment, which means that their value cannot be changed after they are defined during their initialization.
        List<SootField> nonFinalFields = filterNonFinalFieldsInClass(sootFieldsInClass);
        nonFinalFields.forEach(sootField -> transformFieldsIntoStatements(in, sm, type, sootField));
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

    private void transformFieldsIntoStatements(OverrideAssignmentAbstraction in, SootMethod sm, Statement.Type type, SootField sootField) {
        String declaringClassShortName = sootField.getDeclaringClass().getShortName();
        JimpleLocal base = new JimpleLocal(declaringClassShortName, RefType.v(sootField.getDeclaringClass()));
        SootFieldRef fieldRef = Scene.v().makeFieldRef(sootField.getDeclaringClass(), sootField.getName(), sootField.getType(), sootField.isStatic());

        Value value = createFieldValueReference(base, fieldRef);
        Unit unit = new JAssignStmt(value, NullConstant.v());

        Statement stmt = getStatementAssociatedWithUnit(sm, unit, type);
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
            return this.statementsUtils.getDefinition().createStatement(sootMethod, u, Statement.Type.SOURCE_SINK);
        } else if (isLeftUnit(u)) {
            return findLeftStatement(u);
        } else if (isRightUnit(u)) {
            return findRightStatement(u);
        } else if (isInLeftStatementFlow(flowChangeTag)) {
            return this.statementsUtils.getDefinition().createStatement(sootMethod, u, flowChangeTag);
        } else if (isInRightStatementFlow(flowChangeTag)) {
            return this.statementsUtils.getDefinition().createStatement(sootMethod, u, flowChangeTag);
        }
        return this.statementsUtils.getDefinition().createStatement(sootMethod, u, Statement.Type.IN_BETWEEN);
    }

    private boolean isBothUnitOrBothStatementFlow(Unit u, Statement.Type flowChangeTag) {
        return (isRightUnit(u) && isInLeftStatementFlow(flowChangeTag)) || (isLeftUnit(u) && isInRightStatementFlow(flowChangeTag));
    }

    private boolean isLeftUnit(Unit u) {
        return this.statementsUtils.getDefinition().getSourceStatements().stream().map(Statement::getUnit).collect(Collectors.toList()).contains(u);
    }

    private boolean isRightUnit(Unit u) {
        return this.statementsUtils.getDefinition().getSinkStatements().stream().map(Statement::getUnit).collect(Collectors.toList()).contains(u);
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
        return this.statementsUtils.getDefinition().getSinkStatements().stream().filter(s -> s.getUnit().equals(u)).findFirst().get();
    }

    private Statement findLeftStatement(Unit u) {
        return this.statementsUtils.getDefinition().getSourceStatements().stream().filter(s -> s.getUnit().equals(u)).findFirst().get();
    }

    public int getVisitedMethodsCount() {
        return this.traversedMethodsWrapper.getVisitedMethodsCount();
    }

    public int getDepthLimit() {
        return this.depthLimit;
    }

    public void setDepthLimit(int depthLimit) {
        this.depthLimit = depthLimit;
    }
}
