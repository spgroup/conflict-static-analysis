package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class OverridingAssignmentDifferentClassWithSameAttributeNotConflictInterProceduralSample {
    private ObjectExample objectExample;
    private ObjectExample2 objectExample2;

    public OverridingAssignmentDifferentClassWithSameAttributeNotConflictInterProceduralSample() {
        objectExample = new ObjectExample();
        objectExample2 = new ObjectExample2();
    }

    public void m() {
        objectExample.setFoo(1); // LEFT
        objectExample2.setFoo(2); // RIGHT
    }
}

class ObjectExample {
    private int foo;

    public void setFoo(int foo) {
        this.foo = foo;
    }
}

class ObjectExample2 {
    private int foo;

    public void setFoo(int foo) {
        this.foo = foo;
    }
}