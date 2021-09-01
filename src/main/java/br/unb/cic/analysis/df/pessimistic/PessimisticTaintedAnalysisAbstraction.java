package br.unb.cic.analysis.df.pessimistic;


import br.unb.cic.analysis.model.Statement;
import soot.Value;
import soot.baf.Inst;
import soot.jimple.InstanceFieldRef;

import java.util.*;

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
    private Map<String, Value> unmarked;

    PessimisticTaintedAnalysisAbstraction() {
        this.marked = new HashMap<>();
        this.markedFields = new HashMap<>();
        this.unmarked = new HashMap<>();
    }

    public void union(PessimisticTaintedAnalysisAbstraction in, PessimisticTaintedAnalysisAbstraction target) {
        target.marked.putAll(this.marked);
        target.marked.putAll(in.marked);
        target.unmarked.putAll(this.unmarked);
        target.unmarked.putAll(in.unmarked);
        target.markedFields.putAll(this.markedFields);
        target.markedFields.putAll(in.markedFields);

    }

    public void copy(PessimisticTaintedAnalysisAbstraction target) {
        target.marked.clear();
        target.marked.putAll(this.marked);
        target.markedFields.clear();
        target.markedFields.putAll(this.markedFields);
        target.unmarked.clear();
        target.unmarked.putAll(this.unmarked);
    }

    public void mark(Value value, Statement statement) {
        String valueKey = value.toString();
        this.unmarked.remove(valueKey);
        this.marked.put(valueKey, new Definition(value, statement));
    }

    public void unmark(Value value) {
        String valueKey = value.toString();
        this.marked.remove(valueKey);
        this.unmarked.put(valueKey, value);
    }

    public void markFields(Value value, Statement statement) {
        String valueKey = value.toString();

        for (Value unmarkedValue : this.unmarked.values())  {
            if (unmarkedValue instanceof InstanceFieldRef) {
                InstanceFieldRef fieldRef = (InstanceFieldRef) unmarkedValue;

                if (fieldRef.getBase().toString().equals(valueKey)) {
                    this.unmarked.remove(fieldRef.toString());
                }
            }
        }

        this.markedFields.put(valueKey, new Definition(value, statement));
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
        if (!this.unmarked.containsKey(valueKey)) {
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
