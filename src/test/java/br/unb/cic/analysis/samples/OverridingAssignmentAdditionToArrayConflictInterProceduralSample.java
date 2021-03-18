package br.unb.cic.analysis.samples;

import java.util.ArrayList;
import java.util.List;

// Conflict: [left, m():11] --> [right, m():12]
public class OverridingAssignmentAdditionToArrayConflictInterProceduralSample {

    public void m() {
        List<String> validParameterAnnotations = new ArrayList<String>();
        validParameterAnnotations.add("LEFT"); // LEFT
        validParameterAnnotations.add("RIGHT");  // RIGHT
    }
}