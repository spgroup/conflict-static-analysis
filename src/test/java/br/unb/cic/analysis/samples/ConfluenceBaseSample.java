package br.unb.cic.analysis.samples;

public class ConfluenceBaseSample {
    private String text;
    private Integer words;
    private Integer spaces;
    public int countFixes () {
        ConfluenceBaseSample confluenceBaseSample = new ConfluenceBaseSample();
        confluenceBaseSample.countDupWhitespace(); // LEFT
        confluenceBaseSample.countComments();
        confluenceBaseSample.countDupWords(); // RIGHT
        return confluenceBaseSample.spaces + confluenceBaseSample.words;
    }

    private void countDupWords() {
        this.words = new Integer(2);
    }

    private void countComments() {
    }

    private void countDupWhitespace() {
        this.spaces = new Integer(1);
    }
}
