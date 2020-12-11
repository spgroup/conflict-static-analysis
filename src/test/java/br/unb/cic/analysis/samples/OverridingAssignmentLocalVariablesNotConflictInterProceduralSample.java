package br.unb.cic.analysis.samples;

// Not Conflict
public class OverridingAssignmentLocalVariablesNotConflictInterProceduralSample {
    public void m() {
        foo(); // LEFT
        bar(); // RIGHT
    }

    private void foo() {
        int x = 0;
    }

    private void bar() {
        int x = 1;
    }


}