package br.unb.cic.analysis.df;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import soot.Unit;
import soot.ValueBox;
import soot.tagkit.Tag;
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
public class DataFlowAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<String>>{

	private AbstractMergeConflictDefinition definition;
	
	public DataFlowAnalysis(DirectedGraph<Unit> graph, AbstractMergeConflictDefinition definition) {
		super(graph);
		this.definition = definition;
		definition.loadSourceStatements();
		definition.loadSinkStatements();
		doAnalysis();
	}
	
	
	@Override
	protected void flowThrough(FlowSet<String> in, Unit d, FlowSet<String> out) {
		detectConflict(in, d);
		FlowSet<String> temp = new ArraySparseSet<>();
		
		in.difference(kill(d), temp); 
		temp.union(gen(d), out);
	}
	
	private FlowSet<String> kill(Unit d) { 
		FlowSet<String> res = new ArraySparseSet<>();
		
		if(!definition.getSourceStatements().stream().map(s -> s.getUnit()).collect(Collectors.toList()).contains(d)) {
			for(ValueBox v : d.getDefBoxes()) {
				res.add(v.getValue().toString());
			}
		}
		return res; 
	} 

	private FlowSet<String> gen(Unit d) { 
		FlowSet<String> res = new ArraySparseSet<>();
		if(definition.getSourceStatements().stream().map(s -> s.getUnit()).collect(Collectors.toList()).contains(d)) {
			for(ValueBox v : d.getDefBoxes()) {
				res.add(v.getValue().toString());
			}
		}
		return res;
	} 

	private void detectConflict(FlowSet<String> in, Unit d) {
		if(definition.getSinkStatements().stream().map(s -> s.getUnit()).collect(Collectors.toList()).contains(d)) {
			d.getUseBoxes().stream()
			               .filter(box -> in.contains(box.getValue().toString()))
			               .forEach(box -> Collector.instance().addConflict(d + " uses of " +  box.getValue().toString() + " is tainted"));
		}
	}
	
	@Override
	protected FlowSet<String> newInitialFlow() {
		return new ArraySparseSet<>();
	}

	@Override
	protected void merge(FlowSet<String> in1, FlowSet<String> in2, FlowSet<String> out) {
		in1.union(in2, out);
	}

	@Override
	protected void copy(FlowSet<String> source, FlowSet<String> dest) {
		source.copy(dest);
	}

	public List<String> getConflicts() {
		return Collector.instance().getConflicts();
	}
}
