package br.unb.cic.analysis.reachability;

import java.util.*;

import br.unb.cic.analysis.AbstractAnalysis;
import br.unb.cic.analysis.model.Conflict;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import soot.*;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Statement;


/**
 * A generic reach-ability analysis from
 * source statements to sink statements. Note that
 * this is not a real "taint analysis", because we are
 * considering paths that go beyond def-use relationships.
 *
 * @author rbonifacio
 */
public class ReachabilityAnalysis extends SceneTransformer implements AbstractAnalysis {
	private int maxDepth;
	private AbstractMergeConflictDefinition definition;
	private Graph<Statement, DefaultEdge> flowGraph;
	private Set<Conflict> conflicts;

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
		this.flowGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
		this.conflicts = new HashSet<>();
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
		this.definition.loadSourceStatements();
		this.definition.loadSinkStatements();

		buildInterproceduralFlowGraph();

		ConnectivityInspector<Statement, DefaultEdge> inspector = new ConnectivityInspector<>(flowGraph);

		for(Statement source : definition.getSourceStatements()) {
			for (Statement sink : definition.getSinkStatements()) {
				if(inspector.pathExists(source, sink)) {
					conflicts.add(new Conflict(source, sink));
				}
			}
		}

	}

	@Override
	public void clear() {
		conflicts.clear();
	}

	@Override
	public Set<Conflict> getConflicts() {
		return conflicts;
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
	 * this is the "core" method of the inter-procedural reachability
	 * analysis algorithm. It populates a graph based on the statements
	 * that might be reached from a given unit u (considering
	 * method calls).
	 *
	 * @param cfg the Soot control flow graph
	 * @param u the current unit
	 * @param currentLevel the current indirection level (related to invoke statements). we
	 * don't want to go deeper than maxDepth. We also want to avoid infinite loops.
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
