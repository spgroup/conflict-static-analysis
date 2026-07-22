package br.unb.cic.analysis.dfp;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.SootWrapper;
import soot.Scene;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class CallGraphComparisonTest {

    private String cp = "output/files/elasticsearch/d896886973660785aac45275ddb110c1a6babc57/original-without-dependencies/merge/elasticsearch-tests.jar";

    @Test
    public void verifySparkIsSubgraphOfCha() throws Exception {
        String entryPoint = "<org.elasticsearch.action.support.replication.ReplicationOperationTests: void testReplication()>";
        
        System.out.println("=== Starting Call Graph Comparison Proof ===");
        System.out.println("Entry Point: " + entryPoint);

        // 1. Build CHA Call Graph
        soot.G.reset();
        SootWrapper.configureSootOptionsToRunInterproceduralOverrideAssignmentAnalysis(cp, "CHA");
        Scene.v().loadNecessaryClasses();
        soot.SootMethod entryMethod = Scene.v().getMethod(entryPoint);
        Scene.v().setEntryPoints(Collections.singletonList(entryMethod));
        
        soot.PackManager.v().getPack("cg").apply();
        CallGraph chaCG = Scene.v().getCallGraph();
        
        Set<String> chaEdges = new HashSet<>();
        chaCG.iterator().forEachRemaining(e -> chaEdges.add(edgeToString(e)));
        System.out.println("[CHA] Total Edges: " + chaEdges.size());

        // 2. Build SPARK Call Graph
        soot.G.reset();
        SootWrapper.configureSootOptionsToRunInterproceduralOverrideAssignmentAnalysis(cp, "SPARK");
        Scene.v().loadNecessaryClasses();
        entryMethod = Scene.v().getMethod(entryPoint);
        Scene.v().setEntryPoints(Collections.singletonList(entryMethod));

        soot.PackManager.v().getPack("cg").apply();
        CallGraph sparkCG = Scene.v().getCallGraph();

        Set<String> sparkEdges = new HashSet<>();
        sparkCG.iterator().forEachRemaining(e -> sparkEdges.add(edgeToString(e)));
        System.out.println("[SPARK] Total Edges: " + sparkEdges.size());

        // 3. Verify Subgraph Property
        long missingEdges = sparkEdges.stream().filter(e -> !chaEdges.contains(e)).count();

        System.out.println("=== Comparison Result ===");
        System.out.println("Edges in SPARK: " + sparkEdges.size());
        System.out.println("Edges in CHA:   " + chaEdges.size());
        System.out.println("Edges in SPARK but NOT in CHA: " + missingEdges);

        if (missingEdges == 0) {
            System.out.println("SUCCESS: SPARK is a strict subgraph of CHA.");
        } else {
            System.out.println("WARNING: SPARK is NOT a strict subgraph. Discrepancy ratio: " + (double)missingEdges/sparkEdges.size());
            sparkEdges.stream().filter(e -> !chaEdges.contains(e)).limit(5).forEach(e -> System.out.println("  Missing edge example: " + e));
        }

        Assert.assertTrue("SPARK should be a subgraph of CHA", missingEdges == 0);
    }

    private String edgeToString(Edge e) {
        return e.src().toString() + " -> " + e.tgt().toString();
    }
}
