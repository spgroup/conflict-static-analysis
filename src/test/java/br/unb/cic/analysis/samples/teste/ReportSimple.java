package br.unb.cic.analysis.samples.teste;

public class ReportSimple implements Report {
    int dupWords = 0;
    int dupWhiteSpace = 0;

    @Override
    public void countDupWords() {
        dupWords = dupWords + 1;
    }

    @Override
    public void countDupWhiteSpace() {
        dupWhiteSpace = dupWhiteSpace + 1;
    }

    @Override
    public void countComments() {

    }


}