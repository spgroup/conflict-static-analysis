package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, m():8] --> [right, m():10]
public class ContainsInvokeExpConflictSample {
    private int x, y;

    public static void main() {
        ContainsInvokeExpConflictSample m = new ContainsInvokeExpConflictSample();

        m.x = 0; // LEFT
        m.y = 0;
        m.x = m.foo(); // RIGHT
    }

    private int foo() {
        return 1;
    }


}