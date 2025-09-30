package br.unb.cic.analysis;

import scala.collection.JavaConverters;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CallGraphManager extends SceneTransformer {
    private final StatementsUtil statementsUtils;
    private final String algorithm;

    public CallGraphManager(AbstractMergeConflictDefinition definition, List<String> entrypoints, String algorithm) {
        this.statementsUtils = new StatementsUtil(definition, entrypoints);
        this.algorithm = algorithm;
    }

    @Override
    protected void internalTransform(String s, Map<String, String> map) {
        configureEntryPoints();

        CallGraph callGraph = Scene.v().getCallGraph();
        if (algorithm.equals("spark")) {
            System.out.println("CallGraph spark coletado com " + SootWrapper.countEdges(callGraph) + " arestas.");
            SootWrapper.setSparkCG(callGraph);
        } else if (algorithm.equals("cha")) {
            System.out.println("CallGraph CHA coletado com " + SootWrapper.countEdges(callGraph) + " arestas.");
            SootWrapper.setChaCG(callGraph);
        }
    }

    public void configureEntryPoints() {

        scala.collection.immutable.List<SootMethod> scalaList = this.statementsUtils.getCallgraphEntryPoints(); //this instanceof OverrideAssignmentWithPointerAnalysis ? this.statementsUtils.getCallgraphEntryPoints() : this.statementsUtils.getEntryPoints();
        List<SootMethod> entryPoints = new ArrayList<>(JavaConverters.seqAsJavaList(scalaList));
        //List<SootMethod> methods = new ArrayList<>(Collections.singleton(entryPoints.get(1).getDeclaringClass().getMethodByName("main")));
        //System.out.println("CG Entrypoints" + entryPoints);
        Scene.v().setEntryPoints(entryPoints);
    }

    public String getAlgorithm() {
        return algorithm;
    }
}