package br.unb.cic.analysis.ioa;

import br.unb.cic.analysis.AbstractAnalysis;
import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.df.DataFlowAbstraction;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.Statement;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

// TODO Add treatment of if, loops ... (ForwardFlowAnalysis)
// TODO Do not add anything when assignments are equal.
public class InterproceduralOverrideAssignment extends SceneTransformer implements AbstractAnalysis {

    private Set<Conflict> conflicts;
    private PointsToAnalysis pta;
    private AbstractMergeConflictDefinition definition;
    private FlowSet<DataFlowAbstraction> res;
    private Body body;

    private Logger logger;

    public InterproceduralOverrideAssignment(AbstractMergeConflictDefinition definition) {
        this.conflicts = new HashSet<>();
        this.definition = definition;
        this.res = new ArraySparseSet<>();

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

    private void configureEntryPoints() {
        List<SootMethod> entryPoints = new ArrayList<>();
        definition.getSourceStatements().forEach(s -> entryPoints.add(s.getSootMethod()));
        Scene.v().setEntryPoints(entryPoints);
    }

    @Override
    protected void internalTransform(String s, Map<String, String> map) {
        definition.loadSourceStatements();
        definition.loadSinkStatements();
        List<SootMethod> traversedMethods = new ArrayList<>();

        configureEntryPoints();

        List<SootMethod> methods = Scene.v().getEntryPoints();
        this.pta = Scene.v().getPointsToAnalysis();
        methods.forEach(m -> traverse(m, traversedMethods, Statement.Type.IN_BETWEEN));

        String stringConflicts = String.format("%s", conflicts);
        logger.log(Level.INFO, stringConflicts);
    }

    /**
     * This method captures the safe body of the current method and delegates the analysis function to units of (LEFT or RIGHT) or units of BASE.
     *
     * @param sm            name of the current method to be traversed;
     * @param traversed     list of methods that have already been traversed;
     * @param flowChangeTag This parameter identifies whether statement is in the flow of any statements already marked.
     *                      Initially it receives the value IN_BETWEEN but changes if the call to the current method (sm) has been marked as SOURCE or SINK.
     *                      The remaining statements of the current method that have no markup will be marked according to the flowChangeTag.
     */
    private void traverse(SootMethod sm, List<SootMethod> traversed, Statement.Type flowChangeTag) {

        if (traversed.contains(sm) || sm.isPhantom()) {
            return;
        }

        traversed.add(sm);

        this.body = retrieveActiveBodySafely(sm);

        if (this.body != null) {
            this.body.getUnits().forEach(unit -> {

                if (isTagged(flowChangeTag, unit)) {
                    runAnalysisWithTaggedUnit(sm, traversed, flowChangeTag, unit);
                    detectConflict(unit, flowChangeTag, sm);
                } else {
                    runAnalysisWithBaseUnit(sm, traversed, flowChangeTag, unit);
                }
            });
        }
    }

    private Body retrieveActiveBodySafely(SootMethod sm) {
        try {
            return sm.retrieveActiveBody();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void runAnalysisWithTaggedUnit(SootMethod sm, List<SootMethod> traversed, Statement.Type flowChangeTag, Unit unit) {
        runAnalyze(sm, traversed, flowChangeTag, unit, true);
    }

    private void runAnalysisWithBaseUnit(SootMethod sm, List<SootMethod> traversed, Statement.Type flowChangeTag, Unit unit) {
        runAnalyze(sm, traversed, flowChangeTag, unit, false);
    }

    /**
     * This method analyzes a method and adds or removes items from the list of possible conflicts.
     *
     * @param sm            name of the current method to be traversed;
     * @param traversed     list of methods that have already been traversed;
     * @param flowChangeTag This parameter identifies whether statement is in the flow of any statements already marked.
     *                      Initially it receives the value IN_BETWEEN but changes if the call to the current method (sm) has been marked as SOURCE or SINK.
     *                      The remaining statements of the current method that have no markup will be marked according to the flowChangeTag.
     * @param tagged        Identifies whether the unit is checked or not. If false the unit is base, if true the unit is left or right.
     */
    private void runAnalyze(SootMethod sm, List<SootMethod> traversed, Statement.Type flowChangeTag, Unit unit, boolean tagged) {
        /* Are there other possible cases? Yes, see follow links:
        https://soot-build.cs.uni-paderborn.de/public/origin/develop/soot/soot-develop/jdoc/soot/jimple/Stmt.html
        https://github.com/PAMunb/JimpleFramework/blob/d585caefa8d5f967bfdbeb877346e0ff316e0b5e/src/main/rascal/lang/jimple/core/Syntax.rsc#L77-L95
         */

        if (unit instanceof AssignStmt) {
            /* Does AssignStmt check contain objects, arrays or other types?
             Yes, AssignStmt handles assignments and they can be of any type as long as they follow the structure: variable = value
             */
            AssignStmt assignStmt = (AssignStmt) unit;

            /* Check case: x = foo() + bar()
            In this case, this condition will be executed for the call to the foo() method and then another call to the bar() method.
             */
            if (assignStmt.containsInvokeExpr()) {
                Statement stmt = getStatementAssociatedWithUnit(sm, unit, flowChangeTag);
                traverse(assignStmt.getInvokeExpr().getMethod(), traversed, stmt.getType());
            }

            // TODO rename Statement. (UnitWithExtraInformations)
            Statement stmt = getStatementAssociatedWithUnit(sm, unit, flowChangeTag);

            if (tagged) {
                gen(stmt);
            } else {
                kill(unit);
            }

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
            InvokeStmt invokeStmt = (InvokeStmt) unit;
            Statement stmt = getStatementAssociatedWithUnit(sm, unit, flowChangeTag);
            traverse(invokeStmt.getInvokeExpr().getMethod(), traversed, stmt.getType());
        }
    }

    private boolean isTagged(Statement.Type flowChangeTag, Unit unit) {
        return (isLeftStatement(unit) || isRightStatement(unit)) || (isInLeftStatementFLow(flowChangeTag) || isInRightStatementFLow(flowChangeTag));
    }

    private boolean isInRightStatementFLow(Statement.Type flowChangeTag) {
        return flowChangeTag.equals(Statement.Type.SINK);
    }

    private boolean isInLeftStatementFLow(Statement.Type flowChangeTag) {
        return flowChangeTag.equals(Statement.Type.SOURCE);
    }

    // TODO add in two lists (left and right).
    // TODO add depth to InstanceFieldRef and StaticFieldRef...
    private void gen(Statement stmt) {
        // TODO Check for conflict when adding.
        stmt.getUnit().getDefBoxes().forEach(valueBox -> res.add(new DataFlowAbstraction(valueBox.getValue(), stmt)));
    }

    private void kill(Unit unit) {
        res.forEach(dataFlowAbstraction -> removeAll(unit.getDefBoxes(), dataFlowAbstraction));
    }

    private void removeAll(List<ValueBox> defBoxes, DataFlowAbstraction dataFlowAbstraction) {
        defBoxes.forEach(valueBox -> {
            if (containsValue(dataFlowAbstraction, valueBox.getValue())) {
                res.remove(dataFlowAbstraction);
            }
        });
    }

    /*
     * To detect conflicts res verified if "u" is owned by LEFT or RIGHT
     * and we fill res the "potentialConflictingAssignments" list with the changes from the other developer.
     *
     * We pass "u" and "potentialConflictingAssignments" to the checkConflits method
     * to see if Left assignments interfere with Right changes or
     * Right assignments interfere with Left changes.
     */
    private void detectConflict(Unit u, Statement.Type flowChangeTag, SootMethod sm) {
        List<DataFlowAbstraction> potentialConflictingAssignments = new ArrayList<>();

        if (isRightStatement(u) || isInRightStatementFLow(flowChangeTag)) {
            potentialConflictingAssignments = res.toList().stream().filter(
                    DataFlowAbstraction::containsLeftStatement).collect(Collectors.toList());
        } else if (isLeftStatement(u) || isInLeftStatementFLow(flowChangeTag)) {
            potentialConflictingAssignments = res.toList().stream().filter(
                    DataFlowAbstraction::containsRightStatement).collect(Collectors.toList());
        }

        checkConflicts(u, potentialConflictingAssignments, flowChangeTag, sm);

    }

    /*
     * Checks if there is a conflict and if so adds it to the conflict list.
     */
    private void checkConflicts(Unit unit, List<DataFlowAbstraction> potentialConflictingAssignments, Statement.Type flowChangeTag, SootMethod sm) {
        potentialConflictingAssignments.forEach(dataFlowAbstraction -> unit.getDefBoxes().forEach(valueBox -> {
            if (containsValue(dataFlowAbstraction, valueBox.getValue())) {
                Conflict c = new Conflict(getStatementAssociatedWithUnit(sm, unit, flowChangeTag), dataFlowAbstraction.getStmt());
                conflicts.add(c);
                // TODO remove variable from list to avoid duplication of conflicts.
            }
        }));
    }

    // TODO need to treat other cases (Arrays...)
    private boolean containsValue(DataFlowAbstraction dataFlowAbstraction, Value value) {
        if (dataFlowAbstraction.getValue() instanceof JInstanceFieldRef && value instanceof JInstanceFieldRef) {
            return ((JInstanceFieldRef) dataFlowAbstraction.getValue()).getFieldRef().equals(((JInstanceFieldRef) value).getFieldRef());
        }
        return dataFlowAbstraction.getValue().equals(value);
    }

    private Statement getStatementAssociatedWithUnit(SootMethod sm, Unit u, Statement.Type flowChangeTag) {
        if (isLeftStatement(u)) {
            return findLeftStatement(u);
        } else if (isRightStatement(u)) {
            return findRightStatement(u);
        } else if (!isLeftStatement(u) && isInLeftStatementFLow(flowChangeTag)) {
            return createStatement(sm, u, flowChangeTag);
        } else if (!isRightStatement(u) && isInRightStatementFLow(flowChangeTag)) {
            return createStatement(sm, u, flowChangeTag);
        }
        return findStatementBase(u);
    }

    private boolean isLeftStatement(Unit u) {
        return definition.getSourceStatements().stream().map(Statement::getUnit).collect(Collectors.toList()).contains(u);
    }

    private boolean isRightStatement(Unit u) {
        return definition.getSinkStatements().stream().map(Statement::getUnit).collect(Collectors.toList()).contains(u);
    }

    private Statement findRightStatement(Unit u) {
        return definition.getSinkStatements().stream().filter(s -> s.getUnit().equals(u)).
                findFirst().get();
    }

    private Statement findLeftStatement(Unit u) {
        return definition.getSourceStatements().stream().filter(s -> s.getUnit().equals(u)).
                findFirst().get();
    }

    private Statement findStatementBase(Unit d) {
        return Statement.builder()
                .setClass(body.getMethod().getDeclaringClass())
                .setMethod(body.getMethod())
                .setType(Statement.Type.IN_BETWEEN)
                .setUnit(d)
                .setSourceCodeLineNumber(d.getJavaSourceStartLineNumber()).build();
    }

    private Statement createStatement(SootMethod sm, Unit u, Statement.Type flowChangeTag) {
        return Statement.builder().setClass(sm.getDeclaringClass()).setMethod(sm)
                .setUnit(u).setType(flowChangeTag).setSourceCodeLineNumber(u.getJavaSourceStartLineNumber())
                .build();
    }
}
