package br.unb.cic.analysis.samples.ioa.CallGraphFromMainSample;

public class Text {
    private Report r;

    Text() {
        r = new ReportSimple();
    }

    void generateReport() {
        r.countDupWords(); // LEFT
        r.countComments();
        r.countDupWhiteSpace(); // RIGHT
    }
}
