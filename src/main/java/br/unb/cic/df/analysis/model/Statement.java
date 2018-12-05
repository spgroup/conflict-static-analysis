package br.unb.cic.df.analysis.model;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

/**
 * A class that represents either a "source" or a "sink" statement 
 * in control-flow reachability analysis. 
 * 
 * @author rbonifacio
 */
public class Statement {
	public enum Type {
		SOURCE, 
		SINK; 
	}

	private SootClass sootClass; 
	private SootMethod sootMethod; 
	private Unit unit; 
	private Type type;
	
	public Statement(SootClass sootClass, SootMethod sootMethod, Unit unit, Type type) {
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
	
}
