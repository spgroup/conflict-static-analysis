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
     * In the case of arrays, the variable is added to the abstraction completely even
     * though values ​​have been assigned only to some index.
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
            if (checkEqualVariablesName(item, u)) {
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
            getStatementAssociatedWithUnit(u).forEach(valueBox -> {
                Statement stmt = assignStatement(u);

                if (valueBox.getValue() instanceof Local) {
                    Local local = (Local) valueBox.getValue();
                    res.add(new DataFlowAbstraction(local, stmt));
                } else if (valueBox.getValue() instanceof StaticFieldRef) {
                    StaticFieldRef staticFieldRef = (StaticFieldRef) valueBox.getValue();
                    res.add(new DataFlowAbstraction(staticFieldRef, stmt));
                } else if (valueBox.getValue() instanceof JArrayRef) {
                    JArrayRef jArrayRef = (JArrayRef) valueBox.getValue();
                    res.add(new DataFlowAbstraction(jArrayRef, stmt));
                }
            });
        }
        return res;
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

        getStatementAssociatedWithUnit(u).forEach(valueBox -> {
            String localName = getLocalValueBoxName(valueBox);

            for (DataFlowAbstraction filterIn : in) {
                String inName = getLocalDataFlowAbstraction(filterIn);

                if (filterIn.getStmt().getType().equals(Statement.Type.SOURCE) && inName.equals(localName)) {
                    left.add(filterIn);
                } else if (filterIn.getStmt().getType().equals(Statement.Type.SINK) && inName.equals(localName)) {
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
            if (checkEqualVariablesName(dataFlowAbstraction, u)) {
                Conflict c = new Conflict(dataFlowAbstraction.getStmt(), findStatement(u));
                Collector.instance().addConflict(c);
            }
        }
    }
    /*
     * Checks the equality of variable names between an
     * item in the abstraction list and all the UnitBox's ValueBox.
     */
    private boolean checkEqualVariablesName(DataFlowAbstraction dataFlowAbstraction, Unit u) {
        for (ValueBox valueBox : u.getDefBoxes()) {
            return getLocalDataFlowAbstraction(dataFlowAbstraction).equals(getLocalValueBoxName(valueBox));
        }
        return false;
    }

    /*
     * Returns a String containing the name of the variable given a DataFlowAbstraction
     */
    private String getLocalDataFlowAbstraction(DataFlowAbstraction dataFlowAbstraction) {
        String localDataFlowAbstraction = "";
        if (dataFlowAbstraction.getLocal() != null) {
            localDataFlowAbstraction = getLocalName(dataFlowAbstraction.getLocal());
        } else if (dataFlowAbstraction.getLocalStaticFieldRef() != null) {
            localDataFlowAbstraction = getStaticFieldRefName(dataFlowAbstraction.getLocalStaticFieldRef());
        } else if (dataFlowAbstraction.getLocalJArrayRef() != null) {
            localDataFlowAbstraction = getJArrayRefName(dataFlowAbstraction.getLocalJArrayRef());
        }

        return localDataFlowAbstraction;
    }

    /*
     * Returns a String containing the name of the variable given a ValueBox
     */
    private String getLocalValueBoxName(ValueBox valueBox) {
        String localValueBoxName = "";
        if (valueBox.getValue() instanceof Local) {
            localValueBoxName = getLocalName((Local) valueBox.getValue());
        } else if (valueBox.getValue() instanceof StaticFieldRef) {
            localValueBoxName = getStaticFieldRefName((StaticFieldRef) valueBox.getValue());
        } else if (valueBox.getValue() instanceof JArrayRef) {
            localValueBoxName = getJArrayRefName((JArrayRef) valueBox.getValue());
        }
        return localValueBoxName;
    }

    private Statement assignStatement(Unit u) {
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
    private Stream<ValueBox> getStatementAssociatedWithUnit(Unit u) {
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