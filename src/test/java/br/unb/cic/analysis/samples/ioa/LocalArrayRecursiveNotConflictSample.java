package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class LocalArrayRecursiveNotConflictSample {
    public static void main(String[] args) {
        LocalArrayRecursiveNotConflictSample m = new LocalArrayRecursiveNotConflictSample();

        m.foo(); // LEFT
        int y = 0;
        m.bar(); // RIGHT
    }

    private void foo() {
        int[] arr = {0};
    }

    private void bar() {
        int[] arr = {1};
    }
}