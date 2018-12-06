package br.unb.cic.df;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.unb.cic.df.analysis.model.Pair;
import br.unb.cic.df.analysis.model.Statement;
import br.unb.cic.df.analysis.reachability.ReachabilityAnalysis;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Assert;
import org.junit.Test;

import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.toolkits.graph.DirectedGraph;

public class MergeConflictAnalysisTest {
	
	private String targetClassName = "br.unb.cic.InterproceduralTestCase";
	private int source = 8; 
	private int sink = 20;
	private List<Unit> sources = new ArrayList<>(); 
	private List<Unit> sinks = new ArrayList<>();
	private List<SootMethod> methods = new ArrayList<>();

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

	@Test
	public void testReachability() {
		ReachabilityAnalysis analysis =  new ReachabilityAnalysis() {
			@Override
			protected List<Pair<String, List<Integer>>> sourceDefinitions() {
				List<Pair<String, List<Integer>>> res = new ArrayList<>();
				List<Integer> lines = new ArrayList<>();
				lines.add(8);
				res.add(new Pair("br.unb.cic.InterproceduralTestCase", lines));
				return res;
			}
			@Override
			protected List<Pair<String, List<Integer>>> sinkDefinitions() {
				List<Pair<String, List<Integer>>> res = new ArrayList<>();
				List<Integer> lines = new ArrayList<>();
				lines.add(20);
				res.add(new Pair("br.unb.cic.InterproceduralTestCase", lines));
				return res;
			}
		};

		PackManager.v().getPack("wjtp").add(new Transform("wjtp.reachability", analysis));
		soot.Main.main(new String[] {"-w", "-allow-phantom-refs", "-f", "J", "-keep-line-number", "-process-dir", "/Users/rbonifacio/Documents/workspace-java/SootAnalysisTestCase/target/classes/"});

		Assert.assertNotNull(analysis.getPaths());
		Assert.assertEquals(1, analysis.getPaths().size());

		for(GraphPath<Statement, DefaultEdge> path: analysis.getPaths()) {
			System.out.println(path);
		}
	}
	
	//@Test
	public void testInterProcedural() {
		PackManager.v().getPack("wjtp").add(
			    new Transform("wjtp.myTransform", new SceneTransformer() {					
					@Override
					protected void internalTransform(String phaseName, Map<String, String> options) {
						JimpleBasedInterproceduralCFG cfg = new JimpleBasedInterproceduralCFG();
						anotateSourceCode(cfg);
						for(Unit s1: sources) {
							
							if(s1.getJavaSourceStartLineNumber() == source) {
								List<Unit> successors = cfg.getSuccsOf(s1);
								
								System.out.println("Source: " + s1);
								System.out.println("Method: " + cfg.getMethodOf(s1));
								DirectedGraph<Unit> ecfg = cfg.getOrCreateUnitGraph(cfg.getMethodOf(s1));
								System.out.println("Successors: " + ecfg.getSuccsOf(s1).size());
							}
							
							for(Unit s2: sinks) {
								if(cfg.getSuccsOf(s1).contains(s2)) {
									System.out.println("conflito");
								}								
							}
						}
					}

			    }));
		soot.Main.main(new String[] {"-w", "-allow-phantom-refs", "-f", "J", "-keep-line-number", "-process-dir", "/Users/rbonifacio/Documents/workspace-java/SootAnalysisTestCase/target/classes/"});
	}
	
	private void anotateSourceCode(JimpleBasedInterproceduralCFG cfg) {
		for(SootClass c: Scene.v().getApplicationClasses()) {
			boolean method = false;
			if(c.getName().equals(targetClassName)) {
				for(SootMethod m: c.getMethods()) {
					System.out.print("Method under analysis" + m);
					method = false;
					for(Unit u: m.getActiveBody().getUnits()) {
						System.out.print("Unit under analysis" + u);
						System.out.println("Unit start line " + u.getJavaSourceStartLineNumber());
						if(u.getJavaSourceStartLineNumber() == source) {
							sources.add(u);
							if(!method) {
								cfg.initializeUnitToOwner(m);
								cfg.getOrCreateUnitGraph(m);
							}
						}
						else if(u.getJavaSourceStartLineNumber() == sink) {
							sinks.add(u);
							if(!method) {
								cfg.getOrCreateUnitGraph(m);
							}
						}
					}
				}
			}
		}
	}

}
