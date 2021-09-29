package br.unb.cic.analysis.samples.ioa;
// Not Conflict
public class SubclassConflictSample {

    public static void main(String[] args) {
        A x = new A();
        int y = 0; // left
        x.m();  // right
    }

}
class A {
    public int x;

    A(){
        this.x = 0;
    }
    public void m() {
         this.x = 1;
    }
}

class B extends A {
    B(){
        super();
    }

    public void m() {
        this.x = 2;
    }
}