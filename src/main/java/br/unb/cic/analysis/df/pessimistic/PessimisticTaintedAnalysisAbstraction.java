package br.unb.cic.analysis.df.pessimistic;


import br.unb.cic.analysis.model.Statement;
import soot.Value;
import soot.jimple.InstanceFieldRef;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class Definition {
    private Value value;
    private Statement statement;

    Definition(Value value, Statement statement) {
        this.value = value;
        this.statement = statement;
    }

    public Value getValue() {
        return value;
    }

    public Statement getStatement() {
        return statement;
    }
}


public class PessimisticTaintedAnalysisAbstraction {

    private Map<String, Definition> marked;
    private Map<String, Definition> markedFields;

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
        for (String key : this.marked.keySet()) {
            if (in.marked.containsKey(key)) {
               this.marked.remove(key);
            }
        }
        for (String key: this.markedFields.keySet()) {
            if (in.markedFields.containsKey(key)) {
                this.markedFields.remove(key);
            }
        }
    }

    public void mark(Value value, Statement statement) {
        this.marked.put(value.toString(), new Definition(value, statement));
    }

    public void markFields(Value value, Statement statement) {
        this.markedFields.put(value.toString(), new Definition(value, statement));
    }

    public boolean isMarked(Value value) {
        return getMarkedStatement(value) != null;
    }

    public boolean hasMarkedFields(Value value) {
        return getMarkedFieldsStatement(value) != null;
    }

    public Statement getMarkedStatement(Value value) {
        Definition result = null;

        String valueKey = value.toString();
        if (this.marked.containsKey(valueKey)) {
            result = this.marked.get(valueKey);
        } else if (value instanceof InstanceFieldRef) {
            InstanceFieldRef fieldRef = (InstanceFieldRef) value;
            Value base = fieldRef.getBase();
            String baseKey = base.toString();

            if (this.marked.containsKey(baseKey)) {
                result = this.marked.get(baseKey);
            } else if (this.markedFields.containsKey(baseKey)) {
                result = this.markedFields.get(baseKey);
            }
        }

        if (result != null) {
            return result.getStatement();
        }
        return null;
    }

    public Statement getMarkedFieldsStatement(Value value) {
        Definition result = null;
        String valueKey = value.toString();

        if (this.marked.containsKey(valueKey)) {
            result = this.marked.get(valueKey);
        } else if (this.markedFields.containsKey(valueKey)) {
            result = this.markedFields.get(valueKey);
        } else {
            for (Definition definition: this.marked.values()) {
                Value fieldValue = definition.getValue();
                if (fieldValue instanceof InstanceFieldRef) {
                    InstanceFieldRef fieldRef = (InstanceFieldRef) fieldValue;

                    if (fieldRef.getBase().toString().equals(valueKey)) {
                        result = definition;
                    };
                }
            }
        }

        if (result != null) {
            return result.getStatement();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PessimisticTaintedAnalysisAbstraction that = (PessimisticTaintedAnalysisAbstraction) o;
        return Objects.equals(marked, that.marked) && Objects.equals(markedFields, that.markedFields);
    }
}
