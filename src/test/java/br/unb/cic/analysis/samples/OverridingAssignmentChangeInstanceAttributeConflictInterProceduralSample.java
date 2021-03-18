package br.unb.cic.analysis.samples;

// Conflict: [left, m():8] --> [right, InstanceAttributeSample.setFoo():17]
public class OverridingAssignmentChangeInstanceAttributeConflictInterProceduralSample {
    private InstanceAttributeSample instanceAttributeSample;

    public void m() {
        this.instanceAttributeSample = new InstanceAttributeSample(); // LEFT
        instanceAttributeSample.setFoo(2); // RIGHT
    }
}

class InstanceAttributeSample {
    private int att;

    public void setFoo(int foo) {
        this.att = foo;
    }
}