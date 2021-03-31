package br.unb.cic.analysis.model;

import soot.Unit;

import java.util.Objects;

/**
 * An abstract representation of a conflict,
 * stating the class, method and line number
 * of the source / sink.
 */
public class Conflict {
    protected String sourceClassName;
    protected String sourceMethodName;
    protected Integer sourceLineNumber;
    protected String sinkClassName;
    protected String sinkMethodName;
    protected Integer sinkLineNumber;
    protected Unit sourceUnit;
    protected Unit sinkUnit;

    public Conflict(Statement source, Statement sink) {
        this.sourceClassName = source.getSootClass().getName();
        this.sourceMethodName = source.getSootMethod().getName();
        this.sourceLineNumber = source.getSourceCodeLineNumber();
        this.sourceUnit = source.getUnit();
        this.sinkClassName = sink.getSootClass().getName();
        this.sinkMethodName = sink.getSootMethod().getName();
        this.sinkLineNumber = sink.getSourceCodeLineNumber();
        this.sinkUnit = sink.getUnit();
    }

    @Deprecated
    public Conflict(String sourceClassName, String sourceMethodName, Integer sourceLineNumber, String sinkClassName, String sinkMethodName, Integer sinkLineNumber) {
        this.sourceClassName = sourceClassName;
        this.sourceMethodName = sourceMethodName;
        this.sourceLineNumber = sourceLineNumber;
        this.sinkClassName = sinkClassName;
        this.sinkMethodName = sinkMethodName;
        this.sinkLineNumber = sinkLineNumber;
    }

    public String getSourceClassName() {
        return sourceClassName;
    }

    public String getSourceMethodName() {
        return sourceMethodName;
    }

    public Integer getSourceLineNumber() {
        return sourceLineNumber;
    }

    public String getSinkClassName() {
        return sinkClassName;
    }

    public String getSinkMethodName() {
        return sinkMethodName;
    }

    public Integer getSinkLineNumber() {
        return sinkLineNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conflict conflict = (Conflict) o;
        return Objects.equals(sourceClassName, conflict.sourceClassName) &&
                Objects.equals(sourceMethodName, conflict.sourceMethodName) &&
                Objects.equals(sourceLineNumber, conflict.sourceLineNumber) &&
                Objects.equals(sourceUnit, conflict.sourceUnit) &&
                Objects.equals(sinkClassName, conflict.sinkClassName) &&
                Objects.equals(sinkMethodName, conflict.sinkMethodName) &&
                Objects.equals(sinkLineNumber, conflict.sinkLineNumber) &&
                Objects.equals(sinkUnit, conflict.sinkUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceClassName, sourceMethodName, sourceLineNumber, sinkClassName, sinkMethodName, sinkLineNumber);
    }

    @Override
    public String toString() {
        return String.format("source(%s, %s, %d, %s) => sink(%s, %s, %d, %s)", sourceClassName, sourceMethodName, sourceLineNumber, sourceUnit,
                sinkClassName, sinkMethodName, sinkLineNumber, sinkUnit);
    }
}
