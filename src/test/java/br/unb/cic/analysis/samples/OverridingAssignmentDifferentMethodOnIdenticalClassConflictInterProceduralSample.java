package br.unb.cic.analysis.samples;

// Conflict: [left, IdenticalClassSample.setFoo():21] --> [right, IdenticalClassSample.setBar():21]
public class OverridingAssignmentDifferentMethodOnIdenticalClassConflictInterProceduralSample {
    private IdenticalClassSample identicalClassSample;

    public OverridingAssignmentDifferentMethodOnIdenticalClassConflictInterProceduralSample() {
        this.identicalClassSample = new IdenticalClassSample();
    }

    public void m() {
        identicalClassSample.setFoo(1); // LEFT
        identicalClassSample.setBar(2); // RIGHT
    }
}

class IdenticalClassSample {
    private int att;

    public void setBar(int bar) {
        this.att = bar;
    }

    public void setFoo(int foo) {
        this.att = foo;
    }
}