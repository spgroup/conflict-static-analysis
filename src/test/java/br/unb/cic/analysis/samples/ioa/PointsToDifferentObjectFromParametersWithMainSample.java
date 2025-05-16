package br.unb.cic.analysis.samples.ioa;

// PA não reconhece instancias e P1 e P2
public class PointsToDifferentObjectFromParametersWithMainSample {

    public static void callRealisticRun() {
        m(new Point(), new Point());
    }

    private static void m(Point p1, Point p2) {
        p1.x = new Integer(10); // LEFT
        String s = "base";
        p2.x = new Integer(20);  // RIGHT
    }
}