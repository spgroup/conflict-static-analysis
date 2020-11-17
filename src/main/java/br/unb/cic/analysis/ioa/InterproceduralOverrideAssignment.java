package br.unb.cic.analysis.ioa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import soot.*;

import java.util.*;

public class InterproceduralOverrideAssignment extends SceneTransformer  {

    private Set<SootMethod> visitedMethods;

    private PointsToAnalysis pta;

    private AbstractMergeConflictDefinition definition;

    public InterproceduralOverrideAssignment(AbstractMergeConflictDefinition definition) {
        visitedMethods = new HashSet<>();
        this.definition = definition;
    }

    @Override
    protected void internalTransform(String s, Map<String, String> map) {
        definition.loadSourceStatements();
        definition.loadSinkStatements();

        configureEntryPoints();

        List<SootMethod> methods = Scene.v().getEntryPoints();
        pta = Scene.v().getPointsToAnalysis();
        methods.forEach(m -> traverse(m));
    }

    private void traverse(SootMethod m) {
        if(visitedMethods.contains(m) || m.isPhantom()) {
            return;
        }

        Body body = m.retrieveActiveBody();

        body.getUnits().forEach(unit -> {
            // TODO: write specific code for Override Assignment.
        });
    }

    private void configureEntryPoints() {
        List<SootMethod> entryPoints = new ArrayList<>();
        definition.getSourceStatements().forEach(s -> {
            entryPoints.add(s.getSootMethod());
        });
        Scene.v().setEntryPoints(entryPoints);
        // TODO: after fixing the logic for
        //  setting the entry points, remove the following
        //  line.
        throw new RuntimeException("not implemented yet...");
    }
}
