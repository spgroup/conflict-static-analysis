package br.unb.cic.analysis.samples.teste;

public class Text {
    private Report r;

    Text(Report r) {
        this.r = r;
    }

    void generateReport() {
        r.countDupWords(); // LEFT
        r.countComments();
        r.countDupWhiteSpace(); // RIGHT
    }
}