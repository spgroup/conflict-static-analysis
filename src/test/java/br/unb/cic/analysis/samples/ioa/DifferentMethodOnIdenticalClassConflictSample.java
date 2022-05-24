package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, IdenticalClassSample.setFoo():25] --> [right, IdenticalClassSample.setBar():21]
public class DifferentMethodOnIdenticalClassConflictSample {
    private IdenticalClassSample identicalClassSample;

    public static void main(String[] args) {
        DifferentMethodOnIdenticalClassConflictSample m =
                new DifferentMethodOnIdenticalClassConflictSample();

        m.identicalClassSample = new IdenticalClassSample();
        m.identicalClassSample.setFoo(1); // LEFT
        m.identicalClassSample.setBar(2); // RIGHT
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