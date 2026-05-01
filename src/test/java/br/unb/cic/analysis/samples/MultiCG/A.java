package br.unb.cic.analysis.samples.MultiCG;

public class A {
    public int x;
    public A fieldA;

    public void m(A a) {
        a.x = 1;
    }

    public void n() {
        int y = 2;
    }

}
