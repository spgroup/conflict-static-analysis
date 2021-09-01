package br.unb.cic.analysis.samples;

public class IntraproceduralDataflowField {

    int x = 0;

    public void foo() {
        this.x = 1; // source

        System.out.println(this.x); // sink
    }

}
