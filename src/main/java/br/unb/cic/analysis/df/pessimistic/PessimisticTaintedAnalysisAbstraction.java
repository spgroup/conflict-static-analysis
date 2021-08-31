package br.unb.cic.analysis.df.pessimistic;


import br.unb.cic.analysis.model.Statement;
import soot.Value;
import soot.jimple.InstanceFieldRef;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PessimisticTaintedAnalysisAbstraction {

    private Map<Value, Statement> marked;
    private Map<Value, Statement> markedFields;

    PessimisticTaintedAnalysisAbstraction() {
        this.marked = new HashMap<>();
        this.markedFields = new HashMap<>();
    }

    public void union(PessimisticTaintedAnalysisAbstraction in, PessimisticTaintedAnalysisAbstraction target) {
        target.marked.putAll(this.marked);
        target.marked.putAll(in.marked);
        target.markedFields.putAll(this.markedFields);
        target.markedFields.putAll(in.markedFields);
    }

    public void copy(PessimisticTaintedAnalysisAbstraction target) {
        target.marked.clear();
        target.marked.putAll(this.marked);
        target.markedFields.clear();
        target.markedFields.putAll(this.markedFields);
    }

    public void union(PessimisticTaintedAnalysisAbstraction in) {
        this.marked.putAll(in.marked);
        this.markedFields.putAll(in.markedFields);
    }

    public void difference(PessimisticTaintedAnalysisAbstraction in) {
        for (Value key : this.marked.keySet()) {
           if (in.marked.containsKey(key)) {
               this.marked.remove(key);
           }
        }
        for (Value key: this.markedFields.keySet()) {
            if (in.markedFields.containsKey(key)) {
                this.markedFields.remove(key);
            }
        }
    }

    public void mark(Value value, Statement statement) {
        this.marked.put(value, statement);
    }

    public void markFields(Value value, Statement statement) {
        this.markedFields.put(value, statement);
    }

    public boolean isMarked(Value value) {
        if (this.marked.containsKey(value)) return true;

        if (value instanceof InstanceFieldRef) {
            InstanceFieldRef fieldRef = (InstanceFieldRef) value;
            Value base = fieldRef.getBase();

            if (this.markedFields.containsKey(base) || this.marked.containsKey(base)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMarkedFields(Value value) {
        if (this.markedFields.containsKey(value) || this.marked.containsKey(value)) return true;

        for (Value key: this.marked.keySet()) {
            if (key instanceof InstanceFieldRef) {
                InstanceFieldRef fieldRef = (InstanceFieldRef) key;

                if (fieldRef.getBase().equals(value)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PessimisticTaintedAnalysisAbstraction that = (PessimisticTaintedAnalysisAbstraction) o;
        return Objects.equals(marked, that.marked) && Objects.equals(markedFields, that.markedFields);
    }
}
