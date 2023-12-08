package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, countDupWords():22] --> [right, countDupWhitespace():30]
public class LocalTestConflictSample {

    public void countFixes() {
        int x = 1; //RIGHT
        int y = 0;
        x = 2; // LEFT
    }

}