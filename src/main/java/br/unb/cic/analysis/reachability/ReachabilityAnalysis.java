package br.unb.cic.analysis.reachability;

import java.util.*;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Statement;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
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
public class ReachabilityAnalysis extends SceneTransformer {
	private AbstractMergeConflictDefinition definition;
	private int maxDepth;
	private Graph<Statement, DefaultEdge> flowGraph;
	List<GraphPath<Statement, DefaultEdge>> paths;

	/**
	 * Default constuctor using the max indirection
	 * of method calls == 1.
	 */
	public ReachabilityAnalysis(AbstractMergeConflictDefinition definition) {
		this(1, definition);
	}

	/**
	 * Reachability analysis constructor.
	 * @param maxDepth the max indirection of method calls.
	 */
	public ReachabilityAnalysis(int maxDepth, AbstractMergeConflictDefinition definition) {
		this.definition = definition;
		this.maxDepth = maxDepth;
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
		definition.loadSourceStatements();
		definition.loadSinkStatements();
		buildInterproceduralFlowGraph();

		DijkstraShortestPath<Statement, DefaultEdge> dijkstra = new DijkstraShortestPath<>(flowGraph);

		for(Statement source : definition.getSourceStatements()) {
			for(Statement sink : definition.getSinkStatements()) {
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
		for(Statement s: definition.getSourceStatements()) {
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
		if(currentLevel > maxDepth || flowGraph.vertexSet().contains(s)) {
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
		}
	}

	private Statement createNextStatement(SootClass c, SootMethod m, Unit u) {
		Statement s = Statement.builder().setClass(c).setMethod(m).setUnit(u).build();
		return definition.getExistingSinkNode(s);
	}

}
