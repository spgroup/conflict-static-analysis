package br.unb.cic.analysis.samples;

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