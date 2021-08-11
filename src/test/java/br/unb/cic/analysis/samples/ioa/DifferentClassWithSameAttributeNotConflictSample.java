package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class DifferentClassWithSameAttributeNotConflictSample {
    private ObjectExample objectExample;
    private ObjectExample2 objectExample2;

    public DifferentClassWithSameAttributeNotConflictSample() {
        objectExample = new ObjectExample();
        objectExample2 = new ObjectExample2();
    }

    public static void main(String[] args) {
        DifferentClassWithSameAttributeNotConflictSample m = new DifferentClassWithSameAttributeNotConflictSample();

        m.objectExample.setFoo(1); // LEFT
        m.objectExample2.setFoo(2); // RIGHT
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