package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class LocalArrayRecursiveNotConflictSample {
    public void m() {
        foo(); // LEFT
        bar(); // RIGHT
    }

    private void foo() {
        int[] arr = {0};
        bar();
    }

    private void bar() {
        int[] arr = {1};
    }
}