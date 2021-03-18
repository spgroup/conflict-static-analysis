package br.unb.cic.analysis.samples;

// Conflict: [left, m():13] --> [right, m():17]
public class OverridingAssignmentChangePublicAttributesConflictInterProceduralSample {

    private Boolean foo = null;
    private Boolean bar = null;

    public void m() {
        Result result = new Result(); // LEFT

        result.foo = this.foo != null
                ? 1
                : 2;  // RIGHT


        result.bar = this.bar != null ? 1 : 2;  // LEFT
    }
}


class Result {
    public int foo;
    public int bar;

    public void setBar(int bar) {
        this.bar = bar;
    }

    public void setFoo(int foo) {
        this.foo = foo;
    }
}