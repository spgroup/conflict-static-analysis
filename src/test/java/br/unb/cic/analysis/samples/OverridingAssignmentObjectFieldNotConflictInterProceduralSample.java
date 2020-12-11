package br.unb.cic.analysis.samples;

// Not Conflict
public class OverridingAssignmentObjectFieldNotConflictInterProceduralSample {
    public void m() {
        ObjectFieldNotConflict objectFieldNotConflict = new ObjectFieldNotConflict();
        objectFieldNotConflict.foo(1); // left
        ObjectFieldNotConflict objectFieldNotConflict2 = new ObjectFieldNotConflict();
        objectFieldNotConflict2.bar(0);  //right
    }
}

class ObjectFieldNotConflict {
    private int at;

    public void foo(int a) {
        this.at = a + 1;
    }

    public void bar(int a) {
        this.at = a;
    }
}