package br.unb.cic.analysis.samples.ioa.CallGraphFromMainSample;

public class Main {
    public static void main(String[] ars) {
        Report r = new ReportSimple();
        Text t = new Text(r);
        t.generateReport();
    }
}
