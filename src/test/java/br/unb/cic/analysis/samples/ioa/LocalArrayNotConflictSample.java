package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class LocalArrayNotConflictSample {
    public static void main(String[] args) {
        LocalArrayNotConflictSample m = new LocalArrayNotConflictSample();

        m.foo(); // LEFT
        m.bar(); // RIGHT
    }

    private void foo() {
        int[] arr = {0};
    }

    private void bar() {
        int[] arr = {1};
    }
}