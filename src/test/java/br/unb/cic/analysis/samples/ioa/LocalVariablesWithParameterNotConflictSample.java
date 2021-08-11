package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class LocalVariablesWithParameterNotConflictSample {
    public static void main(String[] args) {
        LocalVariablesWithParameterNotConflictSample m = new LocalVariablesWithParameterNotConflictSample();

        m.foo(0); // LEFT
        m.bar(1); // RIGHT
    }

    private void foo(int a) {
        int x = a;
    }

    private void bar(int a) {
        int x = a;
    }


}