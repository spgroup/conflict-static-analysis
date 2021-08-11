package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, m():8] --> [right, foo():14]
public class OverridingAssignmentIfBranchConflictInterProceduralSample {
    private int x;

    public static void main() {
        OverridingAssignmentIfBranchConflictInterProceduralSample m =
                new OverridingAssignmentIfBranchConflictInterProceduralSample();
        m.x = 0; // LEFT
        m.foo(); // RIGHT
    }

    private void foo() {
        if (x >= 0) {
            x = 1;
        } else {
            int a = 0;
            // System.out.println(x)
        }
    }
}