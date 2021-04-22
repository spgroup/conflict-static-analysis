package br.unb.cic.analysis.samples.ioa;

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