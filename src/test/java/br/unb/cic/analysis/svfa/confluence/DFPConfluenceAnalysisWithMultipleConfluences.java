package br.unb.cic.analysis.svfa.confluence;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class DFPConfluenceAnalysisWithMultipleConfluences {

    private DFPConfluenceAnalysis analysis;
    @Before
    public void configure() {
        AbstractMergeConflictDefinition definition = new AbstractMergeConflictDefinition() {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(9);
                lines.add(11);
                res.put("br.unb.cic.analysis.samples.MultipleConfluenceSample", lines);
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(13);
                res.put("br.unb.cic.analysis.samples.MultipleConfluenceSample", lines);
                return res;
            }
        };

        String cp = "target/test-classes";
        analysis = new DFPConfluenceAnalysis(cp, definition, true);
    }

    @Test
    public void testSVFAConfluentAnalysisExpectingNoConflicts() {
        analysis.execute(false);
        Set<ConfluenceConflict> conflicts = analysis.getConfluentConflicts();
        Assert.assertEquals(conflicts.size(), 2);

        Set<String> conflictsAsString = conflicts.stream().map(c -> c.toString()).collect(Collectors.toSet());
        String firstConflictResult = "SOURCE=>BASE: (br.unb.cic.analysis.samples.MultipleConfluenceSample,<br.unb.cic.analysis.samples.MultipleConfluenceSample: void multipleConfluencesToTheSameLine()>,11) => (br.unb.cic.analysis.samples.MultipleConfluenceSample,<br.unb.cic.analysis.samples.MultipleConfluenceSample: void multipleConfluencesToTheSameLine()>,18)\n" +
                "SINK=>BASE: (br.unb.cic.analysis.samples.MultipleConfluenceSample,<br.unb.cic.analysis.samples.MultipleConfluenceSample: void multipleConfluencesToTheSameLine()>,13) => (br.unb.cic.analysis.samples.MultipleConfluenceSample,<br.unb.cic.analysis.samples.MultipleConfluenceSample: void multipleConfluencesToTheSameLine()>,18)";
        Assert.assertTrue(conflictsAsString.contains(firstConflictResult));
        String secondConflictResult = "SOURCE=>BASE: (br.unb.cic.analysis.samples.MultipleConfluenceSample,<br.unb.cic.analysis.samples.MultipleConfluenceSample: void multipleConfluencesToTheSameLine()>,9) => (br.unb.cic.analysis.samples.MultipleConfluenceSample,<br.unb.cic.analysis.samples.MultipleConfluenceSample: void multipleConfluencesToTheSameLine()>,18)\n" +
                "SINK=>BASE: (br.unb.cic.analysis.samples.MultipleConfluenceSample,<br.unb.cic.analysis.samples.MultipleConfluenceSample: void multipleConfluencesToTheSameLine()>,13) => (br.unb.cic.analysis.samples.MultipleConfluenceSample,<br.unb.cic.analysis.samples.MultipleConfluenceSample: void multipleConfluencesToTheSameLine()>,18)";
        Assert.assertTrue(conflictsAsString.contains(secondConflictResult));

    }
}
