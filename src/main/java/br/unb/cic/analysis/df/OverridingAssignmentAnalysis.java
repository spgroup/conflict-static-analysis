package br.unb.cic.analysis.df;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.Statement;
import soot.*;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.JArrayRef;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OverridingAssignmentAnalysis extends ReachDefinitionAnalysis {

    /**
     * Constructor of the DataFlowAnalysis class.
     * <p>
     * According to the SOOT architecture, the constructor for a
     * flow analysis must receive as an argument a graph, set up
     * essential information and call the doAnalysis method of the
     * super class.
     *
     * @param definition a set of conflict definitions.
     */
    public OverridingAssignmentAnalysis(Body methodBody, AbstractMergeConflictDefinition definition) {
        super(methodBody, definition);
    }

    /**
     * Runs the algorithm analysis at a given statement (Unit d). Here we
     * manipulate and compute an out set from the income set in (foward analysis).
     * <p>
     * When left or right assign a variable it is added to the abstraction.
     * <p>
     * If there is an assignment coming from the base for the same variable that had
     * been assigned by left or right then the variable is removed from the abstraction.
     * <p>
     * In the case of arrays, the variable is added to the abstraction no matter if the
     * assignment is just for one of its components, as in array[1] = 2.
     * <p>
     * Conflicts are reported when the same variable is used by developer left and developer Right.
     *
     * @param in a set of abstractions that arrive at the statement d
     * @param u  a specific statement
     * @out the result of applying the analysis considering the income abstraction and the statement d
     */
    @Override
    protected void flowThrough(FlowSet<DataFlowAbstraction> in, Unit u, FlowSet<DataFlowAbstraction> out) {

        detectConflict(in, u);
        FlowSet<DataFlowAbstraction> temp = new ArraySparseSet<>();

        FlowSet<DataFlowAbstraction> killSet = new ArraySparseSet<>();

        for (DataFlowAbstraction item : in) {
            if (abstractionVariableIsInIUnitDefBoxes(item, u)) {
                killSet.add(item);
            }
        }
        in.difference(killSet, temp);
        temp.union(gen(u, in), out);
    }

    /*
     * Here we separate the Unit types to create the DataFlowAbstraction according to its specific constructor.
     */
    @Override
    protected FlowSet<DataFlowAbstraction> gen(Unit u, FlowSet<DataFlowAbstraction> in) {
        FlowSet<DataFlowAbstraction> res = new ArraySparseSet<>();
        if (isLeftStatement(u) || isRightStatement(u)) {
            Statement stmt = getStatementAssociatedWithUnit(u);
            u.getDefBoxes().forEach(valueBox -> {
                if(valueBox.getValue() instanceof Local){
                    res.add(new DataFlowAbstraction((Local) valueBox.getValue(), stmt));
                }else if(valueBox.getValue() instanceof JArrayRef){
                    res.add(new DataFlowAbstraction((Local) getJArrayRefName((JArrayRef) valueBox.getValue()), stmt));
                }else if(valueBox.getValue() instanceof  StaticFieldRef) {
                    res.add(new DataFlowAbstraction((StaticFieldRef) valueBox.getValue(), stmt));
                }
            });
        }
        return res;
    }

    /*
     * To detect conflicts in verified if "u" is owned by LEFT or RIGHT
     * and we fill in the "potentialConflictingAssignments" list with the changes from the other developer.
     *
     * We pass "u" and "potentialConflictingAssignments" to the checkConflits method
     * to see if Left assignments interfere with Right changes or
     * Right assignments interfere with Left changes.
     */
    @Override
    protected void detectConflict(FlowSet<DataFlowAbstraction> in, Unit u) {
        if (!(isRightStatement(u) || isLeftStatement(u))) {
            return;
        }

        List<DataFlowAbstraction> potentialConflictingAssignments = new ArrayList<>();

        if (isRightStatement(u)) {
            potentialConflictingAssignments = in.toList().stream().filter(DataFlowAbstraction::containsLeftStatement).collect(Collectors.toList());
        } else if (isLeftStatement(u)) {
            potentialConflictingAssignments = in.toList().stream().filter(DataFlowAbstraction::containsRightStatement).collect(Collectors.toList());
        }

        checkConflicts(u, potentialConflictingAssignments);

    }

    /*
     * Checks if there is a conflict and if so adds it to the conflict list.
     */
    private void checkConflicts(Unit u, List<DataFlowAbstraction> potentialConflictingAssignments) {
        for (DataFlowAbstraction dataFlowAbstraction : potentialConflictingAssignments) {
            if (abstractionVariableIsInIUnitDefBoxes(dataFlowAbstraction, u)) {
                Conflict c = new Conflict(dataFlowAbstraction.getStmt(), findStatement(u));
                Collector.instance().addConflict(c);
            }
        }
    }

    /*
     * Checks the equality of variable names between an
     * item in the abstraction list and all the UnitBox's ValueBox.
     */
    private boolean abstractionVariableIsInIUnitDefBoxes(DataFlowAbstraction dataFlowAbstraction, Unit u) {
        for (ValueBox valueBox : u.getDefBoxes()) {
            return getVarNameInAbstraction(dataFlowAbstraction).equals(getVarNameFromValueBox(valueBox));
        }
        return false;
    }

    /*
     * Returns a String containing the name of the variable given a DataFlowAbstraction
     */
    private String getVarNameInAbstraction(DataFlowAbstraction dataFlowAbstraction) {
        String varNameDataFlowAbstraction = "";
        if (dataFlowAbstraction.getLocal() != null) {
            varNameDataFlowAbstraction = getLocalName(dataFlowAbstraction.getLocal());
        } else if (dataFlowAbstraction.getLocalStaticFieldRef() != null) {
            varNameDataFlowAbstraction = getStaticFieldRefName(dataFlowAbstraction.getLocalStaticFieldRef());
        }

        return varNameDataFlowAbstraction;
    }

    /*
     * Returns a String containing the name of the variable given a ValueBox
     */
    private String getVarNameFromValueBox(ValueBox valueBox) {
        String varNameValueBox = "";
        if (valueBox.getValue() instanceof Local) {
            varNameValueBox = getLocalName((Local) valueBox.getValue());
        } else if (valueBox.getValue() instanceof StaticFieldRef) {
            varNameValueBox = getStaticFieldRefName((StaticFieldRef) valueBox.getValue());
        } else if (valueBox.getValue() instanceof JArrayRef) {
            varNameValueBox = getJArrayRefName((JArrayRef) valueBox.getValue()).toString();
        }
        return varNameValueBox;
    }

    private Statement getStatementAssociatedWithUnit(Unit u) {
        if (isLeftStatement(u)) {
            return findLeftStatement(u);
        }
        return findRightStatement(u);
    }

    /*
     * Divide the Local name into an array
     * separated by '#' to get just the name of the variable;
     * e.g: x#3 --> x
     */
    private String getLocalName(Local local) {
        return local.getName().split("#")[0];
    }

    /*
     * Returns a String containing the name
     * of the variable used to assign an array;
     * e.g:  int[] arr = {0, 1}; --> "arr"
     */
    private Value getJArrayRefName(JArrayRef jArrayRef) {
        return jArrayRef.getBaseBox().getValue();
    }

    /*
     * Returns a String containing the name
     * of the variable used to assign a static variable;
     * e.g:  private static int x; --> "x"
     */
    private String getStaticFieldRefName(StaticFieldRef staticFieldRef) {
        return staticFieldRef.getField().getName();
    }

}