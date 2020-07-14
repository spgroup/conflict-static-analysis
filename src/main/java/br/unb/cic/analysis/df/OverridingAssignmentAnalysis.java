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
import java.util.stream.Stream;

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
     *
     * When left or right assign a variable it is added to the abstraction.
     *
     * If there is an assignment coming from the base for the same variable that had
     * been assigned by left or right then the variable is removed from the abstraction.
     *
     * In the case of arrays, the variable is added to the abstraction no matter if the
     * assignment is just for one of its components, as in array[1] = 2.
     *
     * Conflicts are reported when the same variable is used by developer left and developer Right.
     *
     * @param in a set of abstractions that arrive at the statement d
     * @param u a specific statement
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
            filterDefBoxesInUnit(u).forEach(valueBox -> {
                res.add(createDataFlowAbstraction(valueBox.getValue(), stmt));
            });
        }
        return res;
    }

    private DataFlowAbstraction createDataFlowAbstraction(Value value, Statement stmt) {
        return new DataFlowAbstraction(value, stmt);
    }

    /*
     * Here we divide the content of "in" into two lists
     * left and right based on the type of your statement.
     *
     * To check if there is a possible conflict we compare
     * the unit of right with the list of left
     * or the unit of left with the list of Right
     */
    @Override
    protected void detectConflict(FlowSet<DataFlowAbstraction> in, Unit u) {
        if (!(isRightStatement(u) || isLeftStatement(u))) {
            return;
        }

        List<DataFlowAbstraction> left = new ArrayList<>();
        List<DataFlowAbstraction> right = new ArrayList<>();

        filterDefBoxesInUnit(u).forEach(valueBox -> {
            String valueBoxVarName = getVarNameFromValueBox(valueBox);

            for (DataFlowAbstraction filterIn : in) {
                String inName = getVarNameInAbstraction(filterIn);

                if (filterIn.containsLeftStatement() && inName.equals(valueBoxVarName)) {
                    left.add(filterIn);
                } else if (filterIn.containsRightStatement() && inName.equals(valueBoxVarName)) {
                    right.add(filterIn);
                }
            }
        });

        if (isRightStatement(u)) {
            checkConflicts(u, left);
        } else if (isLeftStatement(u)) {
            checkConflicts(u, right);
        }
    }

    /*
     * Checks if there is a conflict and if so adds it to the conflict list.
     */
    private void checkConflicts(Unit u, List<DataFlowAbstraction> dataFlowAbstractionList) {
        for (DataFlowAbstraction dataFlowAbstraction : dataFlowAbstractionList) {
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
        } else if (dataFlowAbstraction.getLocalJArrayRef() != null) {
            varNameDataFlowAbstraction = getJArrayRefName(dataFlowAbstraction.getLocalJArrayRef());
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
            varNameValueBox = getJArrayRefName((JArrayRef) valueBox.getValue());
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
    private String getJArrayRefName(JArrayRef jArrayRef) {
        return jArrayRef.getBaseBox().getValue().toString();
    }

    /*
     * Returns a String containing the name
     * of the variable used to assign a static variable;
     * e.g:  private static int x; --> "x"
     */
    private String getStaticFieldRefName(StaticFieldRef staticFieldRef) {
        return staticFieldRef.getField().getName();
    }

    /*
     * Returns a list of Boxes containing Values ​​defined
     * in this Unit for the types selected in the filter.
     */
    private Stream<ValueBox> filterDefBoxesInUnit(Unit u) {
        return u.getDefBoxes()
                .stream()
                .filter(valueBox -> valueBox.getValue() instanceof Local
                        || valueBox.getValue() instanceof JArrayRef
                        || valueBox.getValue() instanceof StaticFieldRef
                );
    }

    private boolean isLeftStatement(Unit u) {
        return isSourceStatement(u);
    }

    private boolean isRightStatement(Unit u) {
        return isSinkStatement(u);
    }

    private Statement findRightStatement(Unit u) {
        return findSinkStatement(u);
    }

    private Statement findLeftStatement(Unit u) {
        return findSourceStatement(u);
    }
}