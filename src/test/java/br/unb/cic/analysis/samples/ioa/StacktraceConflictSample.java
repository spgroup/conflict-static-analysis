package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, m():11] --> [right, m():12]
public class StacktraceConflictSample {
    private int x, y;

    public void main() {
        StacktraceConflictSample m = new StacktraceConflictSample();

        m.foo(); // LEFT
        m.bar(); // RIGHT
    }

    private void foo() {
        qux();
        System.out.println();
        y = 1;
    }

    private void bar() {
        x = 2;
        y = 2;
    }

    private void qux() {
        x = 3;
        System.out.println();
    }
}