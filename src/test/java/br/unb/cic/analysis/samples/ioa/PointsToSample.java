package br.unb.cic.analysis.samples.ioa;

import java.util.ArrayList;
import java.util.List;

// Conflict: [left, m():11] --> [right, m():12]
public class PointsToSample {

    public void m() {
        Point p1 = new Point();
        p1.x = new Integer(10); // LEFT
        Point p2 = new Point();
        p2.x = new Integer(20);  // RIGHT

    }
}