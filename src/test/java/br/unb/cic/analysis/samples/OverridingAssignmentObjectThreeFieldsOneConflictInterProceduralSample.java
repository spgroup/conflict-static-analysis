package br.unb.cic.analysis.samples;

// Conflict: [left, baz():33] --> [right, bar():29]
public class OverridingAssignmentObjectThreeFieldsOneConflictInterProceduralSample {
    public OverridingAssignmentInstance2 a;
    public OverridingAssignmentInstance2 b;

    public OverridingAssignmentObjectThreeFieldsOneConflictInterProceduralSample instanceLocal;

    public void m() {
        instanceLocal = new OverridingAssignmentObjectThreeFieldsOneConflictInterProceduralSample();

        foo();    // left
        bar();    // right in {instanceLocal.b.a.a, instanceLocal.b.a.b }
        base();  // base
        baz();   //left
        qux();   //right
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