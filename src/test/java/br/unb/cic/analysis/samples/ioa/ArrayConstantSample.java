package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, foo():15] --> [right, bar():19]
public class ArrayConstantSample {

    private int[] arr;

    public static void main(String[] args) {
        ArrayConstantSample m = new ArrayConstantSample();
        m.arr = new int[]{0, 0, 0, 0, 0};
        m.foo(3); // LEFT
        int b = 0;
        m.bar(2); // RIGHT
    }

    private void foo(int i) {
        arr[i] = 1;
    }

    private void bar(int i) {
        arr[i] = 2;
    }

}