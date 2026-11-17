package br.unb.cic.analysis.samples.ioa;
// Not Conflict
public class RecursiveMockupNotConflictSample {

    private int x;

    public void m() {
        RecursiveMockupNotConflictSample m = new RecursiveMockupNotConflictSample();

        m.foo(); // LEFT
        m.x = 3;
        m.foo(); // Right
    }

    private void foo() {
        x += 1;
    }
}