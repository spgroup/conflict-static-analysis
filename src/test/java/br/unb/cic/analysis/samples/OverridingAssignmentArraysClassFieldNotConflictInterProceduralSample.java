package br.unb.cic.analysis.samples;

// Not Conflict
public class OverridingAssignmentArraysClassFieldNotConflictInterProceduralSample {
    public static int[] y;

    public static void main(String[] args) {
        foo();           //left
        base();          //base
        bar();           //right
    }

    private static void foo() {
        y[0] = 3;
    }

    private static void base() {
        y[1] = 1;
    }

    private static void bar() {
        y[3] = 4;
    }
}
