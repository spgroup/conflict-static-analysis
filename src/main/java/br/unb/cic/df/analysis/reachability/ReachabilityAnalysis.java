package br.unb.cic.df.analysis.reachability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
	private Graph<Statement, DefaultEdge> flowGraph;
	List<GraphPath<Statement, DefaultEdge>> paths;

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
		paths = new ArrayList<>();
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

		DijkstraShortestPath<Statement, DefaultEdge> dijkstra = new DijkstraShortestPath<>(flowGraph);

		for(Statement source : sourceStatements) {
			for(Statement sink : sinkStatements) {
				ShortestPathAlgorithm.SingleSourcePaths<Statement, DefaultEdge> unitPath = dijkstra.getPaths(source);

				GraphPath<Statement, DefaultEdge> path = unitPath.getPath(sink);
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
	public List<GraphPath<Statement, DefaultEdge>> getPaths() {
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

			buildInterproceduralFlowGraph(cfg, s, 0);
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
	private void buildInterproceduralFlowGraph(final JimpleBasedInterproceduralCFG cfg, Statement s, int currentLevel) {
		if(currentLevel > maxDepth) {// || flowGraph.vertexSet().contains(s)) {
			return;
		}
		flowGraph.addVertex(s);
		if(s.getUnit() instanceof InvokeStmt) {
			InvokeStmt invokeStmt = (InvokeStmt)s.getUnit();
			SootMethod targetMethod = invokeStmt.getInvokeExpr().getMethod();
			cfg.initializeUnitToOwner(targetMethod);
			if(targetMethod.hasActiveBody()) {
				Unit firstUnitOfTargetMethod = targetMethod.getActiveBody().getUnits().getFirst();
				Statement firstStatementOfTargetMethod = createNextStatement(targetMethod.getDeclaringClass(), targetMethod, firstUnitOfTargetMethod);
				buildInterproceduralFlowGraph(cfg, firstStatementOfTargetMethod, currentLevel + 1);
				flowGraph.addEdge(s, firstStatementOfTargetMethod);
			}
		}

		List<Unit> nextUnities = cfg.getSuccsOf(s.getUnit());

		for(Unit next : nextUnities) {
			Statement nextStatement = createNextStatement(s.getSootClass(), s.getSootMethod(), next);
			buildInterproceduralFlowGraph(cfg, nextStatement, currentLevel);
			flowGraph.addEdge(s, nextStatement);
		};
	}

	private Statement createNextStatement(SootClass c, SootMethod m, Unit u) {
		Statement s = Statement.builder().setClass(c).setMethod(m).setUnit(u).build();
		return getExistingSinkNode(s);
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
	 * load the source statements using a template method.
	 */
	private void loadSourceStatements() {
		loadStatements(sourceDefinitions(), sourceStatements, Type.SOURCE);
	}

	/*
	 * load the sink statements using a template method.
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
						Statement stmt = Statement.builder().setClass(c).setMethod(m).setUnit(u).setType(type).build();
						statements.add(stmt);
					}
				}
			}
		}
	}

	private Statement getExistingSinkNode(Statement s) {
		for(Statement sink: sinkStatements) {
			if(sink.getUnit().equals(s.getUnit())) {
				return sink;
			}
		}
		return s;
	}
}
