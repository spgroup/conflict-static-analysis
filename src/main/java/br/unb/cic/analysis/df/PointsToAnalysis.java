package br.unb.cic.analysis.df;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import soot.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.FlowSet;

public class PointsToAnalysis extends DataFlowAnalysis {

    AbstractMergeConflictDefinition definition;

    public PointsToAnalysis(DirectedGraph g, AbstractMergeConflictDefinition definition) {
        super(g, definition);
        this.definition = definition;
        doAnalysis();
    }

    protected void detectConflict(FlowSet<Local> in, Unit d) {
       // int old = Collector.instance().getConflicts().size();
        /*
         * in our case, points to analysis is just an extension of data-flow analysis.
         * for this reason, we should first call super.detectConflict.
         * however, here we are more conservative. we report just
         * one conflict for a given statement d.
         */
        //super.detectConflict(in, d);

//        if(Collector.instance().getConflicts().size() > old) {
//            return;
//        }

        soot.PointsToAnalysis pa = Scene.v().getPointsToAnalysis();
        if(isSinkStatement(d)) {
            for(ValueBox box: d.getUseBoxes()){
                for(Local x: in) {
                    if(box.getValue() instanceof Local) {
                        Local y = (Local)box.getValue();
                        PointsToSet xset = pa.reachingObjects(x);
                        PointsToSet yset = pa.reachingObjects(y);
                        if(xset.hasNonEmptyIntersection(yset)) {
                            Collector.instance().addConflict("Conflict: " + x + "might points to " + y);
                        }
                    }
                }
            }
        }
    }
}
