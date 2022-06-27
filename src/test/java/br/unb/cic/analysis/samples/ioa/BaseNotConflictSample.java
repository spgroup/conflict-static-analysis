package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class BaseNotConflictSample {

    public String text;
    public int fixes;

    public int countFixes() {
        BaseNotConflictSample baseConflictSample = new BaseNotConflictSample();
        baseConflictSample.countDupWhitespace(); //RIGHT
        baseConflictSample.countComments();
        baseConflictSample.countDupWords(); // LEFT
        return baseConflictSample.fixes;
    }

    private void countDupWords() {
        this.fixes = this.fixes + 2;
    }

    private void countComments() {
        this.fixes = 0;
    }

    private void countDupWhitespace() {
        this.fixes = this.fixes + 1;
    }
}