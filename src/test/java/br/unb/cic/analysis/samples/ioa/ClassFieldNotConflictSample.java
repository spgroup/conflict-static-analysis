package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class ClassFieldNotConflictSample {
    private int x;

    public static void main(String[] args) {
        ClassFieldNotConflictSample m = new ClassFieldNotConflictSample();

        m.x = 1; // LEFT
        m.base();
        m.foo(); // RIGHT
    }

    private void base() {
        x = 0;
    }

    private void foo() {
        x = 2;
    }

}