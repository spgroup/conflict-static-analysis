package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, countDupWords():22] --> [right, countDupWhitespace():30]
public class FieldRefTestConflictSample {

    public void countFixes() {
        Point p = new Point();
        p.x = 5; //RIGHT
        int y = 0;
        p.x = 2; // LEFT
    }

}