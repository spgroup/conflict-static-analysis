package br.unb.cic.analysis.samples.dfp.CallGraphFromMainSample;

public class Main {
    public static void main(String[] ars) {
        Report r = new ReportAdvanced();
        Text t = new Text(r);
        t.generateReport();
    }
}
