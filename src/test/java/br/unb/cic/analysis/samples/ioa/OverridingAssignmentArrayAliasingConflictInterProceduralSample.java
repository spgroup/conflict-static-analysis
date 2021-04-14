package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, foo():15] --> [right, bar():19]
public class OverridingAssignmentArrayAliasingConflictInterProceduralSample {
    private int[] arr, barr;

    public void m() {
        arr = new int[]{0, 0, 0, 0, 0};
        barr = arr;
        foo(); // LEFT
        bar(); // RIGHT
    }

    private void foo() {
        arr[1] = 1;
    }

    private void bar() {
        barr[2] = 2;
    }


}