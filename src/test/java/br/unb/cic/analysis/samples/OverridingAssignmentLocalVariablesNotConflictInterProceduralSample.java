package br.unb.cic.analysis.samples;


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