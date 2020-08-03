package br.unb.cic.analysis.samples;

public class InterproceduralConfluenceSample {
    private int counter = 0;

    public void interproceduralSample () {
        int x = 0;
        int y = 0;

        x = x + 1; // source

        y = y + 2; // sink

        incrementCounter(x);
        incrementCounter(y);
    }

    public void incrementCounter(int value) {
        counter += value;
    }
}
