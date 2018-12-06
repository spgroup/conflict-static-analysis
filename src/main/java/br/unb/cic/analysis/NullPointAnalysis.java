package br.unb.cic.analysis;

import soot.Unit;
import soot.ValueBox;
import soot.jimple.NullConstant;
import soot.jimple.internal.JimpleLocalBox;
import soot.tagkit.StringTag;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;


public class NullPointAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<String>>{

	
	public NullPointAnalysis(DirectedGraph<Unit> graph) {
		super(graph);
		doAnalysis();
	}

	@Override
	protected void flowThrough(FlowSet<String> in, Unit d, FlowSet<String> out) {
		//report a null pointer dereference
		reportNullPointerDereference(in, d); 
		
		//propagate the analysis. 
		FlowSet<String> temp = new ArraySparseSet<>();
		in.difference(kill(d), temp);
		temp.union(gen(d), out);
	}
	
	private FlowSet<String> kill(Unit d) {
		FlowSet<String> killSet = new ArraySparseSet<>();
		for(ValueBox v: d.getDefBoxes()) {
			if(mustKill(d)) {
				killSet.add(v.getValue().toString());
			}
		}
		return killSet;
	}
	
	private FlowSet<String> gen(Unit d) {
		FlowSet<String> genSet = new ArraySparseSet<>();
		for(ValueBox v: d.getDefBoxes()) {
			if(mustGen(d)) {
				genSet.add(v.getValue().toString());
			}
		}
		return genSet;
	}
	
	private boolean mustKill(Unit d) {
		return d.getUseBoxes().size() == 1 && d.getUseBoxes().get(0).getValue().equals(NullConstant.v());
	}
	
	private boolean mustGen(Unit d) {
		return ! mustKill(d);
	}
	


	@Override
	protected FlowSet<String> newInitialFlow() {
		return new ArraySparseSet<>();
	}

	@Override
	protected void merge(FlowSet<String> in1, FlowSet<String> in2, FlowSet<String> out) {
		in1.intersection(in2, out);
	}

	@Override
	protected void copy(FlowSet<String> source, FlowSet<String> dest) {
		source.copy(dest);
	}
	
	
	private void reportNullPointerDereference(FlowSet<String> in, Unit d) {
		for(ValueBox v: d.getUseBoxes()) {
			if(v instanceof JimpleLocalBox && !in.contains(v.getValue().toString())) {
				d.addTag(new StringTag("reference to" + d.getJavaSourceStartColumnNumber() + " might be null"));
				System.out.println("reference to " + v + " might be null");
			}
		}
	}
}
