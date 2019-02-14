package br.unb.cic.analysis.pt;

import br.unb.cic.analysis.df.Collector;
import soot.*;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Statement;

import java.util.List;
import java.util.stream.Collectors;

public class PointsToAnalysis  {

    AbstractMergeConflictDefinition definition;
    private soot.PointsToAnalysis pa;

    public PointsToAnalysis(AbstractMergeConflictDefinition definition) {
        this.definition = definition;
        this.pa = Scene.v().getPointsToAnalysis();
    }

    public void doAnalysis() {
        List<Local> sourceLocals = findDefLocals(definition.getSourceStatements());
        List<Local> sinkLocals = findUseLocals(definition.getSinkStatements());

        for(Local source: sourceLocals) {
            for(Local sink: sinkLocals) {
                PointsToSet sourceSet = pa.reachingObjects(source);
                PointsToSet sinkSet = pa.reachingObjects(sink);

                if(sourceSet.hasNonEmptyIntersection(sinkSet)) {
                    Collector.instance().addConflict("Conflict: " + source + "might points to " + sink);
                }
            }
        }
    }

    private List<Local> findDefLocals(List<Statement> stmts) {
        return stmts.stream()
                    .flatMap(s -> s.getUnit().getDefBoxes().stream())
                    .filter(vb -> vb.getValue() instanceof  Local)
                    .map(vb -> (Local)vb.getValue())
                    .collect(Collectors.toList());
    }

    private List<Local> findUseLocals(List<Statement> stmts) {
        return stmts.stream()
                .flatMap(s -> s.getUnit().getUseBoxes().stream())
                .filter(vb -> vb.getValue() instanceof  Local)
                .map(vb -> (Local)vb.getValue())
                .collect(Collectors.toList());
    }
}
