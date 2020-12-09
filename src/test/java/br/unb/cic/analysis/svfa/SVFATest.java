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

    public static final String CLASS_NAME_RECURSIVE_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionSample";
    public static final String CLASS_NAME_RECURSIVE_METHOD_PARAM_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionMethodParamSample";
    public static final String CLASS_NAME_RECURSIVE_METHOD_PARAM2_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionMethodParam2Sample";
    public static final String CLASS_NAME_RECURSIVE_METHOD_PARAM3_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionMethodParam3Sample";
    public static final String CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionClassAttributeSample";
    public static final String CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE2_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionClassAttribute2Sample";
    public static final String CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE3_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionClassAttribute3Sample";
    public static final String CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE4_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionClassAttribute4Sample";
    public static final String CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE5_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionClassAttribute5Sample";

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

    public SVFAInterProcedural configureInterProceduralRecursiveDefinition() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_RECURSIVE_DEFINITION, new int[]{5}, new int[]{10}, true);
        String cp = "target/test-classes";
        return new SVFAInterProcedural(cp, definition);
    }

    public SVFAInterProcedural configureInterProceduralRecursiveMethodParamDefinition() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_RECURSIVE_METHOD_PARAM_DEFINITION, new int[]{5}, new int[]{10}, true);
        String cp = "target/test-classes";
        return new SVFAInterProcedural(cp, definition);
    }

    public SVFAInterProcedural configureInterProceduralRecursiveMethodParam2Definition() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_RECURSIVE_METHOD_PARAM2_DEFINITION, new int[]{5}, new int[]{6}, true);
        String cp = "target/test-classes";
        return new SVFAInterProcedural(cp, definition);
    }

    public SVFAInterProcedural configureInterProceduralRecursiveMethodParam3Definition() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_RECURSIVE_METHOD_PARAM3_DEFINITION, new int[]{5}, new int[]{6, 10}, true);
        String cp = "target/test-classes";
        return new SVFAInterProcedural(cp, definition);
    }

    public SVFAInterProcedural configureInterProceduralRecursiveClassAttributeDefinition() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_DEFINITION, new int[]{6}, new int[]{7}, true);
        String cp = "target/test-classes";
        return new SVFAInterProcedural(cp, definition);
    }

    public SVFAInterProcedural configureInterProceduralRecursiveClassAttribute2Definition() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE2_DEFINITION, new int[]{6}, new int[]{7}, true);
        String cp = "target/test-classes";
        return new SVFAInterProcedural(cp, definition);
    }

    public SVFAInterProcedural configureInterProceduralRecursiveClassAttribute3Definition() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE3_DEFINITION, new int[]{6}, new int[]{7}, true);
        String cp = "target/test-classes";
        return new SVFAInterProcedural(cp, definition);
    }

    public SVFAInterProcedural configureInterProceduralRecursiveClassAttribute4Definition() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE4_DEFINITION, new int[]{6}, new int[]{7}, true);
        String cp = "target/test-classes";
        return new SVFAInterProcedural(cp, definition);
    }

    public SVFAInterProcedural configureInterProceduralRecursiveClassAttribute5Definition() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE5_DEFINITION, new int[]{10}, new int[]{13}, true);
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
    public void testSVFAnalysisInterProceduralRecursiveDefinition() {
        SVFAAnalysis analysis = configureInterProceduralRecursiveDefinition();
        analysis.buildSparseValueFlowGraph();
        Assert.assertEquals(2, analysis.reportConflicts().size());
    }

    @Test
    public void testSVFAnalysisInterProceduralRecursiveMethodParamDefinition() {
        SVFAAnalysis analysis = configureInterProceduralRecursiveMethodParamDefinition();
        analysis.buildSparseValueFlowGraph();
        Assert.assertEquals(1, analysis.reportConflicts().size());
    }

    @Test
    public void testSVFAnalysisInterProceduralRecursiveMethodParam2Definition() {
        SVFAAnalysis analysis = configureInterProceduralRecursiveMethodParam2Definition();
        analysis.buildSparseValueFlowGraph();
        Assert.assertTrue(analysis.reportConflicts().size()>0);
    }

    @Test
    public void testSVFAnalysisInterProceduralRecursiveMethodParam3Definition() {
        SVFAAnalysis analysis = configureInterProceduralRecursiveMethodParam3Definition();
        analysis.buildSparseValueFlowGraph();
        Assert.assertTrue(analysis.reportConflicts().size()>0);
    }

    @Test
    public void testSVFAnalysisInterProceduralRecursiveClassAttributeDefinition() {
        SVFAAnalysis analysis = configureInterProceduralRecursiveClassAttributeDefinition();
        analysis.buildSparseValueFlowGraph();
        Assert.assertTrue(analysis.reportConflicts().size()>0);
    }

    @Test
    public void testSVFAnalysisInterProceduralRecursiveClassAttribute2Definition() {
        SVFAAnalysis analysis = configureInterProceduralRecursiveClassAttribute2Definition();
        analysis.buildSparseValueFlowGraph();
        Assert.assertTrue(analysis.reportConflicts().size()>0);
    }

    @Test
    public void testSVFAnalysisInterProceduralRecursiveClassAttribute3Definition() {
        SVFAAnalysis analysis = configureInterProceduralRecursiveClassAttribute3Definition();
        analysis.buildSparseValueFlowGraph();
        Assert.assertTrue(analysis.reportConflicts().size()>0);
    }

    @Test
    public void testSVFAnalysisInterProceduralRecursiveClassAttribute4Definition() {
        SVFAAnalysis analysis = configureInterProceduralRecursiveClassAttribute4Definition();
        analysis.buildSparseValueFlowGraph();
        Assert.assertTrue(analysis.reportConflicts().size()>0);
    }

    @Test
    public void testSVFAnalysisInterProceduralRecursiveClassAttribute5Definition() {
        SVFAAnalysis analysis = configureInterProceduralRecursiveClassAttribute5Definition();
        analysis.buildSparseValueFlowGraph();
        Assert.assertTrue(analysis.reportConflicts().size()>0);
    }
}
