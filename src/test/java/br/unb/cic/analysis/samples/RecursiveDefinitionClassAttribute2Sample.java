package br.unb.cic.analysis.samples;

public class RecursiveDefinitionClassAttribute2Sample {
    public int x;
    public void m() {
        o(); // left
        System.out.println(x); //right
    }
    public void o() {
        x=0;
    }
}
