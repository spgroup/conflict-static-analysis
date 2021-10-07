package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, foo():15] --> [right, bar():19]
public class ClassFieldConflictSample2 {
    private int x;

    public static void main(String[] args) {
        ClassFieldConflictSample2 m = new ClassFieldConflictSample2();

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
