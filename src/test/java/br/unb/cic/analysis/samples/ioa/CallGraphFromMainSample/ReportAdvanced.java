package br.unb.cic.analysis.samples.ioa.CallGraphFromMainSample;

public class ReportAdvanced implements Report {
    int fixes = 0;

    @Override
    public void countDupWords() {
        fixes++;
    }

    @Override
    public void countDupWhiteSpace() {
        int count = 1;
        fixes = count;
    }

    @Override
    public void countComments() {

    }
}
