package br.unb.cic.analysis.samples;

public class LineReference {

    public static void main(String args[]) {
        int u = 1;
        int x = 5;
        int y = 10;

        int z = inc(x + y);
        int w = x +
                y +
                z ;

        System.out.println(w);
        System.out.println(u);
    }

    public static int inc(int x) {
        return x + 1;
    }

}
