package br.unb.cic.analysis.samples.ioa;

import java.util.ArrayList;
import java.util.List;

// Conflict: [left, m():11] --> [right, m():12]
public class AdditionToArrayConflictSample {

    public void m() {
        List<String> validParameterAnnotations = new ArrayList<String>();
        validParameterAnnotations.add("LEFT"); // LEFT
        validParameterAnnotations.add("RIGHT");  // RIGHT
    }
}