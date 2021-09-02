package br.unb.cic.analysis.df.pessimistic;

import br.unb.cic.analysis.AbstractAnalysis;
import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.Statement;
import soot.Body;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InstanceInvokeExpr;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.HashSet;
import java.util.Set;

public class PessimisticTaintedAnalysis extends ForwardFlowAnalysis<Unit, PessimisticTaintedAnalysisAbstraction> implements AbstractAnalysis {

    private Body methodBody;
    private Set<Conflict> conflicts;
    private AbstractMergeConflictDefinition definition;

    public PessimisticTaintedAnalysis(Body methodBody, AbstractMergeConflictDefinition definition) {
        super(new ExceptionalUnitGraph(methodBody));
        this.methodBody = methodBody;
        this.conflicts = new HashSet<>();
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
        in.copy(out);
        detectConflicts(in, statement);
        kill(out, statement);
        gen(out, statement);
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
        if (statement.getType() == Statement.Type.SOURCE_SINK) {
            conflicts.add(new Conflict(statement, statement));
        }

        if (statement.isSink()) {
            for (ValueBox use : statement.getUnit().getUseBoxes()) {
                Value value = use.getValue();
                Statement markedStatement = in.getMarkedStatement(value);
                boolean isMarked = markedStatement != null;

                if (isMarked) {
                    conflicts.add(new Conflict(in.getMarkedStatement(use.getValue()), statement));
                }
            }

            InstanceInvokeExpr invokeExpr = statement.getInvoke();
            boolean isInvoke = invokeExpr != null;
            if (isInvoke) {
                Value baseValue = invokeExpr.getBase();
                Statement markedFieldsStatement = in.getMarkedFieldsStatement(baseValue);
                boolean hasMarkedFields = markedFieldsStatement != null;

                if (hasMarkedFields) {
                    conflicts.add(new Conflict(markedFieldsStatement, statement));
                }
            }
        }
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
            }
        }
    }

    protected void kill(PessimisticTaintedAnalysisAbstraction in, Statement statement) {
        for (ValueBox def : statement.getUnit().getDefBoxes()) {
            in.unmark(def.getValue());
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
