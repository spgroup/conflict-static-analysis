package br.unb.cic.analysis.df;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.ConfluenceConflictReport;
import br.unb.cic.analysis.model.Statement;
import soot.Body;
import soot.Local;
import soot.Unit;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfluentTaintedAnalysis extends ReachDefinitionAnalysis {

    public ConfluentTaintedAnalysis(Body methodBody, AbstractMergeConflictDefinition definition) {
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
        else if (u.getDefBoxes().size() > 0) {
            u.getUseBoxes().stream().filter(v -> v.getValue() instanceof Local).forEach(v -> {
                Local local = (Local) v.getValue();
                in.forEach(sourceDefs -> {
                    if(sourceDefs.getLocal().equals(local)){ //if variable in the analyzed stmt is present in IN
                         u.getDefBoxes().stream()
                                 .filter(def -> def.getValue() instanceof  Local)
                                 .forEach(def -> {
                             res.add(new DataFlowAbstraction((Local)def.getValue(), findStatement(u))); //add variable assigned as the stmt to IN
                         });
                    }
                });
            });
        }
        return res;
    }

    @Override
    protected void detectConflict(FlowSet<DataFlowAbstraction> in, Unit u){
        if(isSourceStatement(u) || isSinkStatement(u)){
            return;
        }
        List<Statement> sources = new ArrayList<>();
        List<Statement> sinks = new ArrayList<>();

        for(Local local: getUseVariables(u)){
            sources.addAll(in.toList().stream()
                    .filter(element -> element.getStmt().getType().equals(Statement.Type.SOURCE)
                            && element.getLocal().equals(local))
                    .map(item -> item.getStmt()).collect(Collectors.toList()));
            sinks.addAll(in.toList().stream()
                    .filter(element -> element.getStmt().getType().equals(Statement.Type.SINK)
                            && element.getLocal().equals(local))
                    .map(item -> item.getStmt()).collect(Collectors.toList()));
        }

        for(Statement source: sources){
            for(Statement sink: sinks){
                Conflict c = new ConfluenceConflictReport(source, sink, findStatement(u));
                Collector.instance().addConflict(c);
            }
        }
    }
}
