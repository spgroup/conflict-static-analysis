package br.unb.cic.analysis.model;

import soot.Value;

public final class ComparisonAssignment {
    private final String valueInAbs;
    private final String valueInFlow;

    public ComparisonAssignment(Value valueInAbs, Value valueInFlow) {
        this.valueInAbs = valueInAbs == null ? "null" : valueInAbs.toString();
        this.valueInFlow = valueInFlow == null ? "null" : valueInFlow.toString();
    }

    @Override
    public String toString() {
        return "valueInAbs=" + valueInAbs + ", valueInFlow=" + valueInFlow;
    }
}