package br.unb.cic.analysis.ioa;

import br.unb.cic.analysis.AbstractAnalysis;
import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.df.DataFlowAbstraction;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.exceptions.ValueNotHandledException;
import soot.*;
import soot.jimple.*;
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
    private PointsToAnalysis pointsToAnalysis;
    private List<SootMethod> traversedMethods;
    private AbstractMergeConflictDefinition definition;
    private FlowSet<DataFlowAbstraction> left;
    private FlowSet<DataFlowAbstraction> right;
    private Body body;

    private Logger logger;

    public InterproceduralOverrideAssignment(AbstractMergeConflictDefinition definition) {
        this.definition = definition;

        this.conflicts = new HashSet<>();
        this.left = new ArraySparseSet<>();
        this.right = new ArraySparseSet<>();
        this.traversedMethods = new ArrayList<>();
        this.pointsToAnalysis = Scene.v().getPointsToAnalysis();

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

        configureEntryPoints();

        List<SootMethod> methods = Scene.v().getEntryPoints();
        methods.forEach(sootMethod -> traverse(sootMethod, Statement.Type.IN_BETWEEN));



        left.forEach(dataFlowAbstraction -> {
            String leftStmt = String.format("%s", "LEFT: " + dataFlowAbstraction.getStmt());
            logger.log(Level.INFO, leftStmt);
        });

        right.forEach(dataFlowAbstraction -> {
            String rightStmt = String.format("%s", "RIGHT: " + dataFlowAbstraction.getStmt());
            logger.log(Level.INFO, rightStmt);
        });
    }

    /**
     * This method captures the safe body of the current method and delegates the analysis function to units of (LEFT or RIGHT) or units of BASE.
     *
     * @param sootMethod    Current method to be traversed;
     * @param flowChangeTag This parameter identifies whether the unit under analysis is in the flow of any statement already marked.
     *                      Initially it receives the value IN_BETWEEN but changes if the call to the current method (sootMethod) has been marked as SOURCE or SINK.
     *                      The remaining statements of the current method that have no markup will be marked according to the flowChangeTag.
     */
    private void traverse(SootMethod sootMethod, Statement.Type flowChangeTag) {

        if (this.traversedMethods.contains(sootMethod) || sootMethod.isPhantom()) {
            return;
        }

        this.traversedMethods.add(sootMethod);

        this.body = retrieveActiveBodySafely(sootMethod);

        if (this.body != null) {
            this.body.getUnits().forEach(unit -> {

                if (isTagged(flowChangeTag, unit)) {
                    runAnalysisWithTaggedUnit(sootMethod, flowChangeTag, unit);
                } else {
                    runAnalysisWithBaseUnit(sootMethod, flowChangeTag, unit);
                }
            });
        }
    }

    private Body retrieveActiveBodySafely(SootMethod sootMethod) {
        try {
            return sootMethod.retrieveActiveBody();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void runAnalysisWithTaggedUnit(SootMethod sootMethod, Statement.Type flowChangeTag, Unit unit) {
        runAnalysis(sootMethod, flowChangeTag, unit, true);
    }

    private void runAnalysisWithBaseUnit(SootMethod sootMethod, Statement.Type flowChangeTag, Unit unit) {
        runAnalysis(sootMethod, flowChangeTag, unit, false);
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
    private void runAnalysis(SootMethod sootMethod, Statement.Type flowChangeTag, Unit unit, boolean tagged) {
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
                Statement stmt = getStatementAssociatedWithUnit(sootMethod, unit, flowChangeTag);
                traverse(assignStmt.getInvokeExpr().getMethod(), stmt.getType());
            }

            // TODO rename Statement. (UnitWithExtraInformations)
            Statement stmt = getStatementAssociatedWithUnit(sootMethod, unit, flowChangeTag);

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
            Statement stmt = getStatementAssociatedWithUnit(sootMethod, unit, flowChangeTag);
            traverse(invokeStmt.getInvokeExpr().getMethod(), stmt.getType());
        }
    }

    private boolean isTagged(Statement.Type flowChangeTag, Unit unit) {
        return (isLeftStatement(unit) || isRightStatement(unit)) || (isInLeftStatementFlow(flowChangeTag) || isInRightStatementFlow(flowChangeTag));
    }

    private boolean isInRightStatementFlow(Statement.Type flowChangeTag) {
        return flowChangeTag.equals(Statement.Type.SINK);
    }

    private boolean isInLeftStatementFlow(Statement.Type flowChangeTag) {
        return flowChangeTag.equals(Statement.Type.SOURCE);
    }

    // TODO add in two lists (left and right).
    // TODO add depth to InstanceFieldRef and StaticFieldRef...
    private void gen(Statement stmt) {
        if (isLeftStatement(stmt)) {
            checkConflict(stmt, right);
            addToList(stmt, left); // TODO Add only if there is no immediate conflict?

        } else if (isRightStatement(stmt)) {
            checkConflict(stmt, left);
            addToList(stmt, right);
        }
    }

    private boolean isRightStatement(Statement stmt) {
        return stmt.getType().equals(Statement.Type.SINK);
    }

    private boolean isLeftStatement(Statement stmt) {
        return stmt.getType().equals(Statement.Type.SOURCE);
    }

    //TODO Improve the name of this method and the second parameter
    private void addToList(Statement stmt, FlowSet<DataFlowAbstraction> dataFlowAbstractionFlowSet) {
        stmt.getUnit().getDefBoxes().forEach(valueBox -> dataFlowAbstractionFlowSet.add(new DataFlowAbstraction(valueBox.getValue(), stmt)));
    }

    /*
     * Checks if there is a conflict and if so adds it to the conflict list.
     */
    private void checkConflict(Statement stmt, FlowSet<DataFlowAbstraction> dataFlowAbstractionFlowSet) {
        dataFlowAbstractionFlowSet.forEach(dataFlowAbstraction -> stmt.getUnit().getDefBoxes().forEach(valueBox -> {
            try {
                if (containsValue(dataFlowAbstraction, valueBox.getValue())) {
                    conflicts.add(new Conflict(stmt, dataFlowAbstraction.getStmt()));
                    dataFlowAbstractionFlowSet.remove(dataFlowAbstraction);

                }
            } catch (ValueNotHandledException e) {
                assert false;
                e.printStackTrace();
            }
        }));
    }

    private void kill(Unit unit) {
        unit.getDefBoxes().forEach(valueBox -> removeAll(valueBox, left));
        unit.getDefBoxes().forEach(valueBox -> removeAll(valueBox, right));
    }

    private void removeAll(ValueBox valueBox, FlowSet<DataFlowAbstraction> dataFlowAbstractionFlowSet) {
        dataFlowAbstractionFlowSet.forEach(dataFlowAbstraction -> {
            try {
                if (containsValue(dataFlowAbstraction, valueBox.getValue())) {
                    dataFlowAbstractionFlowSet.remove(dataFlowAbstraction);
                }
            } catch (ValueNotHandledException e) {
                e.printStackTrace();
            }
        });
    }

    // TODO need to treat other cases (Arrays...)
    private boolean containsValue(DataFlowAbstraction dataFlowAbstraction, Value value) throws ValueNotHandledException {
        if (dataFlowAbstraction.getValue() instanceof InstanceFieldRef && value instanceof InstanceFieldRef) {
            return ((InstanceFieldRef) dataFlowAbstraction.getValue()).getFieldRef().equals(((InstanceFieldRef) value).getFieldRef());
        }
        if (dataFlowAbstraction.getValue() instanceof Local && value instanceof Local) {
            return dataFlowAbstraction.getValue().equals(value);
        }
        if (dataFlowAbstraction.getValue() instanceof ArrayRef && value instanceof ArrayRef) {
            return ((ArrayRef) dataFlowAbstraction.getValue()).getBaseBox().getValue().toString().equals(((ArrayRef) value).getBaseBox().getValue().toString());
        }
        if (dataFlowAbstraction.getValue() instanceof StaticFieldRef && value instanceof StaticFieldRef) {
            return ((StaticFieldRef) dataFlowAbstraction.getValue()).getField().getName().equals(((StaticFieldRef) value).getField().getName());
        }
        if (!dataFlowAbstraction.getValue().getClass().equals(value.getClass())) {
            return false;
        }

        throw new ValueNotHandledException("Value Not Handled");
    }

    private Statement getStatementAssociatedWithUnit(SootMethod sootMethod, Unit u, Statement.Type flowChangeTag) {
        if (isLeftStatement(u)) {
            return findLeftStatement(u);
        } else if (isRightStatement(u)) {
            return findRightStatement(u);
        } else if (!isLeftStatement(u) && isInLeftStatementFlow(flowChangeTag)) {
            return createStatement(sootMethod, u, flowChangeTag);
        } else if (!isRightStatement(u) && isInRightStatementFlow(flowChangeTag)) {
            return createStatement(sootMethod, u, flowChangeTag);
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
                .setClass(this.body.getMethod().getDeclaringClass())
                .setMethod(this.body.getMethod())
                .setType(Statement.Type.IN_BETWEEN)
                .setUnit(d)
                .setSourceCodeLineNumber(d.getJavaSourceStartLineNumber()).build();
    }

    private Statement createStatement(SootMethod sootMethod, Unit u, Statement.Type flowChangeTag) {
        return Statement.builder().setClass(sootMethod.getDeclaringClass()).setMethod(sootMethod)
                .setUnit(u).setType(flowChangeTag).setSourceCodeLineNumber(u.getJavaSourceStartLineNumber())
                .build();
    }
}
