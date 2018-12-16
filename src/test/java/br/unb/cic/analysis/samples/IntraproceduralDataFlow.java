package br.unb.cic.analysis.samples;

public class IntraproceduralDataFlow {

    public void foo() {
        int x = random();           //source
        int y = x * 10;

        System.out.println(y);

        blah(x);                    //sink
    }

    private int random() {
        return 10;
    }

    private void blah(int z) {
        int x = random();            //source
        int y = z * x;

        System.out.println(y);

        x = 10;

        System.out.println(x);        //sink
    }
}
