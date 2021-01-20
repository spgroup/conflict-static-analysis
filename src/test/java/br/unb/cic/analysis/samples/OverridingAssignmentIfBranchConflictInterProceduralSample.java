package br.unb.cic.analysis.samples;

// Conflict: [left, m():8] --> [right, foo():14]
public class OverridingAssignmentIfBranchConflictInterProceduralSample {
    private int x;

    public void m() {
        x = 0; // LEFT
        foo(); // RIGHT
    }

    private void foo() {
        if (x >= 0) {
            x = 1;
        } else {
            int a = 0;
            // System.out.println(x)
        }
        ;
    }
}