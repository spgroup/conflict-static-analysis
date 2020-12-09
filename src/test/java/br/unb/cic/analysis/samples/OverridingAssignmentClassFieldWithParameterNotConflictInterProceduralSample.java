package br.unb.cic.analysis.samples;


public class OverridingAssignmentClassFieldWithParameterNotConflictInterProceduralSample {
    private int x;

    public void m() {
        x = 0; // LEFT
        foo(x); // RIGHT
    }

    private void foo(int a) {
        x = a;
    }


}