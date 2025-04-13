package br.unb.cic.analysis.model;

import soot.SootClass;
import soot.SootMethod;

public class TraversedLine {
    private final SootClass sootClass;
    private final SootMethod sootMethod;
    private final Integer lineNumber;

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


    public Integer getLineNumber() {
        return lineNumber;
    }


    public SootClass getSootClass() {
        return sootClass;
    }

    public SootMethod getSootMethod() {
        return sootMethod;
    }

    @Override
    public String toString() {
        return "at " + sootClass.getName() + "." + sootMethod.getName() + "(" + sootClass.getShortJavaStyleName() + ".java:" + lineNumber + ")";
    }

    public String toJSON() {
        return String.format(
            "{" + "\n" +
                "\t" + "\"class\": \"%s\"," + "\n" +
                "\t" + "\"method\": \"%s\"," + "\n" +
                "\t" + "\"line\": %d" + "\n" +
            "}",
            sootClass.getName(), sootMethod.getName(), lineNumber
        );
    }
}
