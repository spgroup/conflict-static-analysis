package br.unb.cic.analysis.samples.MultiCG;

public class D extends A {

    public void m(A a) {
        a.x = 4;
    }

    public void n() {
        super.x = 5;
    }
}
