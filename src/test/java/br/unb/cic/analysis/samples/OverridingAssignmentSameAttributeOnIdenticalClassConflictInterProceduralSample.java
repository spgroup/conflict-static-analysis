package br.unb.cic.analysis.samples;

// Conflict: [left, m():12] --> [right, foo():13]
public class OverridingAssignmentSameAttributeOnIdenticalClassConflictInterProceduralSample {
    private TestObject testObject;

    public OverridingAssignmentSameAttributeOnIdenticalClassConflictInterProceduralSample() {
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