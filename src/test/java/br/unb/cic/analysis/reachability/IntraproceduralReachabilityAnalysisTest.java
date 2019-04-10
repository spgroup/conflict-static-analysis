package br.unb.cic.analysis.reachability;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import soot.G;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;

import java.util.*;

public class IntraproceduralReachabilityAnalysisTest {
	
	private ReachabilityAnalysis intraprocedural;

	@Before
	public void configure() {
		G.reset();
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
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.intraprocedural", intraprocedural));
		Options.v().setPhaseOption("cg.spark", "on");
		Options.v().setPhaseOption("cg.spark", "verbose:true");
		String testClasses = "/Users/rbonifacio/tmp/test-classes/";
		soot.Main.main(new String[] {"-w", "-allow-phantom-refs", "-f", "J", "-keep-line-number", "-process-dir", testClasses});
	}

	@Test
	public void testReachability() {
		Assert.assertNotNull(intraprocedural.getConflicts());
		Assert.assertEquals(1, intraprocedural.getConflicts().size());
	}

}
