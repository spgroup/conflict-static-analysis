package br.unb.cic.analysis.ioa;

import br.unb.cic.analysis.AbstractAnalysis;
import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.df.DataFlowAbstraction;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.analysis.model.TraversedLine;
import br.unb.cic.exceptions.ValueNotHandledException;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.util.Chain;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

// TODO Add treatment of if, loops ... (ForwardFlowAnalysis)
// TODO Do not add anything when assignments are equal.
public class InterproceduralOverrideAssignment extends SceneTransformer implements AbstractAnalysis {

    private final int depthLimit;
    private final AbstractMergeConflictDefinition definition;
    private Set<Conflict> conflicts;
    private TraversedMethodsWrapper<SootMethod> traversedMethodsWrapper;
    private int visitedMethods = 0;
    private FlowSet<DataFlowAbstraction> left;
    private FlowSet<DataFlowAbstraction> right;
    private List<TraversedLine> stacktraceList;
    private Logger logger;

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
        this.conflicts = new HashSet<>();
        this.left = new ArraySparseSet<>();
        this.right = new ArraySparseSet<>();
        this.traversedMethodsWrapper = new TraversedMethodsWrapper<>();
        this.stacktraceList = new ArrayList<>();
        this.logger = Logger.getLogger(InterproceduralOverrideAssignment.class.getName());
    }

    @Override
    public void clear() {
        conflicts.clear();
    }

    @Override
    public Set<Conflict> getConflicts() {
        return conflicts;
    }

    @Override
    protected void internalTransform(String s, Map<String, String> map) {
        long startTime = System.currentTimeMillis();

        List<SootMethod> methods = Scene.v().getEntryPoints();
        methods.forEach(sootMethod -> traverse(new ArraySparseSet<>(), sootMethod, Statement.Type.IN_BETWEEN));
        long finalTime = System.currentTimeMillis();
        System.out.println("Runtime: " + ((finalTime - startTime) / 1000d) + "s");
        System.out.println(String.format("%s", "CONFLICTS: " + filterConflictsWithSameRoot(getConflicts())));
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

    private Set<Conflict> filterConflictsWithSameRoot(Set<Conflict> conflictsResults) {
        Set<Conflict> conflictsFilter = new HashSet<>();

        for (Conflict conflict : conflictsResults) {

            if (!hasConflictWithSameSourceAndSinkRootTraversedLine(conflictsFilter, conflict) && !conflict.getSourceClassName().contains("java.lang.Integer")) {
                conflictsFilter.add(conflict);
            }
        }
        return conflictsFilter;
    }

    private boolean hasConflictWithSameSourceAndSinkRootTraversedLine(Set<Conflict> conflictsFilter, Conflict conflict) {
        for (Conflict c : conflictsFilter) {
            if (c.conflictsHaveSameSourceAndSinkRootTraversedLine(conflict)) {
                return true;
            }
        }
        return false;
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
    private FlowSet<DataFlowAbstraction> traverse(FlowSet<DataFlowAbstraction> in, SootMethod sootMethod, Statement.Type flowChangeTag) {

        if (shouldSkip(sootMethod)) {
            return in;
        }

        this.visitedMethods++;

        this.traversedMethodsWrapper.add(sootMethod);

        //System.out.println( sootMethod + " - " + this.traversedMethodsWrapper.size());
        Body body = definition.retrieveActiveBodySafely(sootMethod);

        if (body != null) {
            for (Unit unit : body.getUnits()) {
                TraversedLine traversedLine = new TraversedLine(sootMethod, unit.getJavaSourceStartLineNumber());

                if (isTagged(flowChangeTag, unit)) {
                    addStackTrace(traversedLine);
                    in = runAnalysisWithTaggedUnit(in, sootMethod, flowChangeTag, unit);
                } else {
                    in = runAnalysisWithBaseUnit(in, sootMethod, flowChangeTag, unit);
                }
                removeStackTrace(traversedLine);
            }
        }

        this.traversedMethodsWrapper.remove(sootMethod);
        return in;
    }

    private boolean shouldSkip(SootMethod sootMethod) {
        boolean hasRelativeBeenTraversed = this.traversedMethodsWrapper.hasRelativeBeenTraversed(sootMethod);
        boolean isSizeGreaterThanDepthLimit = this.traversedMethodsWrapper.size() > this.depthLimit;
        boolean isPhantom = sootMethod.isPhantom();

        return hasRelativeBeenTraversed || isSizeGreaterThanDepthLimit || isPhantom;
    }

    private void handleConstructor(FlowSet<DataFlowAbstraction> in, SootMethod sootMethod, Statement.Type flowChangeTag) {
        Chain<SootField> sootFieldsInClass = sootMethod.getDeclaringClass().getFields();
        // Attributes declared as final in Java can only have a single assignment, which means that their value cannot be changed after they are defined during their initialization.
        List<SootField> nonFinalFields = filterNonFinalFieldsInClass(sootFieldsInClass);
        nonFinalFields.forEach(sootField -> transformFieldsIntoStatements(in, sootMethod, flowChangeTag, sootField));
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

    private void transformFieldsIntoStatements(FlowSet<DataFlowAbstraction> in, SootMethod sootMethod, Statement.Type flowChangeTag, SootField sootField) {
        String declaringClassShortName = sootField.getDeclaringClass().getShortName();
        JimpleLocal base = new JimpleLocal(declaringClassShortName, RefType.v(sootField.getDeclaringClass()));
        SootFieldRef fieldRef = Scene.v().makeFieldRef(sootField.getDeclaringClass(), sootField.getName(), sootField.getType(), sootField.isStatic());

        Value value = getValue(base, fieldRef);
        Unit unit = new JAssignStmt(value, NullConstant.v());
        createAndAddStmt(in, sootMethod, flowChangeTag, unit);
    }

    private Value getValue(JimpleLocal base, SootFieldRef fieldRef) {
        Value value;
        if (fieldRef.isStatic()) {
            value = Jimple.v().newStaticFieldRef(fieldRef);
        } else {
            value = Jimple.v().newInstanceFieldRef(base, fieldRef);
        }
        return value;
    }

    private FlowSet<DataFlowAbstraction> runAnalysisWithTaggedUnit(FlowSet<DataFlowAbstraction> in, SootMethod sootMethod, Statement.Type flowChangeTag, Unit unit) {
        return runAnalysis(in, sootMethod, flowChangeTag, unit, true);
    }

    private FlowSet<DataFlowAbstraction> runAnalysisWithBaseUnit(FlowSet<DataFlowAbstraction> in, SootMethod sootMethod, Statement.Type flowChangeTag, Unit unit) {
        return runAnalysis(in, sootMethod, flowChangeTag, unit, false);
    }

    /**
     * This method analyzes a method and adds or removes items from the list of possible conflicts.
     *
     * @param sootMethod    Current method to be traversed;
     * @param flowChangeTag This parameter identifies whether the unit under analysis is in the flow of any statement already marked.
     *                      Initially it receives the value IN_BETWEEN but changes if the call to the current method (sootMethod) has been marked as SOURCE or SINK.
     *                      The remaining statements of the current method that have no markup will be marked according to the flowChangeTag.
     * @param tagged        Identifies whether the unit to be analyzed has been tagged. If false the unit is base, if true the unit is left or right.
     */
    private FlowSet<DataFlowAbstraction> runAnalysis(FlowSet<DataFlowAbstraction> in, SootMethod sootMethod, Statement.Type flowChangeTag, Unit unit, boolean tagged) {
        /* Are there other possible cases? Yes, see follow links:
        https://soot-build.cs.uni-paderborn.de/public/origin/develop/soot/soot-develop/jdoc/soot/jimple/Stmt.html
        https://github.com/PAMunb/JimpleFramework/blob/d585caefa8d5f967bfdbeb877346e0ff316e0b5e/src/main/rascal/lang/jimple/core/Syntax.rsc#L77-L95
         */

        if (unit instanceof AssignStmt) {
            /* Does AssignStmt check contain objects, arrays or other types?
             Yes, AssignStmt handles assignments, and they can be of any type as long as they follow the structure: variable = value
             */
            AssignStmt assignStmt = (AssignStmt) unit;

            if (assignStmt.containsInvokeExpr()) {
                return executeCallGraph(in, flowChangeTag, unit);
            }

            separeteAbstraction(in);
            if (tagged) {
                createAndAddStmt(in, sootMethod, flowChangeTag, unit);
            } else {
                kill(in, unit);
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

        } else if (unit instanceof InvokeStmt) {
            SootMethod sm = ((InvokeStmt) unit).getInvokeExpr().getMethod();
            Statement statement = getStatementAssociatedWithUnit(sm, unit, flowChangeTag);
            if (tagged && sm.isConstructor()) {
                handleConstructor(in, sm, statement.getType());
            }
            return executeCallGraph(in, flowChangeTag, unit);
        }

        return in;
    }

    private void createAndAddStmt(FlowSet<DataFlowAbstraction> in, SootMethod sootMethod, Statement.Type flowChangeTag, Unit unit) {
        Statement stmt = getStatementAssociatedWithUnit(sootMethod, unit, flowChangeTag);
        stmt.setTraversedLine(new ArrayList<>(this.stacktraceList));
        gen(in, stmt);
    }

    private void separeteAbstraction(FlowSet<DataFlowAbstraction> in) {
        in.forEach(item -> {
            if (item.getStmt().isLefAndRightStatement()) {
                left.add(item);
                right.add(item);
            } else if (item.getStmt().isLeftStatement()) {
                left.add(item);
            } else if (item.getStmt().isRightStatement()) {
                right.add(item);
            }
        });
    }

    private FlowSet<DataFlowAbstraction> executeCallGraph(FlowSet<DataFlowAbstraction> in, Statement.Type flowChangeTag, Unit unit) {
        CallGraph callGraph = Scene.v().getCallGraph();
        Iterator<Edge> edges = callGraph.edgesOutOf(unit);

        List<FlowSet<DataFlowAbstraction>> flowSetList = new ArrayList<>();

        while (edges.hasNext()) {
            Edge e = edges.next();
            SootMethod method = e.getTgt().method();

            Statement stmt = getStatementAssociatedWithUnit(method, unit, flowChangeTag);

            FlowSet<DataFlowAbstraction> traverseResult = traverse(in.clone(), method, stmt.getType());
            flowSetList.add(traverseResult);
        }

        FlowSet<DataFlowAbstraction> flowSetUnion = new ArraySparseSet<>();
        for (FlowSet<DataFlowAbstraction> flowSet : flowSetList) {
            flowSetUnion.union(flowSet);
        }

        return flowSetUnion;
    }

    private boolean isTagged(Statement.Type flowChangeTag, Unit unit) {
        return (isLeftUnit(unit) || isRightUnit(unit)) || (isInLeftStatementFlow(flowChangeTag) || isInRightStatementFlow(flowChangeTag) || isInLeftAndRightStatementFlow(flowChangeTag));
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

    // TODO add depth to InstanceFieldRef and StaticFieldRef...
    // TODO rename Statement. (UnitWithExtraInformations)
    private void gen(FlowSet<DataFlowAbstraction> in, Statement stmt) {
        if (stmt.isLefAndRightStatement()) {
            addConflict(stmt, stmt);
        } else if (stmt.isLeftStatement()) {
            checkConflict(stmt, right);
        } else if (stmt.isRightStatement()) {
            checkConflict(stmt, left);
        }
        addStmtToList(stmt, in);
    }

    private void addStmtToList(Statement stmt, FlowSet<DataFlowAbstraction> rightOrLeftList) {
        stmt.getUnit().getDefBoxes().forEach(valueBox -> rightOrLeftList.add(new DataFlowAbstraction(valueBox.getValue(), stmt)));
    }

    /*
     * Checks if there is a conflict and if so adds it to the conflict list.
     */
    private void checkConflict(Statement stmt, FlowSet<DataFlowAbstraction> rightOrLeftList) {
        rightOrLeftList.forEach(dataFlowAbstraction -> stmt.getUnit().getDefBoxes().forEach(valueBox -> {
            try {
                if (containsValue(dataFlowAbstraction, valueBox.getValue())) {
                    addConflict(stmt, dataFlowAbstraction.getStmt());
                    rightOrLeftList.remove(dataFlowAbstraction);
                }
            } catch (ValueNotHandledException e) {
                assert false;
                e.printStackTrace();
            }
        }));
    }

    private void addConflict(Statement left, Statement right) {
        Conflict conflict = new Conflict(left, right);
        if (this.conflicts.contains(conflict)) {
            return;
        }
        this.conflicts.add(conflict);

    }

    private void kill(FlowSet<DataFlowAbstraction> in, Unit unit) {
        unit.getDefBoxes().forEach(valueBox -> removeAll(valueBox, in));
        unit.getDefBoxes().forEach(valueBox -> removeAll(valueBox, left));
        unit.getDefBoxes().forEach(valueBox -> removeAll(valueBox, right));
    }

    private void removeAll(ValueBox valueBox, FlowSet<DataFlowAbstraction> rightOrLeftList) {
        FlowSet<DataFlowAbstraction> itemsToRemoved = new ArraySparseSet<>();
        rightOrLeftList.forEach(dataFlowAbstraction -> {
            try {
                if (containsValue(dataFlowAbstraction, valueBox.getValue())) {
                    itemsToRemoved.add(dataFlowAbstraction);
                }
            } catch (ValueNotHandledException e) {
                e.printStackTrace();
            }
        });

        rightOrLeftList.difference(itemsToRemoved);
    }

    /**
     * This method checks for each type of variable analyzed if it exists within the abstraction.
     *
     * @param dataFlowAbstraction One of the items contained in the abstraction
     * @param value               Variable to be analyzed.
     * @return true if the variable exists in the abstraction and false if it does not.
     * @throws ValueNotHandledException Exception thrown when variable type is not cataloged.
     */
    private boolean containsValue(DataFlowAbstraction dataFlowAbstraction, Value value) throws ValueNotHandledException {

        if (dataFlowAbstraction.getValue() instanceof InstanceFieldRef && value instanceof InstanceFieldRef) {
            return compareInstanceFieldRef(dataFlowAbstraction, value);
        }
        if (dataFlowAbstraction.getValue() instanceof Local && value instanceof Local) {
            return dataFlowAbstraction.getValue().equals(value);
        }
        if (dataFlowAbstraction.getValue() instanceof ArrayRef && value instanceof ArrayRef) {
            return compareArrayRef(dataFlowAbstraction, value);
        }
        if (dataFlowAbstraction.getValue() instanceof StaticFieldRef && value instanceof StaticFieldRef) {
            StaticFieldRef fromDataFlowAbstraction = (StaticFieldRef) dataFlowAbstraction.getValue();
            StaticFieldRef fromValue = (StaticFieldRef) value;
            return compareSignature(fromDataFlowAbstraction.getFieldRef(), fromValue.getFieldRef());
        }
        if (!dataFlowAbstraction.getValue().getClass().equals(value.getClass())) {
            return false;
        }

        throw new ValueNotHandledException("Value Not Handled");
    }

    private boolean compareInstanceFieldRef(DataFlowAbstraction dataFlowAbstraction, Value value) {
        InstanceFieldRef fromDataFlowAbstraction = (InstanceFieldRef) dataFlowAbstraction.getValue();
        InstanceFieldRef fromValue = (InstanceFieldRef) value;
        return fromDataFlowAbstraction.toString().equals(fromValue.toString()) || (compareSignature(fromDataFlowAbstraction.getFieldRef(), fromValue.getFieldRef()) && comparePointsToHasNonEmptyIntersection((Local) fromDataFlowAbstraction.getBase(), (Local) fromValue.getBase()));
    }

    /**
     * Compare whether arrays have the same reference using pointsToAnalysis and ignoring the index
     */
    private boolean compareArrayRef(DataFlowAbstraction dataFlowAbstraction, Value value) {
        ArrayRef fromDataFlowAbstraction = (ArrayRef) dataFlowAbstraction.getValue();
        ArrayRef fromValue = (ArrayRef) value;
        return comparePointsToHasNonEmptyIntersection((Local) fromDataFlowAbstraction.getBase(), (Local) fromValue.getBase());
    }

    /**
     * This method compares the FieldRef (Ex: <Object: java.lang.Integer x>) signature textually.
     *
     * @param fromDataFlowAbstraction SootFieldRef coming from abstraction
     * @param fromValue               SootFieldRef coming from the analyzed variable
     * @return true if the signature is the same and false if it is different
     */
    private boolean compareSignature(SootFieldRef fromDataFlowAbstraction, SootFieldRef fromValue) {
        return fromDataFlowAbstraction.getSignature().equals(fromValue.getSignature());
    }

    /**
     * This method compares whether two objects can point to the same memory address using pointsToAnalysis.
     * This method uses only the InstanceFieldRef base. Ex: in an expression o.x, o is the base.
     *
     * @param fromDataFlowAbstraction InstanceFieldRef coming from abstraction
     * @param fromValue               InstanceFieldRef coming from the analyzed variable
     * @return true if an intersection exists
     */
    private boolean comparePointsToHasNonEmptyIntersection(Local fromDataFlowAbstraction, Local fromValue) {
        PointsToAnalysis pointsToAnalysis = Scene.v().getPointsToAnalysis();
        return pointsToAnalysis.reachingObjects(fromDataFlowAbstraction).hasNonEmptyIntersection(pointsToAnalysis.reachingObjects(fromValue));
    }

    private String getArrayRefName(ArrayRef arrayRef) {
        return arrayRef.getBaseBox().getValue().toString().concat("[" + arrayRef.getIndex().toString() + "]");
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

    private void addStackTrace(TraversedLine traversedLine) {
        this.stacktraceList.add(traversedLine);
    }

    private void removeStackTrace(TraversedLine traversedLine) {
        this.stacktraceList.remove(traversedLine);
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
        return this.visitedMethods;
    }
}