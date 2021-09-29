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

    public void a() {
        MockClass mock = new MockClass();

        mock.setA(1); // source

        int y = mock.getA() + 1;

        System.out.println(y); // sink
    }
}

class MockClass {
    public int a;

    public void setA(int a) {
        this.a = a;
    }

    public int getA() {
        return a;
    }
}