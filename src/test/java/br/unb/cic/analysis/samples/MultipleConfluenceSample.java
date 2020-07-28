package br.unb.cic.analysis.samples;

public class MultipleConfluenceSample {
    public void multipleConfluencesToTheSameLine() {
        int x = 0;
        int y = 0;
        int z = 0;

        x = x + 1; // source

        y = y + 1; // source

        z = z + 1; // sink

        System.out.println(x);
        System.out.println(y);
        System.out.println(z);
        System.out.println(x + y + z);
    }
}
