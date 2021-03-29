package br.unb.cic.analysis.samples;

public class RecursiveDefinitionSample01 {
    public static void foo() {
        blah();   // LEFT
    }

    public static void blah() {
        int x = 10;
        System.out.println(x);   // RIGHT
    }

    // r0 = System.out  // LEFT
    // r0.println();    // RIGHT
}
