package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, foo():13] --> [right, foo():13]
public class RecursiveCallConflictSample {

    public static void main(String[] args) {
        RecursiveCallConflictSample recursiveCallConflictSample = new RecursiveCallConflictSample();
        recursiveCallConflictSample.foo(1); // LEFT
    }

    private int foo(int n) {
        if (n == 1) {
            return n;
        }
        return foo(n - 1) * n; // RIGHT
    }
}