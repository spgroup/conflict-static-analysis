package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, m():17] --> [right, InstanceAttributeSample.setFoo():21]
public class OverridingAssignmentChangeInstanceAttributeConflictInterProceduralSample {
    private InstanceAttributeSample instanceAttributeSample;

    public static void main(String[] args) {
        OverridingAssignmentChangeInstanceAttributeConflictInterProceduralSample m =
                new OverridingAssignmentChangeInstanceAttributeConflictInterProceduralSample();

        m.instanceAttributeSample = new InstanceAttributeSample(); // LEFT
        m.instanceAttributeSample.setFoo(2); // RIGHT
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