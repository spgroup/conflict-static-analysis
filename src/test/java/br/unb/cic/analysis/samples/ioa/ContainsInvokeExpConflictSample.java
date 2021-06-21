package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, m():8] --> [right, m():10]
public class ContainsInvokeExpConflictSample {
    private int x, y;

    public void m() {
        x = 0; // LEFT
        y = 0;
        x = foo(); // RIGHT
    }

    private int foo() {
        return 1;
    }


}