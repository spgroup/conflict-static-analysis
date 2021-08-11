package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class SameAttributeOnIdenticalClassConflictSample {
    private TestObject testObject;

    public SameAttributeOnIdenticalClassConflictSample() {
        testObject = new TestObject();
    }

    public void m() {
        testObject.setFoo(1); // LEFT
        testObject.setFoo(2); // RIGHT
    }
}

class TestObject {
    private int foo;

    public void setFoo(int foo) {
        this.foo = foo;
    }
}