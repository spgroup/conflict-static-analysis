package br.unb.cic.analysis.samples;

public class IntraproceduralDataFlowMain {

    public static void main(String args[]) {
        int x = random();           //source
        int y = x * 10;

        System.out.println(y);

        blah(x);                    //sink
    }

    private static int random() {
        return 10;
    }

    private static void blah(int z) {
        int x = random();            //source
        int y = z * x;

        System.out.println(y);

        x = 10;

        System.out.println(x);        //sink
    }
}
