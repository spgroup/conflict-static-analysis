package br.unb.cic.analysis.samples;

public class DFPMotivating {
    public String text;
    public int fixes, comments;

    public void generateReport(){
        DFPMotivating inst = new DFPMotivating();
        inst.countDupWhitespace(); //RIGHT
        inst.countComments();
        inst.countDupWords(); // LEFT
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
