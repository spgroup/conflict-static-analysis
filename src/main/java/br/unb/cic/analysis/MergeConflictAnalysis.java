package br.unb.cic.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import soot.Unit;
import soot.ValueBox;
import soot.tagkit.Tag;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class MergeConflictAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<String>>{

	private Set<Integer> sourceLines; 
	private Set<Integer> sinkLines;
	private List<String> conflicts; 
	
	public MergeConflictAnalysis(DirectedGraph<Unit> graph, Set<Integer> sourceLines, Set<Integer> sinkLines) {
		super(graph);
		this.sourceLines = sourceLines;
		this.sinkLines = sinkLines;
		conflicts = new ArrayList<>();
		doAnalysis();
		for(String s : conflicts) {
			System.out.println(s);
		}
	}
	
	
	@Override
	protected void flowThrough(FlowSet<String> in, Unit d, FlowSet<String> out) {
		System.out.println("Unit: " );
		System.out.println(" tags: ");
		
		for(Tag t: d.getTags()) {
			System.out.println("    - " + t);
		}
		
		detectConflict(in, d);
		FlowSet<String> temp = new ArraySparseSet<>();
		
		in.difference(kill(d), temp); 
		temp.union(gen(d), out);
		
		System.out.println(out);
	}
	
	private FlowSet<String> kill(Unit d) { 
		FlowSet<String> res = new ArraySparseSet<>();
		
		if(!sourceLines.contains(d.getJavaSourceStartLineNumber())) {
			for(ValueBox v : d.getDefBoxes()) {
				res.add(v.getValue().toString());
			}
		}
		return res; 
	} 

	private FlowSet<String> gen(Unit d) { 
		FlowSet<String> res = new ArraySparseSet<>();
		if(sourceLines.contains(d.getJavaSourceStartLineNumber())) {
			for(ValueBox v : d.getDefBoxes()) {
				System.out.println("deve gerar o codigo ********");
				res.add(v.getValue().toString());
			}
		}
		return res;
	} 

	private void detectConflict(FlowSet<String> in, Unit d) {
		if(sinkLines.contains(d.getJavaSourceStartLineNumber())) {
			d.getUseBoxes().stream()
			               .filter(box -> in.contains(box.getValue().toString()))
			               .forEach(box -> conflicts.add(d + " uses of " +  box.getValue().toString() + " is tainted"));
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


	public void setSourceLines(Set<Integer> sourceLines) {
		this.sourceLines = sourceLines;
	}

	public void setSinkLines(Set<Integer> sinkLines) {
		this.sinkLines = sinkLines;
	}
	
	public List<String> getConflicts() {
		return conflicts;
	}
}
