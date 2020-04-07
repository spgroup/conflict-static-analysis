package br.unb.cic.analysis.df;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.DoubleSourceConflict;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.soot.graph.SourceNode;
import fj.P;
import jdk.nashorn.internal.runtime.regexp.joni.constants.NodeType;
import soot.Body;
import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DoubleSourceAnalysis extends ReachDefinitionAnalysis {

    public DoubleSourceAnalysis(Body methodBody, AbstractMergeConflictDefinition definition) {
        super(methodBody, definition);
    }

    @Override
    protected FlowSet<DataFlowAbstraction> gen(Unit u, FlowSet<DataFlowAbstraction> in) {
        FlowSet<DataFlowAbstraction> res = new ArraySparseSet<>();
        if(isSourceStatement(u) || isSinkStatement(u)) {
            for(Local local: getDefVariables(u)) {
                Statement stmt = isSourceStatement(u) ? findSourceStatement(u) : findSinkStatement(u);
                res.add(new DataFlowAbstraction(local, stmt));
            }
        }
        return res;
    }

    protected void detectConflict(FlowSet<DataFlowAbstraction> in, Unit u) {
        if(isSourceStatement(u) || isSinkStatement(u)) {
            return;
        }
        List<Statement> sources = new ArrayList<>(); // a list of source "vars" used in the unit
        List<Statement> sinks = new ArrayList<>();   // a list of sink "vars" used in the unit

        // iterate over the "used" variables
        for(Local local: getUseVariables(u)) {
            sources.addAll(in.toList().stream()
                    .filter(element -> element.getStmt().getType().equals(Statement.Type.SOURCE)
                            && element.getLocal().equals(local))
                    .map(element -> element.getStmt()).collect(Collectors.toList()));

            sinks.addAll(in.toList().stream()
                    .filter(element -> element.getStmt().getType().equals(Statement.Type.SINK)
                            && element.getLocal().equals(local))
                    .map(item -> item.getStmt()).collect(Collectors.toList()));
        }

        //report the conflicts
        for(Statement source: sources) {
            for(Statement sink: sinks) {
                Conflict c = new DoubleSourceConflict(source, sink, findStatement(u));
                Collector.instance().addConflict(c);
            }
        }
    }

    private List<Local> getUseVariables(Unit u) {
        return u.getUseBoxes().stream()
                .map(box -> box.getValue())
                .filter(v -> v instanceof Local)
                .map(v -> (Local)v)
                .collect(Collectors.toList());
    }

    private List<Local> getDefVariables(Unit u) {
        return u.getDefBoxes().stream()
                .map(box -> box.getValue())
                .filter(v -> v instanceof Local)
                .map(v -> (Local)v)
                .collect(Collectors.toList());
    }



}
