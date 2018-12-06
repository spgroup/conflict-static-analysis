package br.unb.cic.analysis.reachability;

import java.util.ArrayList;
import java.util.List;

import br.unb.cic.analysis.model.Pair;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.analysis.reachability.ReachabilityAnalysis;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import soot.PackManager;
import soot.Transform;

public class ReachabilityAnalysisTest {
	
	private ReachabilityAnalysis analysis;

//	//@Test
//	public void testIntraProcedural() {
//		PackManager.v().getPack("jtp").add(
//			    new Transform("jtp.myTransform", new BodyTransformer() {
//					@Override
//					protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
//						Set<Integer> source = new HashSet<>();
//						Set<Integer> sink = new HashSet<>();
//						
//						source.add(6);
//						sink.add(12);
//						new MergeConflictAnalysis(new ExceptionalUnitGraph(body), source, sink);
//					}
//			    }));
//		
//		soot.Main.main(new String[] {"-w", "-allow-phantom-refs", "-f", "J", "-keep-line-number", "-process-dir", "/Users/rbonifacio/Documents/workspace-java/SootAnalysisTestCase/target/classes/"});
//	}

	@Before
	public void configure() {
		analysis = new ReachabilityAnalysis() {
			@Override
			protected List<Pair<String, List<Integer>>> sourceDefinitions() {
				List<Pair<String, List<Integer>>> res = new ArrayList<>();
				List<Integer> lines = new ArrayList<>();
				lines.add(9);
				res.add(new Pair("br.unb.cic.analysis.samples.InterproceduralTestCase", lines));
				return res;
			}
			@Override
			protected List<Pair<String, List<Integer>>> sinkDefinitions() {
				List<Pair<String, List<Integer>>> res = new ArrayList<>();
				List<Integer> lines = new ArrayList<>();
				lines.add(19);
				lines.add(21);

				res.add(new Pair("br.unb.cic.analysis.samples.InterproceduralTestCase", lines));
				return res;
			}
		};
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.reachability", analysis));
		soot.Main.main(new String[] {"-w", "-allow-phantom-refs", "-f", "J", "-keep-line-number", "-process-dir", "target/test-classes/"});
	}

	@Test
	public void testReachability() {
		Assert.assertNotNull(analysis.getPaths());
		Assert.assertEquals(2, analysis.getPaths().size());
		for(GraphPath<Statement, DefaultEdge> path: analysis.getPaths()) {
			System.out.println(path);
		}
	}
	


}
