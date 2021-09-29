package br.unb.cic.analysis.model;

import soot.Unit;

import java.util.List;
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
    protected Unit sourceUnit;
    protected List<TraversedLine> sourceTraversedLine;
    protected String sinkClassName;
    protected String sinkMethodName;
    protected Integer sinkLineNumber;
    protected Unit sinkUnit;
    protected List<TraversedLine> sinkTraversedLine;


    public Conflict(Statement source, Statement sink) {
        this.sourceClassName = source.getSootClass().getName();
        this.sourceMethodName = source.getSootMethod().getName();
        this.sourceLineNumber = source.getSourceCodeLineNumber();
        this.sourceUnit = source.getUnit();
        this.sourceTraversedLine = source.getStacktrace();
        this.sinkClassName = sink.getSootClass().getName();
        this.sinkMethodName = sink.getSootMethod().getName();
        this.sinkLineNumber = sink.getSourceCodeLineNumber();
        this.sinkUnit = sink.getUnit();
        this.sinkTraversedLine = sink.getStacktrace();
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

    public List<TraversedLine> getSourceTraversedLine() {
        return sourceTraversedLine;
    }

    public void setSourceTraversedLine(List<TraversedLine> sourceTraversedLine) {
        this.sourceTraversedLine = sourceTraversedLine;
    }

    public List<TraversedLine> getSinkTraversedLine() {
        return sinkTraversedLine;
    }

    public void setSinkTraversedLine(List<TraversedLine> sinkTraversedLine) {
        this.sinkTraversedLine = sinkTraversedLine;
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


    public String toStringAbstract() {
        return String.format("source(%s, %s, %d, %s, %s) => sink(%s, %s, %d, %s, %s)", sourceTraversedLine.get(0).getSootClass(),
                sourceTraversedLine.get(0).getSootMethod(), sourceTraversedLine.get(0).getLineNumber(), sourceUnit,
                sourceTraversedLine,
                sinkTraversedLine.get(0).getSootClass(), sinkTraversedLine.get(0).getSootMethod(),
                sinkTraversedLine.get(0).getLineNumber(), sinkUnit, sinkTraversedLine);
    }


    @Override
    public String toString() {
        return String.format("source(%s, %s, %d, %s, %s) => sink(%s, %s, %d, %s, %s)", sourceClassName,
                sourceMethodName, sourceLineNumber, sourceUnit, sourceTraversedLine,
                sinkClassName, sinkMethodName, sinkLineNumber, sinkUnit, sinkTraversedLine);
    }
}
