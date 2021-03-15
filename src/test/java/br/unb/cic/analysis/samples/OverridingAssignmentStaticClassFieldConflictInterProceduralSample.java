package br.unb.cic.analysis.samples;

// Conflict: [left, foo():14] --> [right, bar():22]
public class OverridingAssignmentStaticClassFieldConflictInterProceduralSample {
    public static int y;

    public static void main(String[] args) {
        foo();           //left
        int x = base();  //base
        bar();           //right
    }

    private static void foo() {
        y = 3;
    }

    private static int base() {
        return y + 1;
    }

    private static void bar() {
        y = 4;
    }
}
