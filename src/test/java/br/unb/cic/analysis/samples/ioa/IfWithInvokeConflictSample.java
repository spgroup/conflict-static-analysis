package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, m():8] --> [right, foo():16]
public class IfWithInvokeConflictSample {
    private int x, y;

    public void m() {
        x = 0; // LEFT
        y = 0;
        if (foo()) { // RIGHT
            y = 1;
        }
    }

    private boolean foo() {
        x = 1;
        return true;
    }


}