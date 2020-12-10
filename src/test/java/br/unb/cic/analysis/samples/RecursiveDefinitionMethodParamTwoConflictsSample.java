package br.unb.cic.analysis.samples;
//2 conflicts
public class RecursiveDefinitionMethodParamTwoConflictsSample {
    public static void m() {
        int x = 0; // left
        n(x); //right
    }

    public static void n(int a) {
        System.out.println(a); //right
    }
}
