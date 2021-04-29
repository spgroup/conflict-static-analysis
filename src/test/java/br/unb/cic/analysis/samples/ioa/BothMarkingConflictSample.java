package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, foo():12] --> [right, foo():12]
public class BothMarkingConflictSample {
    private int x;

    public void m() {
        foo(); // LEFT // RIGHT
    }

    private void foo() {
        this.x = 1;
    }

}