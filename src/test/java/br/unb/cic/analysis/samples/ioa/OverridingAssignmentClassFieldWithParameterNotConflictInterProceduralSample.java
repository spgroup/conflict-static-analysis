package br.unb.cic.analysis.samples.ioa;

// Not Conflict - Not implemented yet. You will need constant propagation.
// Currently detected as conflict: [left, m():9] --> [right, foo():14]
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