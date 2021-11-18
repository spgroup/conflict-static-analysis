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

    private Set<Conflict> conflicts;
    private PointsToAnalysis pointsToAnalysis;
    private List<SootMethod> traversedMethods;
    private AbstractMergeConflictDefinition definition;
    private FlowSet<DataFlowAbstraction> left;
    private FlowSet<DataFlowAbstraction> right;
    private List<TraversedLine> stacktraceList;

    private final Logger logger;

    public InterproceduralOverrideAssignment(AbstractMergeConflictDefinition definition) {
        this.definition = definition;

        this.conflicts = new HashSet<>();
        this.left = new ArraySparseSet<>();
        this.right = new ArraySparseSet<>();
        this.traversedMethods = new ArrayList<>();
        this.pointsToAnalysis = Scene.v().getPointsToAnalysis();
        this.stacktraceList = new ArrayList<>();

        this.logger = Logger.getLogger(
                InterproceduralOverrideAssignment.class.getName());
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

        Set<Conflict> conflictsFilter = filterConflicts(getConflicts());
        logger.log(Level.INFO, () -> String.format("%s", "CONFLICTS: " + conflictsFilter));

        long finalTime = System.currentTimeMillis();
        System.out.println("Runtime: " + ((finalTime - startTime) / 1000d) + "s");
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

    private Set<Conflict> filterConflicts(Set<Conflict> conflictsResults) {
        Set<Conflict> conflictsFilter = new HashSet<>();
        for (Conflict conflict : conflictsResults) {
            if (conflictsFilter.isEmpty()) {
                conflictsFilter.add(conflict);
            }
        }
        for (Conflict conflict : conflictsResults) {
            for (Conflict filter : conflictsFilter) {
                if (!conflict.getSourceTraversedLine().isEmpty() && !conflict.getSinkTraversedLine().isEmpty()) {
                    if ((!conflict.getSourceTraversedLine().get(0).equals(filter.getSourceTraversedLine().get(0)))
                            && (!conflict.getSinkTraversedLine().get(0).equals(filter.getSinkTraversedLine().get(0)))) {
                        conflictsFilter.add(conflict);
                    }
                }

            }
        }
        return conflictsFilter;
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
    private FlowSet<DataFlowAbstraction> traverse(FlowSet<DataFlowAbstraction> in, SootMethod sootMethod,
                                                  Statement.Type flowChangeTag) {

        if (this.traversedMethods.contains(sootMethod) || this.traversedMethods.size() > 2 || sootMethod.isPhantom()) {
            return in;
        }

        System.out.println(sootMethod + " - " + this.traversedMethods.size());
        this.traversedMethods.add(sootMethod);

        Body body = definition.retrieveActiveBodySafely(sootMethod);

        if (body != null) {
            handleConstructor(in, sootMethod, flowChangeTag);
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

        this.traversedMethods.remove(sootMethod);
        return in;
    }

    private void handleConstructor(FlowSet<DataFlowAbstraction> in, SootMethod sootMethod,
                                   Statement.Type flowChangeTag) {
        if (sootMethod.isConstructor()) {
            Chain<SootField> sootFieldsInClass = sootMethod.getDeclaringClass().getFields();

            sootFieldsInClass.forEach(sootField -> {
                trasformFieldsIntoStatements(in, sootMethod, flowChangeTag, sootField);
            });
        }
    }

    private void trasformFieldsIntoStatements(FlowSet<DataFlowAbstraction> in, SootMethod sootMethod, Statement.Type flowChangeTag, SootField sootField) {
        String declaringClassShortName = sootField.getDeclaringClass().getShortName();
        String formatName =
                declaringClassShortName.substring(0, 1).toLowerCase() + declaringClassShortName.substring(1);

        JimpleLocal base = new JimpleLocal(formatName, RefType.v(sootField.getDeclaringClass()));
        SootFieldRef fieldRef = Scene.v().makeFieldRef(sootField.getDeclaringClass(),
                sootField.getName(), sootField.getType(), sootField.isStatic());

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

    private FlowSet<DataFlowAbstraction> runAnalysisWithTaggedUnit(FlowSet<DataFlowAbstraction> in, SootMethod sootMethod,
                                                                   Statement.Type flowChangeTag, Unit unit) {
        return runAnalysis(in, sootMethod, flowChangeTag, unit, true);
    }

    private FlowSet<DataFlowAbstraction> runAnalysisWithBaseUnit(FlowSet<DataFlowAbstraction> in, SootMethod sootMethod,
                                                                 Statement.Type flowChangeTag, Unit unit) {
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
    private FlowSet<DataFlowAbstraction> runAnalysis(FlowSet<DataFlowAbstraction> in, SootMethod sootMethod, Statement.Type flowChangeTag,
                                                     Unit unit, boolean tagged) {
        /* Are there other possible cases? Yes, see follow links:
        https://soot-build.cs.uni-paderborn.de/public/origin/develop/soot/soot-develop/jdoc/soot/jimple/Stmt.html
        https://github.com/PAMunb/JimpleFramework/blob/d585caefa8d5f967bfdbeb877346e0ff316e0b5e/src/main/rascal/lang/jimple/core/Syntax.rsc#L77-L95
         */

        if (unit instanceof AssignStmt) {
            /* Does AssignStmt check contain objects, arrays or other types?
             Yes, AssignStmt handles assignments and they can be of any type as long as they follow the structure: variable = value
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

            - The variables of the force are marked as IN_BETWEEN so they do not enter the abstraction.

            - The goto instructions have the following format "if i0> = 1 goto label2;" in this case,
            they are treated as "IfStmt" and do not enter either the "if(unit instanceof AssignStmt)" nor the "else if(unit instanceof InvokeStmt)".
             */

            /* InvokeStmt involves builder?
              Yes. InvokeStmt also involves builders. What changes is the corresponding InvokeExpression.
              For builders, InvokeExpression is an instance of InvokeSpecial */

        } else if (unit instanceof InvokeStmt) {
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

    private FlowSet<DataFlowAbstraction> executeCallGraph(FlowSet<DataFlowAbstraction> in,
                                                          Statement.Type flowChangeTag, Unit unit) {
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

        if (flowSetUnion.isEmpty()) {
            return in;
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
        if (stmt.isLeftStatement()) {
            checkConflict(stmt, right);

        } else if (stmt.isRightStatement()) {
            checkConflict(stmt, left);

        } else if (stmt.isLefAndRightStatement()) {
            addConflict(stmt, stmt);
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
        rightOrLeftList.forEach(dataFlowAbstraction -> {
            try {
                if (containsValue(dataFlowAbstraction, valueBox.getValue())) {
                    rightOrLeftList.remove(dataFlowAbstraction);
                }
            } catch (ValueNotHandledException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean containsValue(DataFlowAbstraction dataFlowAbstraction, Value value) throws ValueNotHandledException {
        if (dataFlowAbstraction.getValue() instanceof InstanceFieldRef && value instanceof InstanceFieldRef) {
            return ((InstanceFieldRef) dataFlowAbstraction.getValue()).getFieldRef().getSignature().equals(((InstanceFieldRef) value).getFieldRef().getSignature());
        }
        if (dataFlowAbstraction.getValue() instanceof Local && value instanceof Local) {
            return dataFlowAbstraction.getValue().equals(value);
        }
        if (dataFlowAbstraction.getValue() instanceof ArrayRef && value instanceof ArrayRef) {
            return dataFlowAbstraction.getValue().equals(value);
        }
        if (dataFlowAbstraction.getValue() instanceof StaticFieldRef && value instanceof StaticFieldRef) {
            return ((StaticFieldRef) dataFlowAbstraction.getValue()).getFieldRef().getSignature().equals(((StaticFieldRef) value).getFieldRef().getSignature());
        }
        if (!dataFlowAbstraction.getValue().getClass().equals(value.getClass())) {
            return false;
        }

        throw new ValueNotHandledException("Value Not Handled");
    }

    private String getArrayRefName(ArrayRef arrayRef) {
        return arrayRef.getBaseBox().getValue().toString().concat("[" + arrayRef.getIndex().toString() + "]");
    }

    private Statement getStatementAssociatedWithUnit(SootMethod sootMethod, Unit u, Statement.Type flowChangeTag) {
        if (isLeftAndRightUnit(u) || isInLeftAndRightStatementFlow(flowChangeTag) || isBothUnitOrBothStatementFlow(u,
                flowChangeTag)) {
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
        return definition.getSinkStatements().stream().filter(s -> s.getUnit().equals(u)).
                findFirst().get();
    }

    private Statement findLeftStatement(Unit u) {
        return definition.getSourceStatements().stream().filter(s -> s.getUnit().equals(u)).
                findFirst().get();
    }

}
