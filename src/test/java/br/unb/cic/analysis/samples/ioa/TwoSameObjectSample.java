package br.unb.cic.analysis.samples.ioa;

// Not conflict
public class TwoSameObjectSample {

    public void m() {
        int x = new Integer(10); // LEFT
        int z = 1;
        int y = new Integer(20);  // RIGHT
    }
}