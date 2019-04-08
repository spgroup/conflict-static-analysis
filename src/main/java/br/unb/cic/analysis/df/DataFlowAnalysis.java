package br.unb.cic.analysis.df;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.ConflictReport;
import br.unb.cic.analysis.model.Statement;
import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.DirectedGraph;
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
public class DataFlowAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<DataFlowAbstraction>>{

	private AbstractMergeConflictDefinition definition;

	public DataFlowAnalysis(DirectedGraph<Unit> graph, AbstractMergeConflictDefinition definition) {
		super(graph);
		this.definition = definition;
		definition.loadSourceStatements();
		definition.loadSinkStatements();
		doAnalysis();
	}
	
	
	@Override
	protected void flowThrough(FlowSet<DataFlowAbstraction> in, Unit d, FlowSet<DataFlowAbstraction> out) {
		detectConflict(in, d);
		FlowSet<DataFlowAbstraction> temp = new ArraySparseSet<>();

		FlowSet<DataFlowAbstraction> killSet = new ArraySparseSet<>();
		FlowSet<Local> mustKill = kill(d);
		for(DataFlowAbstraction item : in) {
			if(mustKill.contains(item.getLocal())) {
				killSet.add(item);
			}
		}
  		in.difference(killSet, temp);
		temp.union(gen(d), out);
	}
	
	private FlowSet<Local> kill(Unit d) {
		FlowSet<Local> res = new ArraySparseSet<>();
		
		for(ValueBox v : d.getDefBoxes()) {
			if(v.getValue() instanceof  Local)
				res.add((Local)v.getValue());
		}
		return res;
	} 

	private FlowSet<DataFlowAbstraction> gen(Unit d) {
		FlowSet<DataFlowAbstraction> res = new ArraySparseSet<>();
		if(isSourceStatement(d)) {
			for(ValueBox v : d.getDefBoxes()) {
				if(v.getValue() instanceof Local)
					res.add(new DataFlowAbstraction((Local)v.getValue(), findSourceStatement(d)));
			}
		}
		return res;
	} 

	protected void detectConflict(FlowSet<DataFlowAbstraction> in, Unit d) {
		if(isSinkStatement(d)) {
			for(ValueBox box: d.getUseBoxes()) {
				if(box.getValue() instanceof Local) {
					for(DataFlowAbstraction item: in) {
						if(item.getLocal().equals((Local)box.getValue())) {
							Conflict c = new Conflict(item.getStmt(), findSinkStatement(d));
							Collector.instance().addConflict(c.toString());
						}
					}
				}
			}
		}
	}

	protected Statement findSourceStatement(Unit d) {
		return definition.getSourceStatements().stream().filter(s -> s.getUnit().equals(d)).
				findFirst().get();
	}

	protected Statement findSinkStatement(Unit d) {
		return definition.getSinkStatements().stream().filter(s -> s.getUnit().equals(d)).
				findFirst().get();
	}

	protected boolean isSourceStatement(Unit d) {
		return definition.getSourceStatements().stream().map(s -> s.getUnit()).collect(Collectors.toList()).contains(d);
	}

	protected boolean isSinkStatement(Unit d) {
		return definition.getSinkStatements().stream().map(s -> s.getUnit()).collect(Collectors.toList()).contains(d);
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

	public Set<String> getConflicts() {
		return Collector.instance().getConflicts();
	}

}
