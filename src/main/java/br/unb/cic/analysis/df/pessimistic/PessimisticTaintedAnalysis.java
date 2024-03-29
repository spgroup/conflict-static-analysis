package br.unb.cic.analysis.df.pessimistic;

import br.unb.cic.analysis.AbstractAnalysis;
import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.Statement;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

public class PessimisticTaintedAnalysis extends ForwardFlowAnalysis<Unit, PessimisticTaintedAnalysisAbstraction> implements AbstractAnalysis {

    private Body methodBody;
    private Set<Conflict> conflicts;
    private AbstractMergeConflictDefinition definition;

    private Map<Local, InstanceFieldRef> fieldLocalAssociation;

    public PessimisticTaintedAnalysis(Body methodBody, AbstractMergeConflictDefinition definition) {
        super(new ExceptionalUnitGraph(methodBody));
        this.methodBody = methodBody;
        this.conflicts = new HashSet<>();
        this.fieldLocalAssociation = new HashMap<>();
        this.definition = definition;
        this.definition.loadSinkStatements();
        this.definition.loadSourceStatements();
        doAnalysis();
    }

    @Override
    protected PessimisticTaintedAnalysisAbstraction newInitialFlow() {
        return new PessimisticTaintedAnalysisAbstraction();
    }

    @Override
    protected void flowThrough(PessimisticTaintedAnalysisAbstraction in, Unit unit, PessimisticTaintedAnalysisAbstraction out) {
        Statement statement = createStatement(unit);
        if (statement.isAssign()) {
            checkAndAssociateAssignedLocalToField(statement);
        }
        in.copy(out);
        detectConflicts(in, statement);
        kill(out, statement);
        gen(out, statement);
    }

    private void checkAndAssociateAssignedLocalToField(Statement statement) {
        AssignStmt assignUnit = (AssignStmt) statement.getUnit();
        Value rightOpValue = assignUnit.getRightOp();
        if (rightOpValue instanceof InstanceFieldRef) {
            InstanceFieldRef fieldRef = (InstanceFieldRef) rightOpValue;

            for (ValueBox defBox : statement.getUnit().getDefBoxes()) {
                fieldLocalAssociation.put((Local) defBox.getValue(), fieldRef);
            }
        }
    }

    protected Statement createStatement(Unit d) {
        Statement.Type type = Statement.Type.IN_BETWEEN;

        boolean isSource = this.definition.isSourceStatement(d);
        boolean isSink = this.definition.isSinkStatement(d);
        if (isSource && isSink) {
            type = Statement.Type.SOURCE_SINK;
        } else if (isSource) {
            type = Statement.Type.SOURCE;
        } else if (isSink){
            type = Statement.Type.SINK;
        }

        return Statement.builder()
                .setClass(methodBody.getMethod().getDeclaringClass())
                .setMethod(methodBody.getMethod())
                .setType(type)
                .setUnit(d)
                .setSourceCodeLineNumber(d.getJavaSourceStartLineNumber()).build();
    }

    protected void detectConflicts(PessimisticTaintedAnalysisAbstraction in, Statement statement) {
        if (statement.isSink()) {
            for (ValueBox use : statement.getUnit().getUseBoxes()) {
                Value value = use.getValue();
                Statement valueDefinitionStatement = in.getValueDefinitionStatement(value);
                boolean isMarked = valueDefinitionStatement != null;

                if (!isMarked && isLocalAndIsAssociatedToField(value)) {
                    valueDefinitionStatement = in.getValueDefinitionStatement(fieldLocalAssociation.get(value));
                    isMarked = valueDefinitionStatement != null;
                }

                if (isMarked) {
                    conflicts.add(new Conflict(in.getValueDefinitionStatement(use.getValue()), statement));
                }
            }

            InstanceInvokeExpr invokeExpr = statement.getInvoke();
            boolean isInvoke = invokeExpr != null;
            if (isInvoke) {
                Value baseValue = invokeExpr.getBase();
                Statement valueFieldsDefinitionStatement = in.getValueFieldsDefinitionStatement(baseValue);
                boolean hasMarkedFields = valueFieldsDefinitionStatement != null;

                if (!hasMarkedFields && isLocalAndIsAssociatedToField(baseValue)) {
                    valueFieldsDefinitionStatement = in.getValueFieldsDefinitionStatement(fieldLocalAssociation.get(baseValue));
                    hasMarkedFields = valueFieldsDefinitionStatement != null;
                }

                if (hasMarkedFields) {
                    conflicts.add(new Conflict(valueFieldsDefinitionStatement, statement));
                }
            }
        }
    }

    private boolean isLocalAndIsAssociatedToField(Value value) {
        return value instanceof Local && fieldLocalAssociation.containsKey(value);
    }

    protected void gen(PessimisticTaintedAnalysisAbstraction in, Statement statement) {
        if (statement.isSource() || in.usesMarkedValue(statement)) {
            for (ValueBox def : statement.getUnit().getDefBoxes()) {
                Value value = def.getValue();

                in.mark(value, statement);
            }
            if (statement.isInvoke()) {
                InstanceInvokeExpr invoke = statement.getInvoke();
                Value baseValue = invoke.getBase();

                in.markFields(baseValue, statement);

                if (isLocalAndIsAssociatedToField(baseValue)) {
                    in.markFields(fieldLocalAssociation.get(baseValue), statement);
                }
            }
        }
    }

    protected void kill(PessimisticTaintedAnalysisAbstraction in, Statement statement) {
        if (!statement.isSource()) {
            for (ValueBox def : statement.getUnit().getDefBoxes()) {
                in.unmark(def.getValue());
            }
        }
        // for now we wont consider method invocation for unmarking the
        // base object fields because the most pessimistic behaviour for this case
        // would be considering that the method didn't change any fields
        // if this causes to many false positives we can implement it
    }

    @Override
    protected void merge(PessimisticTaintedAnalysisAbstraction in1, PessimisticTaintedAnalysisAbstraction in2, PessimisticTaintedAnalysisAbstraction out) {
        in1.merge(in2, out);
    }

    @Override
    protected void copy(PessimisticTaintedAnalysisAbstraction in, PessimisticTaintedAnalysisAbstraction out) {
        in.copy(out);
    }

    @Override
    public void clear() {
        conflicts.clear();
    }

    @Override
    public Set<Conflict> getConflicts() {
        return conflicts;
    }
}
