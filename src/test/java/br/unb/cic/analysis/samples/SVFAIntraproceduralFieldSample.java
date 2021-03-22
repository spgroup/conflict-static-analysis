package br.unb.cic.analysis.samples;

public class SVFAIntraproceduralFieldSample {
    // Foo x;

    public void foo() {
        Foo x = new Foo();           // LEFT
        System.out.println(x);   // RIGHT
    }

    class Foo {

    }
}
