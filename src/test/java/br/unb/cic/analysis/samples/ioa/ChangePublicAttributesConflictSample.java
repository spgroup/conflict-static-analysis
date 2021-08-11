package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, m():9] --> [right, Result<init>():19]
public class ChangePublicAttributesConflictSample {

    public void m() {
        Result result = new Result(); // LEFT

        result.foo = 2;  // RIGHT
        result.bar = 1;  // LEFT
    }
}

class Result {
    public int foo;
    public int bar;

    Result() {
        this.foo = 0;
        this.bar = 0;
    }
}

