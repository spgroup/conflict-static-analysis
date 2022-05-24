package br.unb.cic.analysis.samples;

public class InterproceduralDataflowField {

    int x = 0;

    public void foo() {
        this.x = 1; // source

        bar(); // sink
    }

    public void bar() {
        System.out.println(this.x);
    }

}
