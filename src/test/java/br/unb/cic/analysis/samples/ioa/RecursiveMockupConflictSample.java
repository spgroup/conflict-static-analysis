package br.unb.cic.analysis.samples.ioa;

// Conflict: [{left, foo():14] --> [right, foo():14}, {left, foo():14] --> [right, foo():14}, {left, foo():14] --> [right, foo():14}]
public class RecursiveMockupConflictSample {
    private int x;

    public static void main(String[] args) {
        RecursiveMockupConflictSample m = new RecursiveMockupConflictSample();
        m.foo(); // LEFT
        m.foo(); // Right
    }

    private void foo() {
        this.x += 1;
    }
}