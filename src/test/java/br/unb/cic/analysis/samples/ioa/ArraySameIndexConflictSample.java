package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, foo():15] --> [right, bar():19]
public class ArraySameIndexConflictSample {
    private int[] arr;

    public static void main(String[] args) {
        ArraySameIndexConflictSample m = new ArraySameIndexConflictSample();
        m.arr = new int[]{0, 0, 0, 0, 0};
        m.foo(); // LEFT
        m.bar(); // RIGHT
    }

    private void foo() {
        arr[1] = 1;
    }

    private void bar() {
        arr[1] = 2;
    }
}