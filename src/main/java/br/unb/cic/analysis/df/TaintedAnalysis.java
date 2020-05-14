package br.unb.cic.analysis.df;

import soot.*;
import soot.jimple.internal.JArrayRef;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;

import java.util.List;

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
            List<ValueBox> aux = u.getDefBoxes();
            for (ValueBox v : u.getDefBoxes()) {
                if (v.getValue() instanceof Local)
                    res.add(new DataFlowAbstraction((Local) v.getValue(), findSourceStatement(u)));
                else if (v.getValue() instanceof JArrayRef) {
                    JArrayRef ref = (JArrayRef) v.getValue();
                    res.add(new DataFlowAbstraction((Local) ref.getBaseBox().getValue(), findSourceStatement(u)));
                }
            }
        } else if (u.getDefBoxes().size() > 0) {
            List<ValueBox> aux = u.getUseBoxes();   //delete after DEBUG
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
