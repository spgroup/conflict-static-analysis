package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, countDupWords():22] --> [right, countDupWhitespace():30]
public class BaseConflictSample {

    public String text;
    public int fixes, comments;

    public void callRealisticRun() {
        BaseConflictSample baseConflictSample = new BaseConflictSample();
        baseConflictSample.countFixes();
    }

    public int countFixes() {
        countDupWhitespace(); //RIGHT
        countComments();
        countDupWords(); // LEFT
        return fixes;
    }

    private void countDupWords() {
        fixes = fixes + 2;
    }

    private void countComments() {
        comments =  comments +1;
    }

    private void countDupWhitespace() {
        fixes = fixes + 1;
    }
}