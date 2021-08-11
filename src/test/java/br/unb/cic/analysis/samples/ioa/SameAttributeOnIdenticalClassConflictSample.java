package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, setFoo():18] --> [right, setFoo():18]
public class SameAttributeOnIdenticalClassConflictSample {

    public static void main(String[] args) {
        TestObject testObject = new TestObject();

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