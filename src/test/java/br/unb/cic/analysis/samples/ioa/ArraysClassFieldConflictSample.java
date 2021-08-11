package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, foo():13] --> [right, bar():17]
public class ArraysClassFieldConflictSample {
    public static int[] y;

    public static void main(String[] args) {
        foo();           //left
        bar();           //right
    }

    private static void foo() {
        y[0] = 3;
    }

    private static void bar() {
        y[0] = 4;
    }
}
