package br.unb.cic.analysis.svfa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.ufpe.cin.soot.graph.Graph;
import br.ufpe.cin.soot.graph.LambdaNode;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class SVFATest {

    public static final String CLASS_NAME_INTRAPROCEDURAL = "br.unb.cic.analysis.samples.IntraproceduralDataFlow";
    public static final String CLASS_NAME_INTERPROCEDURAL = "br.unb.cic.analysis.samples.InterproceduralTestCaseSameClass";
    public static final String CLASS_NAME_RECURSIVE_DEFINITION_01 = "br.unb.cic.analysis.samples.RecursiveDefinitionSample01";
    public static final String CLASS_NAME_RECURSIVE_DEFINITION_02 = "br.unb.cic.analysis.samples.RecursiveDefinitionSample02";
    public static final String CLASS_NAME_RECURSIVE_DEFINITION_03 = "br.unb.cic.analysis.samples.RecursiveDefinitionSample03";

    public static final String CLASS_NAME_INTRAPROCEDURAL_FIELD_SAMPLE = "br.unb.cic.analysis.samples.SVFAIntraproceduralFieldSample";

    public static final String CLASS_NAME_RECURSIVE_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionSample";
    public static final String CLASS_NAME_RECURSIVE_METHOD_PARAM_ONE_CONFLICT_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionMethodParamOneConflictSample";
    public static final String CLASS_NAME_RECURSIVE_METHOD_PARAM_TWO_CONFLICTS_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionMethodParamTwoConflictsSample";
    public static final String CLASS_NAME_RECURSIVE_METHOD_PARAM_TWO_CONFLICTS2_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionMethodParamTwoConflicts2Sample";
    public static final String CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_ONE_CONFLICT_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionClassAttributeOneConflictSample";
    public static final String CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_ONE_CONFLICT2_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionClassAttributeOneConflict2Sample";
    public static final String CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_ONE_CONFLICT3_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionClassAttributeOneConflict3Sample";
    public static final String CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_ONE_CONFLICT_TWO_LEVELS_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionClassAttributeOneConflictTwoLevelsSample";
    public static final String CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_ONE_CONFLICT_TWO_LEVELS2_DEFINITION = "br.unb.cic.analysis.samples.RecursiveDefinitionClassAttributeOneConflictTwoLevels2Sample";

    public SVFAInterProcedural configureInterTest(String classpath, int[] leftchangedlines, int[] rightchangedlines) {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(classpath, leftchangedlines, rightchangedlines);
        String cp = "target/test-classes";
        return new SVFAInterProcedural(cp, definition);
    }

    public SVFAIntraProcedural configureIntraTest(String classpath, int[] leftchangedlines, int[] rightchangedlines) {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(classpath, leftchangedlines, rightchangedlines);
        String cp = "target/test-classes";
        return new SVFAIntraProcedural(cp, definition);
    }


    public SVFAInterProcedural configureInterProceduralDifferentMethod() {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(CLASS_NAME_INTERPROCEDURAL, new int[]{9}, new int[]{19});
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

    public SVFAInterProcedural configureRecursiveTest(String classpath, int[] leftchangedlines, int[] rightchangedlines, boolean recursive) {
        AbstractMergeConflictDefinition definition = DefinitionFactory.definition(classpath, leftchangedlines, rightchangedlines, recursive);
        String cp = "target/test-classes";
        return new SVFAInterProcedural(cp, definition);
    }

    @Test
    public void testSVFAnalysisInterProcedural() {
        SVFAAnalysis analysis = configureInterTest(CLASS_NAME_INTRAPROCEDURAL, new int[]{6}, new int[]{11});
        analysis.buildSparseValueFlowGraph();
        Graph g = analysis.svg();
        Assert.assertEquals(5, g.nodes().size());
        Assert.assertEquals(1, analysis.reportConflicts().size());
        Assert.assertEquals(1, analysis.findSourceSinkPaths().size());
    }

    @Test
    public void testSVFAnalysisInterProcedural2() {
        SVFAAnalysis analysis = configureInterTest(CLASS_NAME_INTERPROCEDURAL, new int[]{9}, new int[]{19});
        analysis.buildSparseValueFlowGraph();
        for (List<LambdaNode> paths : analysis.findSourceSinkPaths()) {
            System.out.println(String.join("-> ", paths.stream().map(n -> n.show()).collect(Collectors.toList())));
        }
        Assert.assertEquals(1, analysis.reportConflicts().size());
    }

    @Test
    public void testSVFAnalysisIntraProceduralSameMethod() {
        SVFAAnalysis analysis = configureIntraTest(CLASS_NAME_INTRAPROCEDURAL, new int[]{6}, new int[]{11});
        analysis.buildSparseValueFlowGraph();
        Assert.assertEquals(1, analysis.reportConflicts().size());
    }

    @Test
    public void testSVFAnalysisIntraProceduralDifferentMethod() {
        SVFAAnalysis analysis = configureIntraTest(CLASS_NAME_INTERPROCEDURAL, new int[]{9}, new int[]{19});
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

//    @Test
//    public void testSVFAnalysisInterProceduralRecursiveDefinition03() {
//        SVFAAnalysis analysis = configureInterProceduralRecursiveDefinition03();
//        analysis.buildSparseValueFlowGraph();
//        Assert.assertEquals(1, analysis.reportConflicts().size());
//    }


    @Test
    public void testSVFAnalysisInterProceduralRecursiveMethodParamOneConflictDefinition() {
        SVFAAnalysis analysis = configureRecursiveTest(CLASS_NAME_RECURSIVE_METHOD_PARAM_ONE_CONFLICT_DEFINITION, new int[]{5}, new int[]{10}, true);
        analysis.buildSparseValueFlowGraph();
        Assert.assertEquals(1, analysis.reportConflicts().size());
        System.out.println(analysis.reportConflicts().size());
    }

    @Test
    public void testSVFAnalysisInterProceduralRecursiveMethodParamTwoConflicts2Definition() {
        SVFAAnalysis analysis = configureRecursiveTest(CLASS_NAME_RECURSIVE_METHOD_PARAM_TWO_CONFLICTS2_DEFINITION, new int[]{5}, new int[]{6}, true);
        analysis.buildSparseValueFlowGraph();
        Assert.assertTrue(analysis.reportConflicts().size()>0);
    }

    @Test
    public void testSVFAnalysisInterProceduralRecursiveMethodParamTwoConflictsDefinition() {
        SVFAAnalysis analysis = configureRecursiveTest(CLASS_NAME_RECURSIVE_METHOD_PARAM_TWO_CONFLICTS_DEFINITION, new int[]{5}, new int[]{6, 10}, true);
        analysis.buildSparseValueFlowGraph();
        Assert.assertTrue(analysis.reportConflicts().size()>0);
    }

    @Ignore
    public void testSVFAnalysisInterProceduralRecursiveClassAttributeOneConflictDefinition() {
        SVFAAnalysis analysis = configureRecursiveTest(CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_ONE_CONFLICT_DEFINITION, new int[]{6}, new int[]{7}, true);
        analysis.buildSparseValueFlowGraph();
        Assert.assertTrue(analysis.reportConflicts().size()>0);
    }

    @Test
    public void testSVFAnalysisIntraProceduralFieldOneConflictDefinition() {
        SVFAAnalysis analysis = configureRecursiveTest(CLASS_NAME_INTRAPROCEDURAL_FIELD_SAMPLE, new int[]{7}, new int[]{8}, false);
        analysis.buildSparseValueFlowGraph();
        Assert.assertTrue(analysis.reportConflicts().size()>0);
    }
//
//    @Test
//    public void testSVFAnalysisInterProceduralRecursiveClassAttributeOneConflict2Definition() {
//        SVFAAnalysis analysis = configureRecursiveTest(CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_ONE_CONFLICT2_DEFINITION, new int[]{6}, new int[]{7}, true);
//        analysis.buildSparseValueFlowGraph();
//        Assert.assertTrue(analysis.reportConflicts().size()>0);
//    }
//
//    @Test
//    public void testSVFAnalysisInterProceduralRecursiveClassAttributeOneConflict3Definition() {
//        SVFAAnalysis analysis = configureRecursiveTest(CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_ONE_CONFLICT3_DEFINITION, new int[]{6}, new int[]{7}, true);
//        analysis.buildSparseValueFlowGraph();
//        Assert.assertTrue(analysis.reportConflicts().size()>0);
//    }
//
//    @Test
//    public void testSVFAnalysisInterProceduralRecursiveClassAttributeOneConflictTwoLevelsDefinition() {
//        SVFAAnalysis analysis = configureRecursiveTest(CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_ONE_CONFLICT_TWO_LEVELS_DEFINITION, new int[]{6}, new int[]{7}, true);
//        analysis.buildSparseValueFlowGraph();
//        Assert.assertTrue(analysis.reportConflicts().size()>0);
//    }
//
//    @Test
//    public void testSVFAnalysisInterProceduralRecursiveClassAttributeOneConflictTwoLevels2Definition() {
//        SVFAAnalysis analysis = configureRecursiveTest(CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_ONE_CONFLICT_TWO_LEVELS2_DEFINITION, new int[]{10}, new int[]{13}, true);
//        analysis.buildSparseValueFlowGraph();
//        Assert.assertTrue(analysis.reportConflicts().size()>0);
//    }


    public void testSVFAnalysisInterProceduralRecursiveClassAttributeOneConflict2Definition() {
        SVFAAnalysis analysis = configureRecursiveTest(CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_ONE_CONFLICT2_DEFINITION, new int[]{6}, new int[]{7}, true);
        analysis.buildSparseValueFlowGraph();
        Assert.assertTrue(analysis.reportConflicts().size()>0);
    }

    @Ignore
    public void testSVFAnalysisInterProceduralRecursiveClassAttributeOneConflict3Definition() {
        SVFAAnalysis analysis = configureRecursiveTest(CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_ONE_CONFLICT3_DEFINITION, new int[]{6}, new int[]{7}, true);
        analysis.buildSparseValueFlowGraph();
        Assert.assertTrue(analysis.reportConflicts().size()>0);
    }

    @Ignore
    public void testSVFAnalysisInterProceduralRecursiveClassAttributeOneConflictTwoLevelsDefinition() {
        SVFAAnalysis analysis = configureRecursiveTest(CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_ONE_CONFLICT_TWO_LEVELS_DEFINITION, new int[]{6}, new int[]{7}, true);
        analysis.buildSparseValueFlowGraph();
        Assert.assertTrue(analysis.reportConflicts().size()>0);
    }

    @Ignore
    public void testSVFAnalysisInterProceduralRecursiveClassAttributeOneConflictTwoLevels2Definition() {
        SVFAAnalysis analysis = configureRecursiveTest(CLASS_NAME_RECURSIVE_CLASS_ATTRIBUTE_ONE_CONFLICT_TWO_LEVELS2_DEFINITION, new int[]{10}, new int[]{13}, true);
        analysis.buildSparseValueFlowGraph();
        Assert.assertTrue(analysis.reportConflicts().size()>0);
    }
}
