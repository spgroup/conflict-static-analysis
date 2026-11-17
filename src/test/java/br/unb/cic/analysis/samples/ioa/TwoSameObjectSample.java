package br.unb.cic.analysis.samples.ioa;
// Nesse caso, PA está dizendo que os pointsTo dos new Integer() tem interseção
// Not conflict
public class TwoSameObjectSample {

    public void m() {
        int x = new Integer(10); // LEFT
        int z = 1;
        int y = new Integer(20);  // RIGHT
    }
}