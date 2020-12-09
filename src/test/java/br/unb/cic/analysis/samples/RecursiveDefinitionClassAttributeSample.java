package br.unb.cic.analysis.samples;

public class RecursiveDefinitionClassAttributeSample {
    public int x;
    public void m() {
        x = 0; // left
        n(); //right
    }

    public void n() {
        System.out.println(x);
    }

}
