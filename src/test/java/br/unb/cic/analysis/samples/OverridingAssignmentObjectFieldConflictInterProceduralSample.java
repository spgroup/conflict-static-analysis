package br.unb.cic.analysis.samples;

// Conflict: [left, ObjectFieldConflict.m():17] --> [right, ObjectFieldConflict.bar():21]
public class OverridingAssignmentObjectFieldConflictInterProceduralSample {
    public void m() {
        ObjectFieldConflict c = new ObjectFieldConflict();
        c.foo(1); // left
        ObjectFieldConflict d = c;
        d.bar(0);  //right
    }
}

class ObjectFieldConflict {
    private int at;

    public void foo(int a) {
        this.at = a + 1;
    }

    public void bar(int a) {
        this.at = a;
    }
}