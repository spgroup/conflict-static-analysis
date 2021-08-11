package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, m():8] --> [right, foo():13]
public class OverridingAssignmentClassFieldConflictInterProceduralSample {
    private int x;

    public static void main(String[] args) {
        OverridingAssignmentClassFieldConflictInterProceduralSample m =
                new OverridingAssignmentClassFieldConflictInterProceduralSample();
        m.x = 0; // LEFT
        m.foo(); // RIGHT
    }

    private void foo() {
        x = 1;
    }
}