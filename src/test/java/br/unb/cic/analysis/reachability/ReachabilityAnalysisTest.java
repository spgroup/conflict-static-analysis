package br.unb.cic.analysis.reachability;

import java.util.*;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.df.Collector;
import br.unb.cic.analysis.model.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import soot.G;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;

public class ReachabilityAnalysisTest {
	
	private ReachabilityAnalysis interproceduralSameClass;
	private ReachabilityAnalysis interproceduralDifferentClasses;
	private ReachabilityAnalysis intraprocedural;

	@Before
	public void configure() {
		G.reset();
		Collector.instance().clear();
		interproceduralSameClass = new ReachabilityAnalysis(new AbstractMergeConflictDefinition() {
			@Override
			protected Map<String, List<Integer>> sourceDefinitions() {
				Map<String, List<Integer>> res = new HashMap<>();
				List<Integer> lines = new ArrayList<>();
				lines.add(9);
				res.put("br.unb.cic.analysis.samples.InterproceduralTestCaseSameClass", lines);
				return res;
			}
			@Override
			protected Map<String, List<Integer>> sinkDefinitions() {
				Map<String, List<Integer>> res = new HashMap<>();
				List<Integer> lines = new ArrayList<>();
				lines.add(19);
				lines.add(21);

				res.put("br.unb.cic.analysis.samples.InterproceduralTestCaseSameClass", lines);
				return res;
			}
		});

//		interproceduralDifferentClasses = new ReachabilityAnalysis(new AbstractMergeConflictDefinition() {
//			@Override
//			protected List<Pair<String, List<Integer>>> sourceDefinitions() {
//				List<Pair<String, List<Integer>>> res = new ArrayList<>();
//				List<Integer> lines = new ArrayList<>();
//				lines.add(9);
//				res.add(new Pair("br.unb.cic.analysis.samples.InterproceduralTestCaseDifferentClasses", lines));
//				return res;
//			}
//			@Override
//			protected List<Pair<String, List<Integer>>> sinkDefinitions() {
//				List<Pair<String, List<Integer>>> res = new ArrayList<>();
//				List<Integer> lines = new ArrayList<>();
//				lines.add(26);
//
//				res.add(new Pair("br.unb.cic.analysis.samples.NotRelevant", lines));
//				return res;
//			}
//		});

		intraprocedural = new ReachabilityAnalysis(new AbstractMergeConflictDefinition() {
			@Override
			protected Map<String, List<Integer>> sourceDefinitions() {
				Map<String, List<Integer>> res = new HashMap<>();
				List<Integer> lines = Arrays.asList(new Integer[]{23});
				res.put("br.unb.cic.analysis.samples.BillingSystem", lines);
				return res;
			}

			@Override
			protected Map<String, List<Integer>> sinkDefinitions() {
				Map<String, List<Integer>> res = new HashMap<>();
				List<Integer> lines = Arrays.asList(new Integer[]{26});
				res.put("br.unb.cic.analysis.samples.BillingSystem", lines);
				return res;
			}
		});
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.analysis", interproceduralSameClass));
		//PackManager.v().getPack("wjtp").add(new Transform("wjtp.interproceduralDifferentClasses", interproceduralDifferentClasses));
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.intraprocedural", intraprocedural));
		Options.v().setPhaseOption("cg.spark", "on");
		Options.v().setPhaseOption("cg.spark", "verbose:true");
		String testClasses = "/Users/rbonifacio/tmp/test-classes/";
		soot.Main.main(new String[] {"-w", "-allow-phantom-refs", "-f", "J", "-keep-line-number", "-process-dir", testClasses});
	}

	@Test
	public void testReachability() {
		Assert.assertNotNull(interproceduralSameClass.getConflicts());
		Assert.assertEquals(2, interproceduralSameClass.getConflicts().size());
		Assert.assertNotNull(intraprocedural.getConflicts());
		Assert.assertEquals(1, intraprocedural.getConflicts().size());
//		Assert.assertNotNull(interproceduralDifferentClasses.getPaths());
//		Assert.assertEquals(1, interproceduralDifferentClasses.getPaths().size());
	}

}
