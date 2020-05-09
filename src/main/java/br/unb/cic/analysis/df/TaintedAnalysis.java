package br.unb.cic.analysis.df;

import soot.*;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;

public class TaintedAnalysis extends ReachDefinitionAnalysis {

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
    public TaintedAnalysis(Body methodBody, AbstractMergeConflictDefinition definition) {
        super(methodBody, definition);
    }

    @Override
    protected FlowSet<DataFlowAbstraction> gen(Unit u, FlowSet<DataFlowAbstraction> in) {
        FlowSet<DataFlowAbstraction> res = new ArraySparseSet<>();
        if (isSourceStatement(u)) {
            for(Local local: getDefVariables(u)) {
                    res.add(new DataFlowAbstraction(local, findSourceStatement(u)));
            }
        } else if (u.getDefBoxes().size() > 0) {
            u.getUseBoxes().stream().filter(v -> v.getValue() instanceof Local).forEach(v -> {
                Local local = (Local) v.getValue();
                in.forEach(sourceDefs -> {
                    if (sourceDefs.getLocal().equals(local)) {
                        // add a new entry to each variable that is being assigned in the unit.
                        // something like:  a, b = x
                        u.getDefBoxes().stream()
                                       .filter(def -> def.getValue() instanceof  Local)
                                       .forEach(def -> {
                            res.add(new DataFlowAbstraction((Local)def.getValue(), findStatement(u)));
                        });
                    }
                });
            });
        }
        return res;
    }

}
