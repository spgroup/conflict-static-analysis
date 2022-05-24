package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class LocalVariablesNotConflictSample2 {
    public static void main(String[] args) {
        LocalVariablesNotConflictSample2 m = new LocalVariablesNotConflictSample2();

        int x = 0;  // LEFT
        m.foo(x);     // RIGHT

    }

    private void foo(int a) {
        a = 3;
    }


}