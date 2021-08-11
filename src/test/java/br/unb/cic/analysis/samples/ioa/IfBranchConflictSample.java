package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, main():10] --> [right, foo():16]
public class IfBranchConflictSample {
    private int x;

    public static void main() {
        IfBranchConflictSample m =
                new IfBranchConflictSample();
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