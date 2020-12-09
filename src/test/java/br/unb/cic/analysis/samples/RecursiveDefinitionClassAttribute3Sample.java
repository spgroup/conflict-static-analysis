package br.unb.cic.analysis.samples;

public class RecursiveDefinitionClassAttribute3Sample {
    int x;
    public void m() {
        o(); // left
        n(); // right
    }
    public void n() {
        System.out.println(x);
    }
    public void o() {
        x=0;
    }
}
