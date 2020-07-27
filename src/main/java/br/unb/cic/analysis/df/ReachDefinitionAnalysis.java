package br.unb.cic.analysis.df;

import java.util.*;
import java.util.stream.Collectors;

import br.unb.cic.analysis.AbstractAnalysis;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.Statement;
import soot.Body;
import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JInstanceFieldRef;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;

/**
 * Intraprocedural dataflow analysis for identifying
 * merge conflicts from source definitions to sink
 * usage scenarios. In this case we reduce this problem
 * to a def-use analysis.
 */
public class ReachDefinitionAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<DataFlowAbstraction>> implements AbstractAnalysis  {

	protected Body methodBody;
	private AbstractMergeConflictDefinition definition;
	private Set<Conflict> conflicts;

	/**
	 * Constructor of the DataFlowAnalysis class.
	 *
	 * According to the SOOT architecture, the constructor for a
	 * flow analysis must receive as an argument a graph, set up
	 * essential information and call the doAnalysis method of the
	 * super class.
	 */
	public ReachDefinitionAnalysis(Body methodBody, AbstractMergeConflictDefinition definition) {
		super(new ExceptionalUnitGraph(methodBody));
		this.methodBody = methodBody;
		this.definition = definition;
		this.conflicts = new HashSet<>();
		definition.loadSourceStatements();
		definition.loadSinkStatements();
		doAnalysis();
	}
	
	/**
	 * Runs the algorithm analysis at a given statement (Unit d). Here we
	 * manipulate and compute an out set from the income set in (foward analysis).
	 *
	 * @param in a set of abstractions that arrive at the statement d
	 * @param u a specific statement
	 * @out the result of applying the analysis considering the income abstraction and the statement d
	 */
	@Override
	protected void flowThrough(FlowSet<DataFlowAbstraction> in, Unit u, FlowSet<DataFlowAbstraction> out) {
		detectConflict(in, u);
		FlowSet<DataFlowAbstraction> temp = new ArraySparseSet<>();

		FlowSet<DataFlowAbstraction> killSet = new ArraySparseSet<>();
		FlowSet<Local> mustKill = kill(u);
		for(DataFlowAbstraction item : in) {
			if(mustKill.contains(item.getLocal())) {
				killSet.add(item);
			}
		}
		in.difference(killSet, temp);
		temp.union(gen(u, in), out);
	}

	/*
	 * what elements in the income abstraction set
	 * should be killed, when considering the statement
	 * u.
	 */
	protected FlowSet<Local> kill(Unit u) {
		FlowSet<Local> res = new ArraySparseSet<>();

		for(Local local: getDefVariables(u)) {
			res.add(local);
		}

		return res;
	}

	/*
	 * what elements should be generated, when considering the
	 * statement u.
	 */
	protected FlowSet<DataFlowAbstraction> gen(Unit u, FlowSet<DataFlowAbstraction> in) {
		FlowSet<DataFlowAbstraction> res = new ArraySparseSet<>();
		if(isSourceStatement(u)) {
			for(Local local: getDefVariables(u)) {
				res.add(new DataFlowAbstraction(local, findSourceStatement(u)));
			}
		}
		return res;
	}

	@Override
	protected FlowSet<DataFlowAbstraction> newInitialFlow() {
		return new ArraySparseSet<>();
	}

	@Override
	protected void merge(FlowSet<DataFlowAbstraction> in1, FlowSet<DataFlowAbstraction> in2, FlowSet<DataFlowAbstraction> out) {
		in1.union(in2, out);
	}

	@Override
	protected void copy(FlowSet<DataFlowAbstraction> source, FlowSet<DataFlowAbstraction> dest) {
		source.copy(dest);
	}

	/*
	 * Here we detect a possible conflict with the "sink" statement
	 * d. The conflict occurs when:
	 *
	 * - d is a sink statement
	 * - d refers to a "source" variable
	 */
	protected void detectConflict(FlowSet<DataFlowAbstraction> in, Unit d) {
		if(isSinkStatement(d)) {
			for(ValueBox box: d.getUseBoxes()) {
				if(box.getValue() instanceof Local) {
					for(DataFlowAbstraction item: in) {
						if(item.getLocal().equals(box.getValue())) {
							Conflict c = new Conflict(item.getStmt(), findSinkStatement(d));
							Collector.instance().addConflict(c);
						}
					}
				}
			}
		}
	}

	//------------------ auxiliary methods ---------------------------//

	protected Statement findSourceStatement(Unit d) {
		return definition.getSourceStatements().stream().filter(s -> s.getUnit().equals(d)).
				findFirst().get();
	}

	protected Statement findSinkStatement(Unit d) {
		return definition.getSinkStatements().stream().filter(s -> s.getUnit().equals(d)).
				findFirst().get();
	}

	protected Statement findStatement(Unit d) {
		return Statement.builder()
				.setClass(methodBody.getMethod().getDeclaringClass())
				.setMethod(methodBody.getMethod())
				.setType(Statement.Type.SOURCE)
				.setUnit(d)
				.setSourceCodeLineNumber(d.getJavaSourceStartLineNumber()).build();
	}

	protected Statement findStatementBase(Unit d) {
		return Statement.builder()
				.setClass(methodBody.getMethod().getDeclaringClass())
				.setMethod(methodBody.getMethod())
				.setType(Statement.Type.IN_BETWEEN)
				.setUnit(d)
				.setSourceCodeLineNumber(d.getJavaSourceStartLineNumber()).build();
	}

	protected boolean isSourceStatement(Unit d) {
		return definition.getSourceStatements().stream().map(s -> s.getUnit()).collect(Collectors.toList()).contains(d);
	}

	protected boolean isSinkStatement(Unit d) {
		return definition.getSinkStatements().stream().map(s -> s.getUnit()).collect(Collectors.toList()).contains(d);
	}

	public void clear() {
		Collector.instance().clear();
	}

	public Set<Conflict> getConflicts() {
		return Collector.instance().getConflicts();
	}

	public Set<HashMap<String, JInstanceFieldRef>> getHashMapJInstanceField() {
		return Collector.instance().getHashJInstanceField();
	}

	public Set<HashMap<String, StaticFieldRef>> getHashMapStatic() {
		return Collector.instance().getHashStaticField();
	}

	protected List<Local> getUseVariables(Unit u) {
		return u.getUseBoxes().stream()
				.map(box -> box.getValue())
				.filter(v -> v instanceof Local)
				.map(v -> (Local)v)
				.collect(Collectors.toList());
	}

	protected List<Local> getDefVariables(Unit u) {
		List<Local> localDefs = new ArrayList<>();
		for (ValueBox v : u.getDefBoxes()) {
			if (v.getValue() instanceof Local) {
				localDefs.add((Local) v.getValue());
			} else if (v.getValue() instanceof JArrayRef) {
				JArrayRef ref = (JArrayRef) v.getValue();
				localDefs.add((Local) ref.getBaseBox().getValue());
			}
			else if (v.getValue() instanceof JInstanceFieldRef) {
				JInstanceFieldRef ref = (JInstanceFieldRef) v.getValue();
				localDefs.add((Local) ref.getBaseBox().getValue());
			}
		}
		return localDefs;
	}

	protected boolean isLeftStatement(Unit u) {
		return isSourceStatement(u);
	}

	protected boolean isRightStatement(Unit u) {
		return isSinkStatement(u);
	}

	protected Statement findRightStatement(Unit u) {
		return findSinkStatement(u);
	}

	protected Statement findLeftStatement(Unit u) {
		return findSourceStatement(u);
	}
}
