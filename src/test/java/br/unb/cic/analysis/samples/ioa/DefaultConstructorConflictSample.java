package br.unb.cic.analysis.samples.ioa;

public class DefaultConstructorConflictSample {
    private static int x;

    public void m() {
        DefaultConstructorConflictSample defaultConstructorConflictSample = new DefaultConstructorConflictSample(); // LEFT
        int y = 2;
        defaultConstructorConflictSample.x = 1; // RIGHT
    }
}