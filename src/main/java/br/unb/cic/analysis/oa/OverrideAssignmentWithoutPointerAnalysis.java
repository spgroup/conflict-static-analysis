package br.unb.cic.analysis.oa;

import br.unb.cic.analysis.AbstractAnalysis;
import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Statement;
import soot.Local;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;

import java.util.List;

public class OverrideAssignmentWithoutPointerAnalysis extends OverrideAssignment implements AbstractAnalysis {

    public OverrideAssignmentWithoutPointerAnalysis(AbstractMergeConflictDefinition definition, int depthLimit, Boolean interprocedural, List<String> entrypoints) {
        super(definition, depthLimit, interprocedural, entrypoints);
    }

    public OverrideAssignmentWithoutPointerAnalysis(AbstractMergeConflictDefinition definition, int depthLimit, boolean interprocedural) {
        super(definition, depthLimit, interprocedural);
    }

    public OverrideAssignmentWithoutPointerAnalysis(AbstractMergeConflictDefinition definition) {
        super(definition);
    }

    @Override
    protected void gen(OverrideAssignmentAbstraction in, Statement stmt) {
        in.add(stmt);
    }

    @Override
    protected boolean isSameStateElement(Statement stmtInAbs, Statement stmtInFlow) {
        for (ValueBox defBoxInAbs : stmtInAbs.getUnit().getDefBoxes()) {
            for (ValueBox defBoxInFlow : stmtInFlow.getUnit().getDefBoxes()) {
                Value valueInAbs = defBoxInAbs.getValue();
                Value valueInFlow = defBoxInFlow.getValue();

                if (valueInAbs instanceof Local && valueInFlow instanceof Local) {
                    return isSameLocal(stmtInAbs, stmtInFlow, valueInAbs, valueInFlow);
                } else if (valueInAbs instanceof InstanceFieldRef && valueInFlow instanceof InstanceFieldRef) {
                    return isSameFieldRef(stmtInAbs, stmtInFlow, (InstanceFieldRef) valueInAbs, (InstanceFieldRef) valueInFlow);
                } else if (valueInAbs instanceof ArrayRef && valueInFlow instanceof ArrayRef) {
                    return isSameArrayRef(valueInAbs, valueInFlow);
                } else if (valueInAbs instanceof StaticFieldRef && valueInFlow instanceof StaticFieldRef) {
                    return isSameStaticFieldRef(valueInAbs, valueInFlow);
                } else if (valueInAbs instanceof ArrayRef && valueInFlow instanceof Local) {
                    return areArrayAndLocalCompatible(stmtInAbs, stmtInFlow, valueInAbs, valueInFlow);
                } else if (valueInAbs instanceof Local && valueInFlow instanceof ArrayRef) {
                    return isLocalAndArrayCompatible(stmtInAbs, stmtInFlow, (ArrayRef) valueInFlow);
                }
            }
        }
        return false;
    }

    private static boolean isSameArrayRef(Value valueInAbs, Value valueInFlow) {
        return valueInAbs.toString().equals(valueInFlow.toString());
    }

    private static boolean isSameFieldRef(Statement stmtInAbs, Statement stmtInFlow, InstanceFieldRef valueInAbs, InstanceFieldRef valueInFlow) {
        boolean typesEqual = valueInAbs.getType().equals(valueInFlow.getType());
        boolean fieldRefsEqual = valueInAbs.getFieldRef().equals(valueInFlow.getFieldRef());
        boolean baseNameEqual = valueInAbs.getBase().toString().equals(valueInFlow.getBase().toString());
        boolean nameOrTypeAreEquals = baseNameEqual || typesEqual;
        boolean methodIsConstructor = stmtInAbs.getSootMethod().isConstructor() && stmtInFlow.getSootMethod().isConstructor();

        return nameOrTypeAreEquals && fieldRefsEqual && !methodIsConstructor;
    }

    private static boolean isLocalAndArrayCompatible(Statement stmtInAbs, Statement stmtInFlow, ArrayRef valueInFlow) {
        if (!stmtInAbs.getSootMethod().equals(stmtInFlow.getSootMethod())) {
            return false;
        }
        return stmtInAbs.getUnit().getUseBoxes().get(0).getValue().equals(valueInFlow.getBase());
    }

    private static boolean areArrayAndLocalCompatible(Statement stmtInAbs, Statement stmtInFlow, Value valueInAbs, Value valueInFlow) {
        if (!stmtInAbs.getSootMethod().equals(stmtInFlow.getSootMethod())) {
            return false;
        }
        return valueInAbs.toString().contains(valueInFlow.toString());
    }
}
