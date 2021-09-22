package br.unb.cic.analysis.model;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A class that represents either a "source" or a "sink" statement 
 * in a semantic merge conflict analysis.
 * 
 * @author rbonifacio
 */
public class Statement {
	public enum Type {
		SOURCE,
		SINK,
		IN_BETWEEN,
		BOTH;
	}

	private static StatementBuilder builder;

	public static StatementBuilder builder() {
		if(builder == null) {
            builder = new StatementBuilder();
        }
        return builder;
    }

    private SootClass sootClass;
    private SootMethod sootMethod;
    private Unit unit;
    private Type type;
    private Integer sourceCodeLineNumber;
    private List<TraversedLine> traversedLine;

    Statement(SootClass sootClass, SootMethod sootMethod, Unit unit, Type type, Integer sourceCodeLineNumber) {
        this.sootClass = sootClass;
        this.sootMethod = sootMethod;
        this.unit = unit;
        this.type = type;
        this.sourceCodeLineNumber = sourceCodeLineNumber;
        this.traversedLine = new ArrayList<>();
    }

    public List<TraversedLine> getStacktrace() {
        return traversedLine;
    }

    public void setStacktrace(List<TraversedLine> traversedLine) {
        this.traversedLine = traversedLine;
    }

    public SootClass getSootClass() {
        return sootClass;
    }

    public SootMethod getSootMethod() {
        return sootMethod;
    }

	public Unit getUnit() {
		return unit;
	}

	public Type getType() {
		return type;
	}

	public Integer getSourceCodeLineNumber() {
		return sourceCodeLineNumber;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Statement statement = (Statement) o;
		return Objects.equals(sootClass, statement.sootClass) &&
				Objects.equals(sootMethod, statement.sootMethod) &&
				Objects.equals(unit, statement.unit) &&
				type == statement.type &&
				Objects.equals(sourceCodeLineNumber, statement.sourceCodeLineNumber);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sootClass, sootMethod, unit, type, sourceCodeLineNumber);
	}

	public String toString() {

		return String.format("stmt(%d, %s, %s)", sourceCodeLineNumber, unit,
				type);
	}
}
