package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class OverridingAssignmentLocalVariablesWithParameterNotConflictInterProceduralSample {
    public void m() {
        foo(0); // LEFT
        bar(1); // RIGHT
    }

    private void foo(int a) {
        int x = a;
    }

    private void bar(int a) {
        int x = a;
    }


}