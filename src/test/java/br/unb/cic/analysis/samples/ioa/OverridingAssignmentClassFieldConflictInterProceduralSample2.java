package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, foo():13] --> [right, bar():17]
public class OverridingAssignmentClassFieldConflictInterProceduralSample2 {
    private int x;

    public static void main(String[] args) {
        OverridingAssignmentClassFieldConflictInterProceduralSample2 m =
                new OverridingAssignmentClassFieldConflictInterProceduralSample2();
        m.foo(); // LEFT
        m.bar(); // RIGHT
    }

    private void foo() {
        x = 0;
    }

    private void bar() {
        x = 1;
    }

}
