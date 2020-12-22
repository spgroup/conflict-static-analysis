package br.unb.cic.analysis.samples;

// Conflict: [left, m():7] --> [right, foo():13]
public class OverridingAssignmentClassFieldConflictInterProceduralSample {
    private int x;

    public void m() {
        x = 0; // LEFT
        x = foo() + bar(); // RIGHT
    }

    private int foo() {
        return 1;
    }

    private int bar() {
        return 1;
    }

}