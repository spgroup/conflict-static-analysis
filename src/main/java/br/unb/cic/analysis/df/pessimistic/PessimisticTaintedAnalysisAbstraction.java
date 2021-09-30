package br.unb.cic.analysis.df.pessimistic;


import br.unb.cic.analysis.model.Statement;
import soot.Value;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Definition that = (Definition) o;
        return Objects.equals(value, that.value) && Objects.equals(statement, that.statement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, statement);
    }
}

/**
 * This data structure is used to allow two types of checks:
 *      - If the value is marked
 *      - If the value has marked fields
 *
 * It also allows to mark all fields for a value, always assuming that the
 * value class has at least one field that can be marked.
 *
 * Note that marking fields for a value can also cause a check on one of the value's fields
 * to return true.
 */
public class PessimisticTaintedAnalysisAbstraction {

    /**
     * This map is used to represent values that were directly marked
     */
    private Map<String, Definition> marked;
    /**
     * This map represents values that were passed to the markFields method
     * if a value is in this map, then it means that all its fields are marked
     */
    private Map<String, Definition> markedFields;
    /**
     * This map represents the unmarked values for the cases that we want o unmark a specific field
     * in a value that has all fields marked.
     */
    private Map<String, Value> unmarked;

    PessimisticTaintedAnalysisAbstraction() {
        this.marked = new HashMap<>();
        this.markedFields = new HashMap<>();
        this.unmarked = new HashMap<>();
    }

    public void merge(PessimisticTaintedAnalysisAbstraction in, PessimisticTaintedAnalysisAbstraction target) {
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

    /**
     * Marks directly the value so that the whole value and its fields are considered for dataflows.
     * @param value
     * @param statement
     */
    public void mark(Value value, Statement statement) {
        String valueKey = value.toString();

        // tries to remove the value from the unmarked map if
        // it has been unmarked before
        this.unmarked.remove(valueKey);

        this.marked.put(valueKey, new Definition(value, statement));
    }

    /**
     * Unmarks directly the value so that it or its fields are no longer considered for dataflows.
     * @param value
     */
    public void unmark(Value value) {
        String valueKey = value.toString();

        this.marked.remove(valueKey);
        this.markedFields.remove(valueKey);

        // adds it to the unmarked map, so that if it is a field in value that
        // has its fields marked, this specific field is unmarked
        if (value instanceof InstanceFieldRef) {
            this.unmarked.put(valueKey, value);
        }
    }


    /**
     * Marks a value fields so that only its fields are considered for dataflows.
     * @param value
     * @param statement
     */
    public void markFields(Value value, Statement statement) {
        String valueKey = value.toString();

        // check if any of the value's fields are in the unmarked
        // map and remove them

        Map<String, Value> temp = new HashMap<>(this.unmarked);
        for (Value unmarkedValue : temp.values())  {
            if (unmarkedValue instanceof InstanceFieldRef) {
                InstanceFieldRef fieldRef = (InstanceFieldRef) unmarkedValue;

                if (fieldRef.getBase().toString().equals(valueKey)) {
                    this.unmarked.remove(fieldRef.toString());
                }
            }
        }

        this.markedFields.put(valueKey, new Definition(value, statement));
    }

    /**
     * Returns true if the value should be considered for dataflows.
     * @param value
     */
    public boolean isMarked(Value value) {
        return getValueDefinitionStatement(value) != null;
    }

    /**
     * Returns true if at least one if its fields should be considered for dataflows.
     * @param value
     */
    public boolean hasMarkedFields(Value value) {
        return getValueFieldsDefinitionStatement(value) != null;
    }

    /**
     * Returns the statement that was passed for the mark or markFields that marked value
     *
     * If the value is a Local, returns the statement that marked it directly if it exists
     *
     * If tha value is a Field, returns the statement that marked it directly, or marked the parent value
     * or marked the parent value fields
     *
     * @param value
     */
    public Statement getValueDefinitionStatement(Value value) {
        Definition result = null;

        String valueKey = value.toString();

        // check if the value was directly unmarked
        // (useful for the case that the value is field in a value that has fields marked)
        if (!this.unmarked.containsKey(valueKey)) {
            // if the value was directly marked than return the statement that marked it
            if (this.marked.containsKey(valueKey)) {
                result = this.marked.get(valueKey);
            } else if (value instanceof InstanceFieldRef) {
                InstanceFieldRef fieldRef = (InstanceFieldRef) value;
                Value base = fieldRef.getBase();
                String baseKey = base.toString();

                // if the value is a field and its base value was directly marked
                // return the statement that marked the base value
                if (this.marked.containsKey(baseKey)) {
                    result = this.marked.get(baseKey);
                // if the value is a field and its base value had its fields marked
                // return the statement that marked the fields
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

    /**
     * Returns the statement that marked the value fields, it can return either the statement that
     * marked directly one of the fields or that marked all fields with the markFields method.
     * @param value
     */
    public Statement getValueFieldsDefinitionStatement(Value value) {
        Definition result = null;
        String valueKey = value.toString();

        // if the value was directly marked then it has the fields marked as well
        // return the statement that marked the value
        if (this.marked.containsKey(valueKey)) {
            result = this.marked.get(valueKey);

        // if the value had its fields marked then it has at least one field marked
        // return the statement that marked the value fields
        } else if (this.markedFields.containsKey(valueKey)) {
            result = this.markedFields.get(valueKey);
        } else {
            // check if any of the fields was directly marked, in that case
            // return the statement that marked the field
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

    /**
     * This is used to check if any of the value used by the statement is marked in any way
     * it also assumes that a method call in an object uses the object fields
     * @param statement
     * @return
     */
    public boolean usesMarkedValue(Statement statement) {
        boolean isDirectlyMarked = statement
                .getUnit()
                .getUseBoxes()
                .stream()
                .anyMatch(use -> this.isMarked(use.getValue()));

        if (isDirectlyMarked) {
            return true;
        }

        // if it is an invoke we will consider it used all fields
        // so if any of the fields is marked, then it uses a marked field
        InstanceInvokeExpr invokeExpr = statement.getInvoke();
        boolean isInvoke = invokeExpr != null;
        if (isInvoke && this.hasMarkedFields(invokeExpr.getBase())) {
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PessimisticTaintedAnalysisAbstraction that = (PessimisticTaintedAnalysisAbstraction) o;
        return Objects.equals(marked, that.marked) && Objects.equals(markedFields, that.markedFields) && Objects.equals(unmarked, that.unmarked);
    }

    @Override
    public int hashCode() {
        return Objects.hash(marked, markedFields, unmarked);
    }
}
