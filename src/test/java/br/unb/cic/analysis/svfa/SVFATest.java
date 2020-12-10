package br.unb.cic.analysis.svfa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.soot.graph.Graph;
import br.unb.cic.soot.graph.Node;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scalax.collection.GraphBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SVFATest {

    public static final String CLASS_NAME_INTRAPROCEDURAL = "br.unb.cic.analysis.samples.IntraproceduralDataFlow";
    public static final String CLASS_NAME_INTERROCEDURAL = "br.unb.cic.analysis.samples.InterproceduralTestCaseSameClass";

    public static final String CLASS_NAME_RECURSIVE_DEFINITION_01 = "br.unb.cic.analysis.samples.RecursiveDefinitionSample01";
    public static final String CLASS_NAME_RECURSIVE_DEFINITION_02 = "br.unb.cic.analysis.samples.RecursiveDefinitionSample02";
    public static final String CLASS_NAME_RECURSIVE_DEFINITION_03 = "br.unb.cic.analysis.samples.RecursiveDefinitionSample03";


    public SVFAInterProcedural configureInterProceduralSameMethod() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_INTRAPROCEDURAL, new int[]{6}, new int[]{11});
        String cp = "target/test-classes";
        return new SVFAInterProcedural(cp, definition);
    }

    public SVFAIntraProcedural configureIntraProceduralSameMethod() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_INTRAPROCEDURAL, new int[]{6}, new int[]{11});
        String cp = "target/test-classes";
        return new SVFAIntraProcedural(cp, definition);
    }

    public SVFAIntraProcedural configureIntraProceduralDifferentMethod() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_INTERROCEDURAL, new int[]{9}, new int[]{19});
        String cp = "target/test-classes";
        return new SVFAIntraProcedural(cp, definition);
    }

    public SVFAInterProcedural configureInterProceduralDifferentMethod() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_INTERROCEDURAL, new int[]{9}, new int[]{19});
        String cp = "target/test-classes";
        return new SVFAInterProcedural(cp, definition);
    }

    public SVFAInterProcedural configureInterProceduralRecursiveDefinition01() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_RECURSIVE_DEFINITION_01, new int[]{5}, new int[]{10}, true);
        String cp = "target/test-classes";
        return new SVFAInterProcedural(cp, definition);
    }

    public SVFAInterProcedural configureInterProceduralRecursiveDefinition02() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_RECURSIVE_DEFINITION_02, new int[]{5}, new int[]{14}, true);
        String cp = "target/test-classes";
        return new SVFAInterProcedural(cp, definition);
    }

    public SVFAInterProcedural configureInterProceduralRecursiveDefinition03() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_RECURSIVE_DEFINITION_03, new int[]{11}, new int[]{6}, true);
        String cp = "target/test-classes";
        return new SVFAInterProcedural(cp, definition);
    }

    @Test
    public void testSVFAnalysisIntraProcedural() {
        SVFAAnalysis analysis = configureInterProceduralSameMethod();
        analysis.buildSparseValueFlowGraph();
        Graph<Node> g = analysis.svg();
        Assert.assertEquals(9, g.nodes().size());
        Assert.assertEquals(1, analysis.reportConflicts().size());
        Assert.assertEquals(1, analysis.findSourceSinkPaths().size());
    }

    @Test
    public void testSVFAnalysisInterProcedural() {
        SVFAAnalysis analysis = configureInterProceduralDifferentMethod();
        analysis.buildSparseValueFlowGraph();
        for (List<Node> paths : analysis.findSourceSinkPaths()) {
            System.out.println(String.join("-> ", paths.stream().map(n -> n.toString()).collect(Collectors.toList())));
        }
        Assert.assertEquals(1, analysis.reportConflicts().size());
    }

    @Test
    public void testSVFAnalysisIntraProceduralSameMethod() {
        SVFAAnalysis analysis = configureIntraProceduralSameMethod();
        analysis.buildSparseValueFlowGraph();
        Assert.assertEquals(1, analysis.reportConflicts().size());
    }

    @Test
    public void testSVFAnalysisIntraProceduralDifferentMethod() {
        SVFAAnalysis analysis = configureIntraProceduralDifferentMethod();
        analysis.buildSparseValueFlowGraph();
        Assert.assertEquals(0, analysis.reportConflicts().size());
    }

    @Test
    public void testSVFAnalysisInterProceduralRecursiveDefinition01() {
        SVFAAnalysis analysis = configureInterProceduralRecursiveDefinition01();
        analysis.buildSparseValueFlowGraph();
        Assert.assertEquals(2, analysis.reportConflicts().size());
    }

    @Test
    public void testSVFAnalysisInterProceduralRecursiveDefinition02() {
        SVFAAnalysis analysis = configureInterProceduralRecursiveDefinition02();
        analysis.buildSparseValueFlowGraph();
        Assert.assertEquals(2, analysis.reportConflicts().size());
    }

    @Test
    public void testSVFAnalysisInterProceduralRecursiveDefinition03() {
        SVFAAnalysis analysis = configureInterProceduralRecursiveDefinition03();
        analysis.buildSparseValueFlowGraph();
        Assert.assertEquals(1, analysis.reportConflicts().size());
        //analysis.reportConflicts().foreach(path -> System.out.println(path));
    }

}
