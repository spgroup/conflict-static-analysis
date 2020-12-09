package br.unb.cic.analysis.samples;

public class RecursiveDefinitionMethodParam2Sample {
    public static void m() {
        int x = 0; // left
        n(x); //right
    }

    public static void n(int a) {
        System.out.println(a);
    }
}
