package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, foo():15] --> [right, bar():19]
public class OverridingAssignmentArrayAliasingConflictInterProceduralSample {
    private int[] arr, barr;

    public static void main(String[] args) {
        OverridingAssignmentArrayAliasingConflictInterProceduralSample m =
                new OverridingAssignmentArrayAliasingConflictInterProceduralSample();
        m.arr = new int[]{0, 0, 0, 0, 0};
        m.barr = m.arr;
        m.foo(); // LEFT
        m.bar(); // RIGHT
    }

    private void foo() {
        arr[1] = 1;
    }

    private void bar() {
        barr[1] = 2;
    }


}