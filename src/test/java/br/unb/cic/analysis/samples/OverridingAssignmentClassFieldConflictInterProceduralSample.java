package br.unb.cic.analysis.samples;

// Conflict: [left, m():8] --> [right, foo():13]
public class OverridingAssignmentClassFieldConflictInterProceduralSample {
    private int x;

    public void m() {
        x = 0; // LEFT
        foo(); // RIGHT
    }

    private void foo() {
        x = 1;
    }


}