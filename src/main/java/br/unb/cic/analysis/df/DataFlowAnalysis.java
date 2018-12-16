package br.unb.cic.analysis.df;

import java.util.List;
import java.util.stream.Collectors;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

/**
 * Intraprocedural dataflow analysis for identifying
 * merge conflicts from source defitions to sink
 * usage scenarios. Therefore, we reduce this problem
 * to a def-use analysis.
 */
public class DataFlowAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<Local>>{

	private AbstractMergeConflictDefinition definition;
	
	public DataFlowAnalysis(DirectedGraph<Unit> graph, AbstractMergeConflictDefinition definition) {
		super(graph);
		this.definition = definition;
		definition.loadSourceStatements();
		definition.loadSinkStatements();
		doAnalysis();
	}
	
	
	@Override
	protected void flowThrough(FlowSet<Local> in, Unit d, FlowSet<Local> out) {
		detectConflict(in, d);
		FlowSet<Local> temp = new ArraySparseSet<>();

		in.difference(kill(d), temp);
		temp.union(gen(d), out);
	}
	
	private FlowSet<Local> kill(Unit d) {
		FlowSet<Local> res = new ArraySparseSet<>();
		
		//if(!isSourceStatement(d)) {
			for(ValueBox v : d.getDefBoxes()) {
				if(v.getValue() instanceof  Local)
					res.add((Local)v.getValue());
			}
		//}
		return res; 
	} 

	private FlowSet<Local> gen(Unit d) {
		FlowSet<Local> res = new ArraySparseSet<>();
		if(isSourceStatement(d)) {
			for(ValueBox v : d.getDefBoxes()) {
				if(v.getValue() instanceof Local)
					res.add((Local)v.getValue());
			}
		}
		return res;
	} 

	protected void detectConflict(FlowSet<Local> in, Unit d) {
		if(isSinkStatement(d)) {
			for(ValueBox box: d.getUseBoxes()) {
				if(box.getValue() instanceof Local && in.contains((Local)box.getValue())) {
					Collector.instance().addConflict(d + " uses of " +  box.getValue().toString() + " is tainted");
				}
			}
		}
	}

	protected boolean isSourceStatement(Unit d) {
		return definition.getSourceStatements().stream().map(s -> s.getUnit()).collect(Collectors.toList()).contains(d);
	}

	protected boolean isSinkStatement(Unit d) {
		return definition.getSinkStatements().stream().map(s -> s.getUnit()).collect(Collectors.toList()).contains(d);
	}

	@Override
	protected FlowSet<Local> newInitialFlow() {
		return new ArraySparseSet<>();
	}

	@Override
	protected void merge(FlowSet<Local> in1, FlowSet<Local> in2, FlowSet<Local> out) {
		in1.union(in2, out);
	}

	@Override
	protected void copy(FlowSet<Local> source, FlowSet<Local> dest) {
		source.copy(dest);
	}

	public List<String> getConflicts() {
		return Collector.instance().getConflicts();
	}
}
