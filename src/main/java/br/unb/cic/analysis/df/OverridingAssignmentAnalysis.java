package br.unb.cic.analysis.df;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.Statement;
import soot.Body;
import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.internal.JArrayRef;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void flowThrough(FlowSet<DataFlowAbstraction> in, Unit u, FlowSet<DataFlowAbstraction> out) {
        detectConflict(in, u);
        FlowSet<DataFlowAbstraction> temp = new ArraySparseSet<>();

        FlowSet<DataFlowAbstraction> killSet = new ArraySparseSet<>();

        for (DataFlowAbstraction item : in) {
            if (statementEquals(item, u)) {
                killSet.add(item);
            }
        }
        in.difference(killSet, temp);
        temp.union(gen(u, in), out);
    }

    @Override
    protected FlowSet<DataFlowAbstraction> gen(Unit u, FlowSet<DataFlowAbstraction> in) {
        FlowSet<DataFlowAbstraction> res = new ArraySparseSet<>();
        if (isSourceStatement(u) || isSinkStatement(u)) {
            u.getUseAndDefBoxes().stream().filter(v -> v.getValue() instanceof Local).forEach(v -> {
                Statement stmt = isSourceStatement(u)
                        ? findSourceStatement(u)
                        : findSinkStatement(u);
                res.add(new DataFlowAbstraction((Local) v.getValue(), stmt));
            });
        }
        return res;
    }

    @Override
    protected void detectConflict(FlowSet<DataFlowAbstraction> in, Unit u) {
        if (!(isSinkStatement(u) || isSourceStatement(u))) {
            return;
        }

        List<DataFlowAbstraction> left = new ArrayList<>();
        List<DataFlowAbstraction> right = new ArrayList<>();

        u.getUseAndDefBoxes().stream().filter(v -> v.getValue() instanceof Local).forEach(v -> {
            String localName = getLocalName((Local) v.getValue());
            for (DataFlowAbstraction filterIn : in) {
                String inName = getLocalName(filterIn.getLocal());

                if (filterIn.getStmt().getType().equals(Statement.Type.SOURCE) && inName.equals(localName)) {
                    left.add(filterIn);
                } else if (filterIn.getStmt().getType().equals(Statement.Type.SINK) && inName.equals(localName)) {
                    right.add(filterIn);
                }
            }
        });

        if (isSinkStatement(u)) {
            checkConflicts(u, left);
        } else if (isSourceStatement(u)) {
            checkConflicts(u, right);
        }
    }

    /*
     * Checks if the current Unit is equal to
     * each of the statements within the abstraction.
     * If true, a new conflict is created.
     */
    private void checkConflicts(Unit u, List<DataFlowAbstraction> statements) {
        for (DataFlowAbstraction statement : statements) {
            if (statementEquals(statement, u)) {
                Conflict c = new Conflict(statement.getStmt(), findStatement(u));
                Collector.instance().addConflict(c);
            }
        }
    }

    /*
     * Compares the equality between the name
     * of the current statement and unit variables.
     */
    private boolean statementEquals(DataFlowAbstraction statement, Unit u) {
        String statementName = getLocalName(statement.getLocal());
        for (ValueBox local : u.getDefBoxes()) {
            String localName = "";

            if (local.getValue() instanceof JArrayRef) {
                localName = getBaseBoxName(local);
            }

            if (local.getValue() instanceof Local) {
                localName = getLocalName((Local) local.getValue());
            }

            return statementName.equals(localName);
        }
        return false;
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
     * Returns a string containing the name
     * of the variable used to assign an array;
     * e.g:  int[] arr = {0, 1}; --> "arr"
     */
    private String getBaseBoxName(ValueBox valueBox) {
        return (((JArrayRef) valueBox.getValue()).getBaseBox().getValue()).toString();
    }
}