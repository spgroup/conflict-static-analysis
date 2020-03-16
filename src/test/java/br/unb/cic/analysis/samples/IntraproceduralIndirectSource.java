package br.unb.cic.analysis.samples;

public class IntraproceduralIndirectSource {

    public void m() {
        int x = 1000;
        x = random();          // source (left)
        System.out.println(x);

        int y = x + 1;

        System.out.print(y);     // sink
    }

    private int random() {
        return 0;
    }
}
