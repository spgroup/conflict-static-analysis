package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class BaseNotConflictSample {

    public String text;

    public void callRealisticRun() {
        BaseNotConflictSample baseNotConflictSample = new BaseNotConflictSample();
        baseNotConflictSample.cleanText();
    }

    public void cleanText() {
        normalizeWhitespaces(); //RIGHT
        removeComments();
        removeDuplicatedWords(); // LEFT
    }

    private void removeDuplicatedWords() {
        text = text + "removeDuplicatedWords";
    }

    private void removeComments() {
        text = text + "removeComments";
    }

    private void normalizeWhitespaces() {
        text = text + "normalizeWhitespaces";
    }
}