package br.unb.cic.analysis.samples;

// Conflict: [left, foo():13] --> [right, bar():17]
public class OverridingAssignmentClassFieldConflictInterProceduralSample2 {
    private int x;

    public void m() {
        foo(); // LEFT
        bar(); // RIGHT
    }

    private void foo() {
        x = 0;
    }

    private void bar() {
        x = 1;
    }

}
