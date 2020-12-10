package br.unb.cic.analysis.samples;

public class RecursiveDefinitionSample02 {
    public static void foo() {
        blah();   // LEFT
    }

    public static void blah() {
        int x = 10;
        ugly(x);
    }

    public static void ugly(int z) {
        System.out.println(z);   // RIGHT
    }
}
