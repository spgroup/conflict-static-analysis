package br.unb.cic.analysis.samples;

// Conflict: [left, m():7] --> [right, foo():8]
public class OverridingAssignmentConcatMethodsConflictInterProceduralSample {

    public void m() {
        int x = foo() + bar(); // LEFT
        x = x + qux(); // RIGHT
        System.out.println(x);
    }

    private int foo() {
        return 1;
    }

    private int bar() {
        return 2;
    }

    private int qux() {
        return 3;
    }
}