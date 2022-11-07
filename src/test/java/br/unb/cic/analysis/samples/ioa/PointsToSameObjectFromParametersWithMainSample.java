package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, m():11] --> [right, m():13]
public class PointsToSameObjectFromParametersWithMainSample {

    public static void callRealisticRun() {
        m(new Point(), new Point());
    }

    public static void m(Point p1, Point p2) {
        p1.x = new Integer(10); // LEFT
        String s = "base";
        p1.x = new Integer(20);  // RIGHT
    }
}