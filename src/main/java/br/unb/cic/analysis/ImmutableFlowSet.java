package br.unb.cic.analysis;

import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

public class ImmutableFlowSet<U> {
	private FlowSet<U> flowSet; 
	
	public ImmutableFlowSet() {
		flowSet = new ArraySparseSet<>();
	}
	
	public ImmutableFlowSet(FlowSet<U> flowSet) {
		this.flowSet = flowSet; 
	}
	
	public ImmutableFlowSet<U> add(U value) {
		FlowSet<U> clone = flowSet.clone(); 
		
		clone.add(value);
		
		return new ImmutableFlowSet<>(clone);
	}
	
	public ImmutableFlowSet<U> union(ImmutableFlowSet<U> other) {
		FlowSet<U> out = new ArraySparseSet<>();
		flowSet.union(other.flowSet, out);
		return new ImmutableFlowSet<>(out);
	}
	
	public ImmutableFlowSet<U> intersection(ImmutableFlowSet<U> other) {
		FlowSet<U> out = new ArraySparseSet<>();
		flowSet.intersection(other.flowSet, out);
		return new ImmutableFlowSet<>(out);
	}
	
	public ImmutableFlowSet<U> difference(ImmutableFlowSet<U> other) {
		FlowSet<U> out = new ArraySparseSet<>();
		flowSet.difference(other.flowSet, out);
		return new ImmutableFlowSet<>(out);
	}
	
	public ImmutableFlowSet<U> copy() {
		return new ImmutableFlowSet<>(flowSet.clone());
	}
	public boolean contains(U value) {
		return flowSet.contains(value);
	}
	
	public boolean isEmpty() {
		return flowSet.isEmpty();
	}
	
	public int size() {
		return flowSet.size();
	}
	
	public String toString() {
		return flowSet.toString();
	}

}
