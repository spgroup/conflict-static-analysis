package br.unb.cic.analysis.samples;

public class RecursiveDefinitionSample03 {
    public static void foo() {
        int x = 0;
        int y = x + blah(x);     // RIGHT
        System.out.println(y);
    }

    public static int blah(int z) {
        int x = 10 + z;          // LEFT
        System.out.println(x);
        return 0;
    }
}
