package br.unb.cic.analysis.samples.ioa;

public class DefaultConstructorConflictSample {
    private static int x;

    public DefaultConstructorConflictSample() {

    }

    public void m() {
        DefaultConstructorConflictSample defaultConstructorConflictSample = new DefaultConstructorConflictSample();
        int y = 2;
        defaultConstructorConflictSample.x = 1;
    }
}