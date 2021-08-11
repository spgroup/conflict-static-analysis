package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, foo():12] --> [right, foo():12]
public class BothMarkingConflictSample {
    private static int x;

    public void main() {
        foo(); // LEFT
    }

    private static void foo() {
        x = 1; // RIGHT
    }

}