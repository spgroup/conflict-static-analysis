package br.unb.cic.analysis.samples.ioa;


public class PointsToSameObjectFromParametersSample3 {

    public void m(Point p1, Point p2) {
        p1.x = new Integer(10); // LEFT
        p1 = new Point();
        p1.x = new Integer(20);  // RIGHT
    }
}