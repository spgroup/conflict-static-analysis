package br.unb.cic.analysis.model;

import java.util.Objects;

public class DoubleSourceConflict extends Conflict {

    protected String targetClassName;
    protected String targetMethodName;
    protected Integer targetLineNumber;

    public DoubleSourceConflict(Statement source, Statement sink, Statement target) {
        super(source, sink);
        targetClassName = target.getSootClass().getName();
        targetMethodName = target.getSootMethod().getName();
        targetLineNumber = target.getUnit().getJavaSourceStartLineNumber();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DoubleSourceConflict that = (DoubleSourceConflict) o;
        return Objects.equals(targetClassName, that.targetClassName) &&
                Objects.equals(targetMethodName, that.targetMethodName) &&
                Objects.equals(targetLineNumber, that.targetLineNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), targetClassName, targetMethodName, targetLineNumber);
    }

    @Override
    public String toString() {
        return String.format("(source(%s, %s, %d), sink(%s, %s, %d)) => target(%s, %s, %d)", sourceClassName, sourceMethodName, sourceLineNumber,
                sinkClassName, sinkMethodName, sinkLineNumber, targetClassName, targetMethodName, targetLineNumber);
    }
}
