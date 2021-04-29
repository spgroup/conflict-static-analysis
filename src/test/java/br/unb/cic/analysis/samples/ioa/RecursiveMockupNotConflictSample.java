package br.unb.cic.analysis.samples.ioa;

public class RecursiveMockupNotConflictSample {

    private int x;

    public void m() {
        foo(); // LEFT
        this.x = 3;
        foo(); // Right
    }

    private void foo() {
        this.x += 1;
    }
}