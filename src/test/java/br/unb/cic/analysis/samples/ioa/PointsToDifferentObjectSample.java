package br.unb.cic.analysis.samples.ioa;


public class PointsToDifferentObjectSample {

    public void m() {
        Point p1 = new Point();
        p1.x = new Integer(10); // LEFT
        Point p2 = new Point();
        p2.x = new Integer(20);  // RIGHT

    }
}