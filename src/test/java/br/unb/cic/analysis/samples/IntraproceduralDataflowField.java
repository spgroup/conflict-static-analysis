package br.unb.cic.analysis.samples;

public class IntraproceduralDataflowField {

    int x = 0;

    public void foo() {
        this.x = 1; // source

        System.out.println(this.x); // sink
    }

    public void bar() {
        this.x++; // source

        this.x = 10;

        System.out.println(this.x); // sink
    }

    public void m() {
        this.foo(); // source

        this.x = 1;

        System.out.println(this.x); // sink
    }

    public void x() {
        this.foo(); // source

        this.x = 1;

        this.foo(); // source

        System.out.println(this.x); // sink
    }

}
