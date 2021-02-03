package br.unb.cic.analysis.samples;

// Conflict: [left, m():7] --> [right, foo():8]
public class OverridingAssignmentConcatMethodsConflictInterProceduralSample {

    public void m() {
        int x = foo() + bar(); // LEFT i3 = $i0 + $i1 - x = $stack2 + $stack3
        x = x + qux();         // RIGHT i4 = i3 + $i2 - x = x + $stack4
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