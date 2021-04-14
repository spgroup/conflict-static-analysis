package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class OverridingAssignmentClassFieldNotConflictInterProceduralSample {
    private int x;

    public void m() {
        x = 0; // LEFT
        base();
        foo(); // RIGHT
    }

    private void base() {
        x = 0;
    }

    private void foo() {
        x = 1;
    }


}