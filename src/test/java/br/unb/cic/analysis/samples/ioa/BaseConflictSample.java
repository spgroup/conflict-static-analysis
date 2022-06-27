package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, countDupWords():17] --> [right, countDupWhitespace():25]
public class BaseConflictSample {

    public String text;
    public int fixes;

    public int countFixes() {
        BaseConflictSample baseConflictSample = new BaseConflictSample();
        baseConflictSample.countDupWhitespace(); //RIGHT
        baseConflictSample.countComments();
        baseConflictSample.countDupWords(); // LEFT
        return baseConflictSample.fixes;
    }

    private void countDupWords() {
        fixes = fixes + 2;
    }

    private void countComments() {

    }

    private void countDupWhitespace() {
        fixes = fixes + 1;
    }
}