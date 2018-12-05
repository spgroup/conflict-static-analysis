package br.unb.cic.df.analysis.reachability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.unb.cic.df.analysis.model.Pair;
import br.unb.cic.df.analysis.model.Statement;
import br.unb.cic.df.analysis.model.Statement.Type;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;


/**
 * A generic reach-ability analysis from
 * source statements to sink statements. Note that
 * this is not a real "taint analysis", because we are
 * considering paths that go beyond def-use relationships.
 *
 * @author rbonifacio
 */
public abstract class ReachabilityAnalysis extends SceneTransformer {
	protected List<Statement> sourceStatements;
	protected List<Statement> sinkStatements;
	private int maxDepth;
	private Graph<Unit, DefaultEdge> flowGraph;
	List<GraphPath<Unit, DefaultEdge>> paths;

	/**
	 * Default constuctor using the max indirection
	 * of method calls == 1.
	 */
	public ReachabilityAnalysis() {
		this(1);
	}

	/**
	 * Reachability analysis constructor.
	 * @param maxDepth the max indirection of method calls.
	 */
	public ReachabilityAnalysis(int maxDepth) {
		this.maxDepth = maxDepth;
		sourceStatements = new ArrayList<>();
		sinkStatements = new ArrayList<>();
		flowGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
	}

	/**
	 * The analysis workflow. This method implements an
	 * extension point from the Soot Framework.
	 *
	 * @param phaseName the phase that this Soot transform works
	 * @param options the soot execution options
	 */
	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		loadSourceStatements();
		loadSinkStatements();
		buildInterproceduralFlowGraph();

		DijkstraShortestPath<Unit, DefaultEdge> dijkstra = new DijkstraShortestPath<>(flowGraph);

		for(Unit source : sourceStatements.stream().map(s -> s.getUnit()).collect(Collectors.toList())) {
			for(Unit sink : sinkStatements.stream().map(s -> s.getUnit()).collect(Collectors.toList())) {
				ShortestPathAlgorithm.SingleSourcePaths<Unit, DefaultEdge> unitPath = dijkstra.getPaths(source);

				GraphPath<Unit, DefaultEdge> path = unitPath.getPath(sink);
				if(path != null) {
					this.paths.add(path);
				}
			}
		}
	}

	/**
	 * Returns the list of paths from sources to sinks.
	 * @return paths from source statements to sink statements.
	 */
	public List<GraphPath<Unit, DefaultEdge>> getPaths() {
		return paths;
	}

	/*
	 * This is just the "entry point" of our inter-procedural
	 * reach-ability analysis.
	 */
	private void buildInterproceduralFlowGraph() {
		JimpleBasedInterproceduralCFG cfg = new JimpleBasedInterproceduralCFG();
		for(Statement s: sourceStatements) {
			cfg.initializeUnitToOwner(s.getSootMethod());

			flowGraph.addVertex(s.getUnit());

			buildInterproceduralFlowGraph(cfg, s.getUnit(), 0);
		}
	}

	/*
	 * this is the "brain" method of the inter-procedural reach-ability
	 * analysis algorithm. It populates a graph based on the statements
	 * that might be reached from a given unit u (considering
	 * method calls).
	 *
	 * @param cfg the Soot control flow graph
	 * @param u the current unit
	 * @param currentLevel the current indirection level (related to invoke statements). we
	 * don't want to go deeper than maxDepth.
	 */
	private void buildInterproceduralFlowGraph(final JimpleBasedInterproceduralCFG cfg, Unit u, int currentLevel) {
		if(currentLevel > maxDepth || flowGraph.vertexSet().contains(u)) {
			return;
		}

		if(u instanceof InvokeStmt) {
			Collection<SootMethod> methods = cfg.getCalleesOfCallAt(u);

			methods.forEach(targetMethod -> {
				cfg.initializeUnitToOwner(targetMethod);
				Unit firstStatementOfTargetMethod = targetMethod.getActiveBody().getUnits().getFirst();
				flowGraph.addVertex(firstStatementOfTargetMethod);
				flowGraph.addEdge(u, firstStatementOfTargetMethod);
				buildInterproceduralFlowGraph(cfg, firstStatementOfTargetMethod, currentLevel + 1);
			});
		}

		List<Unit> nextUnities = cfg.getSuccsOf(u);

		nextUnities.forEach(next -> {
			flowGraph.addVertex(next);
			flowGraph.addEdge(u, next);
			buildInterproceduralFlowGraph(cfg, next, currentLevel);
		});
	}

	/**
	 * This method should return a list of pairs, where the
	 * first element is the full qualified name of
	 * a class and the second element is a list of integers
	 * stating the lines of code where does exist a "source"
	 * statement.
	 */
	protected abstract List<Pair<String, List<Integer>>> sourceDefinitions();

	/**
	 * This method should return a list of pairs, where the
	 * first element is the the full qualified name of
	 * a class and the second element is a list of integers
	 * stating the lines of code where does exist a "sink"
	 * statement.
	 */
	protected abstract List<Pair<String, List<Integer>>> sinkDefinitions();

	/*
	 * load the source statements using a template method
	 * approach.
	 */
	private void loadSourceStatements() {
		loadStatements(sourceDefinitions(), sourceStatements, Type.SOURCE);
	}

	/*
	 * load the sink statements using a template method
	 * statements.
	 */
	private void loadSinkStatements() {
		loadStatements(sinkDefinitions(), sinkStatements, Type.SINK);
	}

	/*
	 * just an auxiliary method to load the statements
	 * related to either the source elements or sink
	 * elements. this method only exists because it
	 * avoids some duplicated code that might arise on
	 * loadSourceStatements and loadSinkStatements.
	 */
	private void loadStatements(List<Pair<String, List<Integer>>> definitions,  List<Statement> statements, Type type) {
		for(Pair<String, List<Integer>> pair: definitions) {
			SootClass c = Scene.v().getSootClass(pair.getFirst());
			if(c == null) continue; 	
			for(SootMethod m: c.getMethods()) {
				for(Unit u: m.getActiveBody().getUnits()) {
					if(pair.getSecond().contains(u.getJavaSourceStartLineNumber())) {
						Statement stmt = new Statement(c, m, u, type);
						statements.add(stmt);
					}
				}
			}
		}
	}
}
