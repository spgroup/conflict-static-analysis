package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, InstanceAttributeSample():20] --> [right, InstanceAttributeSample.setFoo():24]
public class ChangeInstanceAttributeConflictSample {
    private InstanceAttributeSample instanceAttributeSample;

    public static void main(String[] args) {
        ChangeInstanceAttributeConflictSample m =
                new ChangeInstanceAttributeConflictSample();

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