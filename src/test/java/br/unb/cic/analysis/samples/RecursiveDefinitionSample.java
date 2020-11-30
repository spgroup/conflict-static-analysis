package br.unb.cic.analysis.samples;

public class RecursiveDefinitionSample {
    public static void foo() {
        blah();   // LEFT
    }

    public static void blah() {
        int x = 10;
        System.out.println(x);   // RIGHT
    }
}
