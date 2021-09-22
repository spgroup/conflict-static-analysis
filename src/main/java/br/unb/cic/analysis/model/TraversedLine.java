package br.unb.cic.analysis.model;

import soot.SootClass;
import soot.SootMethod;

public class TraversedLine {
    protected SootClass sootClass;
    protected SootMethod sootMethod;
    protected Integer lineNumber;

    public TraversedLine(SootClass sootClass, SootMethod sootMethod, Integer lineNumber) {
        this.sootClass = sootClass;
        this.sootMethod = sootMethod;
        this.lineNumber = lineNumber;
    }

    public TraversedLine(SootMethod sootMethod, Integer lineNumber) {
        this.sootClass = sootMethod.getDeclaringClass();
        this.sootMethod = sootMethod;
        this.lineNumber = lineNumber;
    }

    public SootClass getSootClass() {
        return sootClass;
    }

    public void setSootClass(SootClass sootClass) {
        this.sootClass = sootClass;
    }

    public SootMethod getSootMethod() {
        return sootMethod;
    }

    public void setSootMethod(SootMethod sootMethod) {
        this.sootMethod = sootMethod;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return "Stacktrace{" +
                "sootClass=" + sootClass.getShortJavaStyleName() +
                ", sootMethod=" + sootMethod.getSubSignature() +
                ", lineNumber=" + lineNumber +
                '}';
    }
}
