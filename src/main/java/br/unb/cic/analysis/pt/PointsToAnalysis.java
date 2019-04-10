package br.unb.cic.analysis.pt;

import br.unb.cic.analysis.AbstractAnalysis;
import br.unb.cic.analysis.model.Conflict;
import soot.*;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Statement;
import soot.jimple.parser.analysis.Analysis;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PointsToAnalysis implements AbstractAnalysis {

    private AbstractMergeConflictDefinition definition;
    private Set<Conflict> conflicts;
    private soot.PointsToAnalysis pa;

    public PointsToAnalysis(AbstractMergeConflictDefinition definition) {
        this.definition = definition;
        this.conflicts = new HashSet<>();
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
                    throw new RuntimeException("Not implemented yet");
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

    @Override
    public void clear() {
        conflicts.clear();
    }

    @Override
    public Set<Conflict> getConflicts() {
        return conflicts;
    }
}
