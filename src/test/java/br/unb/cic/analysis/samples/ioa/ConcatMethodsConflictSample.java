package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, main():10] --> [right, main():11]
public class ConcatMethodsConflictSample {

    public static void main(String[] args) {
        ConcatMethodsConflictSample m = new ConcatMethodsConflictSample();

        int x = m.foo() + m.bar(); // LEFT i3 = $i0 + $i1 - x = $stack2 + $stack3
        x = x + m.qux();         // RIGHT i4 = i3 + $i2 - x = x + $stack4
    }

    private int foo() {
        return 1;
    }

    private int bar() {
        return 2;
    }

    private int qux() {
        return 3;
    }
}