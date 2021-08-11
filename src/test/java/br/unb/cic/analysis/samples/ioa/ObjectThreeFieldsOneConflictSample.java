package br.unb.cic.analysis.samples.ioa;

import br.unb.cic.analysis.samples.OverridingAssignmentInstance2;

// Conflict: [left, baz():35] --> [right, bar():31]
public class ObjectThreeFieldsOneConflictSample {
    public OverridingAssignmentInstance2 a;
    public OverridingAssignmentInstance2 b;

    public static ObjectThreeFieldsOneConflictSample instanceLocal;

    public static void main(String[] args) {
        instanceLocal = new ObjectThreeFieldsOneConflictSample();

        instanceLocal.foo();   // left
        instanceLocal.bar();   // right in {instanceLocal.b.a.a, instanceLocal.b.a.b }
        instanceLocal.base();  // base
        instanceLocal.baz();   //left
        instanceLocal.qux();   //right
    }

    private void foo() {
        instanceLocal.b.a.a = instanceLocal.b.a.a + 3;
    }

    private void base() {
        instanceLocal.b.a.a = 7;
    }

    private void bar() {
        instanceLocal.b.a.b = 3;
    }

    private void baz() {
        instanceLocal.b.a.b = 4;
    }

    private void qux() {
        instanceLocal.b.a.a = 4;
    }
}