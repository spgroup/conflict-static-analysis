package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class DifferentAttributeOnIdenticalClassNotConflictSample {
    private ObjectSample objectSample1, objectSample2;

    public DifferentAttributeOnIdenticalClassNotConflictSample() {
        objectSample1 = new ObjectSample();
        objectSample2 = new ObjectSample();
    }

    public static void main(String[] args) {
        DifferentAttributeOnIdenticalClassNotConflictSample m =
                new DifferentAttributeOnIdenticalClassNotConflictSample();

        m.objectSample1.setFoo(1); // LEFT
        m.objectSample2.setBar(2); // RIGHT
    }
}

class ObjectSample {
    private int foo;
    private int bar;

    public void setBar(int bar) {
        this.bar = bar;
    }

    public void setFoo(int foo) {
        this.foo = foo;
    }
}