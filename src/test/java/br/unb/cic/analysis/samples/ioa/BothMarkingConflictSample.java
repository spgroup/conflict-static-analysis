package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, foo():13] --> [right, foo():13]
public class BothMarkingConflictSample {
    private int x;

    public static void main(String[] args) {
        BothMarkingConflictSample m = new BothMarkingConflictSample();
        m.foo(); // LEFT
    }

    private void foo() {
        x = 1; // RIGHT
    }

}