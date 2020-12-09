package br.unb.cic.analysis.samples;


public class OverridingAssignmentClassFieldNotConflictInterProceduralSample2 {
    private int x;

    public void m() {
        foo(); // LEFT
        base();
        bar(); // RIGHT
    }

    private void base() {
        x = 0;
    }

    private void foo() {
        x = 1;
    }

    private void bar() {
        x = 2;
    }
}