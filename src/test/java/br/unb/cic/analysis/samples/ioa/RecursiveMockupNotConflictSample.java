package br.unb.cic.analysis.samples.ioa;
// Not Conflict
public class RecursiveMockupNotConflictSample {

    private int x;

    public static void main(String[] args) {
        RecursiveMockupNotConflictSample m = new RecursiveMockupNotConflictSample();

        m.foo(); // LEFT
        m.x = 3;
        m.foo(); // Right
    }

    private void foo() {
        this.x += 1;
    }
}