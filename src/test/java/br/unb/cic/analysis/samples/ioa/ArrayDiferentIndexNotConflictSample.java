package br.unb.cic.analysis.samples.ioa;

public class ArrayDiferentIndexNotConflictSample {
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
        arr[2] = 2;
    }


}