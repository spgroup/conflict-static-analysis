package br.unb.cic.analysis;

import java.util.ArrayList;
import java.util.List;

import soot.Scene;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class InterproceduralMergeConflictAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<String>>{

	private List<String> conflicts; 
	
	public InterproceduralMergeConflictAnalysis(DirectedGraph<Unit> graph) {
		super(graph);
		conflicts = new ArrayList<>();
		doAnalysis();
		for(String s : conflicts) {
			System.out.println(s);
		}
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
		
		if(d.getTag("source") == null) {
			for(ValueBox v : d.getDefBoxes()) {
				res.add(v.getValue().toString());
			}
		}
		return res; 
	} 

	private FlowSet<String> gen(Unit d) { 
		FlowSet<String> res = new ArraySparseSet<>();
		if(d.getTag("source") != null) {
			for(ValueBox v : d.getDefBoxes()) {
				res.add(v.getValue().toString());
			}
		}
		return res;
	} 

	private void detectConflict(FlowSet<String> in, Unit d) {
		if(d.getTag("sink") != null) {
			d.getUseBoxes().stream()
			               .filter(box -> in.contains(box.getValue().toString()))
			               .forEach(box -> conflicts.add(d + " uses of " +  box.getValue().toString() + " is tainted"));
		}
//		else { // deals with the inter-procedural case.  
//			if(d instanceof InvokeStmt) {
//				InvokeStmt call = (InvokeStmt)d;
//				call.getInvokeExpr().getMethod().getBo
//			}
//		}
		//JimpleBasedInterproceduralCFG cfg = new JimpleBasedInterproceduralCFG();
		//cfg.getSuccsOf(u) in the case of u as a source, we can check if we might reach a sink. 
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
		return conflicts;
	}
}
