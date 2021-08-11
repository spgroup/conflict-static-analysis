package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, main():10] --> [right, foo():15]
public class ClassFieldConflictSample {
    private int x;

    public static void main(String[] args) {
        ClassFieldConflictSample m =
                new ClassFieldConflictSample();
        m.x = 0; // LEFT
        m.foo(); // RIGHT
    }

    private void foo() {
        x = 1;
    }
}