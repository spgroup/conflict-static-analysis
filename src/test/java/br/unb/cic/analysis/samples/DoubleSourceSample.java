package br.unb.cic.analysis.samples;

public class DoubleSourceSample {
    public static void main(String args[]) {
        int x = 10;
        int y = 20;

        x = x + 1;   // left

        System.out.println(args);

        y = y - 1;   // right

        System.out.println(x + y);
    }
}
