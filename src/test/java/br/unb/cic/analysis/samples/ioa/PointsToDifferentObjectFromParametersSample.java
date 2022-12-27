package br.unb.cic.analysis.samples.ioa;

// Not conflict
public class PointsToDifferentObjectFromParametersSample {

    public void m(Point p1, Point p2) {
        p1.x = new Integer(10); // LEFT
        String s = "base";
        p2.x = new Integer(20);  // RIGHT
    }
}