package br.unb.cic.analysis.samples.dfp.CallGraphFromMainSample;

public class ReportSimple implements Report {
    int dupWords = 0;
    int dupWhiteSpace = 0;

    @Override
    public void countDupWords() {
        dupWords++;
    }

    @Override
    public void countDupWhiteSpace() {
        dupWhiteSpace++;
    }

    @Override
    public void countComments() {

    }


}
