package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, m():8] --> [right, foo():16]
public class IfWithInvokeConflictSample {
    private int x, y;

    public static void main(String[] args) {
        IfWithInvokeConflictSample m = new IfWithInvokeConflictSample();
        m.x = 0; // LEFT
        m.y = 0;
        if (m.foo()) { // RIGHT
            m.y = 1;
        }
    }

    private boolean foo() {
        x = 1;
        return true;
    }


}