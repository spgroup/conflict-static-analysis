package br.unb.cic.analysis.samples.ioa;


public class PointsToSameObjectFromParametersSample2 {

    public void m(Point p1, Point p2) {
        p1.x = new Integer(10); // LEFT
        p1 = p2;
        p1.x = new Integer(20);  // RIGHT
    }
}