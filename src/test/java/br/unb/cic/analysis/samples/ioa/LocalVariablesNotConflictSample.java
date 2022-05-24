package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class LocalVariablesNotConflictSample {
    public static void main(String[] args) {
        LocalVariablesNotConflictSample m = new LocalVariablesNotConflictSample();

        m.foo(); // LEFT
        m.bar(); // RIGHT
    }

    private void foo() {
        int x = 0;
    }

    private void bar() {
        int x = 1;
    }


}