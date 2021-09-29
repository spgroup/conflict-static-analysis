package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class ClassFieldNotConflictSample2 {
    private int x;

    public static void main(String[] args) {
        ClassFieldNotConflictSample2 m = new ClassFieldNotConflictSample2();

        m.foo(); // LEFT
        m.base();
        m.bar(); // RIGHT
    }

    private void base() {
        x = 0;
    }

    private void foo() {
        x = 1;
    }

    private void bar() {
        x = 2;
    }
}