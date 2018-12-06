package br.unb.cic.df.analysis.model;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import java.util.Objects;

/**
 * A class that represents either a "source" or a "sink" statement 
 * in control-flow reachability analysis. 
 * 
 * @author rbonifacio
 */
public class Statement {
	public enum Type {
		SOURCE, 
		SINK,
		IN_BETWEEN;
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
	
	Statement(SootClass sootClass, SootMethod sootMethod, Unit unit, Type type) {
		this.sootClass = sootClass;
		this.sootMethod = sootMethod;
		this.unit = unit;
		this.type = type;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Statement statement = (Statement) o;
		return Objects.equals(sootClass, statement.sootClass) &&
				Objects.equals(sootMethod, statement.sootMethod) &&
				Objects.equals(unit, statement.unit) &&
				type == statement.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(sootClass, sootMethod, unit, type);
	}

	public String toString() {
		return unit.toString();
	}
}
