package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, m():11] --> [right, m():12]
public class PointsToSameObjectSample {

    public void m() {
        Point p1 = new Point();
        p1.x = new Integer(10); // LEFT
        Point p2 = p1;
        p2.x = new Integer(20);  // RIGHT

    }
}