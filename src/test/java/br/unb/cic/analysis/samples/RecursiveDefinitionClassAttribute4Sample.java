package br.unb.cic.analysis.samples;

public class RecursiveDefinitionClassAttribute4Sample {
    int x;
    public void m() {
        a(); // left
        b(); //right
    }
    public void a() {
        o();
    }
    public void b() {
        n();
    }
    public void n() {
        System.out.println(x);
    }
    public void o() {
        x=0;
    }
}
