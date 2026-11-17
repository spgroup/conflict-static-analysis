package br.unb.cic.analysis.samples.ioa.PointsToDifferentMethodsSample;

public class Cx {
    private final Ox o;

    Cx() {
        o = new Px();
    }

    void m() {
        Cx c = new Cx();
        c.o.x = 1; // LEFT

        c.o.n(); // RIGHT
    }
}
