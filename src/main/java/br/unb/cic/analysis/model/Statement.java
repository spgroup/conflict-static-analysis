package br.unb.cic.analysis.model;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;

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
		SOURCE_SINK
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

	public List<TraversedLine> getTraversedLine() {
		return traversedLine;
	}

	public void setTraversedLine(List<TraversedLine> traversedLine) {
		this.traversedLine = traversedLine;
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
		return unit.toString();
	}

	public boolean isSource() {
		return type == Type.SOURCE || type == Type.SOURCE_SINK;
	}

	public boolean isSink() {
		return type == Type.SINK || type == Type.SOURCE_SINK;
	}

	public boolean isAssign() {
		return this.unit instanceof AssignStmt;
	}

	public boolean isInvoke() {
		return getInvoke() != null;
	}

	public InstanceInvokeExpr getInvoke() {
		if (this.unit instanceof InvokeStmt) {
			InvokeStmt invoke = (InvokeStmt) this.unit;
			InvokeExpr expr = invoke.getInvokeExpr();

			if (expr instanceof InstanceInvokeExpr) {
				return (InstanceInvokeExpr) expr;
			}
 		}
		for (ValueBox use : this.unit.getUseBoxes()) {
			if (use.getValue() instanceof InstanceInvokeExpr) {
				return (InstanceInvokeExpr) use.getValue();
			}
		}
		return null;
	}

}