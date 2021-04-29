package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, foo():13] --> [right, foo():13]
public class RecursiveMockupConflictSample {
    private int x;

    public void m() {
        foo(); // LEFT
        foo(); // Right
    }

    private void foo() {
        this.x += 1;
    }
}