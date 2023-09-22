package br.unb.cic.analysis.samples.ioa;

// Not Conflict - Not implemented yet. You will need constant propagation.
// Currently detected as conflict: [left, m():9] --> [right, foo():14]
public class ClassFieldWithParameterNotConflictSample {
    private int x;

    public static void main(String[] args) {
        ClassFieldWithParameterNotConflictSample m =
                new ClassFieldWithParameterNotConflictSample();
        m.x = 0; // LEFT
        int a = 1;
        m.foo(m.x); // RIGHT
    }

    private void foo(int a) {
        x = a;
    }

}