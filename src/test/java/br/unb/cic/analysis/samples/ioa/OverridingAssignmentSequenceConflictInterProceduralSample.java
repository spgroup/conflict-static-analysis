package br.unb.cic.analysis.samples.ioa;

// Conflict: [{left, m():7 --> right, m():8}, {left, m():9 --> right, m():8}]
public class OverridingAssignmentSequenceConflictInterProceduralSample {

    public void m() {
        int x = 0; // LEFT
        x = 1;     // RIGHT
        x = 2;     // LEFT
    }


}