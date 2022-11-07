package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, m():8] --> [right, m():10]
public class PointsToSameArraySample {

    public void m() {
        int[] a = new int[2];
        a[0] = 5; // LEFT
        int[] b = a;
        b[0] = 6;  // RIGHT

    }
}