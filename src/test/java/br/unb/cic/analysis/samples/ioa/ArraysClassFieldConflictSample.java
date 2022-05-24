package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, foo():14] --> [right, bar():18]
public class ArraysClassFieldConflictSample {
    public int[] y;

    public ArraysClassFieldConflictSample() {
        this.y = new int[]{0};
    }

    public static void main(String[] args) {
        ArraysClassFieldConflictSample m = new ArraysClassFieldConflictSample();
        m.foo();           //left
        m.bar();           //right
    }

    private void foo() {
        y[0] = 3;
    }

    private void bar() {
        y[0] = 4;
    }
}
