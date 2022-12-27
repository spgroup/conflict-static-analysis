package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, m():8] --> [right, m():10]
public class PointsToSameArrayIndexSample {

    public void m(int i) {
        int[] a = new int[2];
        a[i] = 5; // LEFT
        int[] b = a;
        b[i] = 6;  // RIGHT

    }
}