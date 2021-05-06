package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, m():17] --> [right, InstanceAttributeSample.setFoo():21]
public class OverridingAssignmentChangeInstanceAttributeConflictInterProceduralSample {
    private InstanceAttributeSample instanceAttributeSample;

    public void m() {
        this.instanceAttributeSample = new InstanceAttributeSample(); // LEFT
        instanceAttributeSample.setFoo(2); // RIGHT
    }
}

class InstanceAttributeSample {
    private int att;

    public InstanceAttributeSample() {
        this.att = 0;
    }

    public void setFoo(int foo) {
        this.att = foo;
    }
}