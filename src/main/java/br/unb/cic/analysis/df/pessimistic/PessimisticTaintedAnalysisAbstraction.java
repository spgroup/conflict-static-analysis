package br.unb.cic.analysis.df.pessimistic;


import br.unb.cic.analysis.model.Statement;
import soot.Value;

import java.util.HashMap;
import java.util.Map;

public class PessimisticTaintedAnalysisAbstraction {

    private Map<Value, Statement> marked;

    PessimisticTaintedAnalysisAbstraction() {
        this.marked = new HashMap<>();
    }

    public void merge(PessimisticTaintedAnalysisAbstraction in, PessimisticTaintedAnalysisAbstraction target) {
        target.marked.putAll(this.marked);
        target.marked.putAll(in.marked);
    }

    public void copy(PessimisticTaintedAnalysisAbstraction target) {
        target.marked.clear();
        target.marked.putAll(this.marked);
    }

    public void union(PessimisticTaintedAnalysisAbstraction in) {
        this.marked.putAll(in.marked);
    }

    public void difference(PessimisticTaintedAnalysisAbstraction in) {
        for (Value key : this.marked.keySet()) {
           if (in.marked.containsKey(key)) {
               this.marked.remove(key);
           }
        }
    }

    public void mark(Value value, Statement statement) {
        this.marked.put(value, statement);
    }

    public boolean isMarked(Value value) {
        return this.marked.containsKey(value);
    }

    public boolean hasMarkedFields(Statement statement) {
        return false;
    }
}
