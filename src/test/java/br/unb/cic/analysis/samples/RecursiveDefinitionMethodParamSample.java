package br.unb.cic.analysis.samples;

public class RecursiveDefinitionMethodParamSample {
    public static void m() {
        int x = 0; // left
        n(x);
    }

    public static void n(int a) {
        System.out.println(a); // right
    }
}
