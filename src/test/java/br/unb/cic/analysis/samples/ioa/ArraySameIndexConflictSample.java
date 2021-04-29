package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, foo():14] --> [right, bar():18]
public class ArraySameIndexConflictSample {
    private int[] arr;

    public void m() {
        arr = new int[]{0, 0, 0, 0, 0};
        foo(); // LEFT
        bar(); // RIGHT
    }

    private void foo() {
        arr[1] = 1;
    }

    private void bar() {
        arr[1] = 2;
    }
}