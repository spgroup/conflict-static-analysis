package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class StaticClassFieldNotConflictSample {
    public static int y;

    public static void main(String[] args) {
        foo();           //left
        base();          //base
        bar();           //right
    }

    private static void foo() {
        y = 3;
    }

    private static void base() {
        y = 1;
    }

    private static void bar() {
        y = 4;
    }
}
