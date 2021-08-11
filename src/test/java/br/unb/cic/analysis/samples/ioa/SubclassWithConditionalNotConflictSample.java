package br.unb.cic.analysis.samples.ioa;

public class SubclassWithConditionalNotConflictSample {
    private static C c;

    public static void main(String[] args){
        int y = 1; // LEFT
        m(true);
    }

    public static void m(boolean b) {
        if(b){
            c = new C();
        }else {
            c = new D();
        }
        c.m();
    }
}

class C {
    public int x;

    C() {
        this.x = 0;
    }

    public void m() {
        foo(); // LEFT
    }

    public void foo() {
        this.x = 1; // LEFT
    }
}

class D extends C {
    D(){
        super();
    }
    public void m() {
        this.x = 2; // RIGHT
    }
}