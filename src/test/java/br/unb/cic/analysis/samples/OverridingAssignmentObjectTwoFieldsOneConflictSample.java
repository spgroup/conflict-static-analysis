package br.unb.cic.analysis.samples;

public class OverridingAssignmentObjectTwoFieldsOneConflictSample {
    public OverridingAssignmentInstance a;
    public OverridingAssignmentInstance b;

    public static void main(String[] args) {
        OverridingAssignmentObjectTwoFieldsOneConflictSample instanceLocal = new OverridingAssignmentObjectTwoFieldsOneConflictSample();

        instanceLocal.b.a = instanceLocal.b.a + 3; // left
        instanceLocal.b.b = 3; // right
        instanceLocal.b.b = instanceLocal.b.a+3; // base
        instanceLocal.b.b = 4; //left
        instanceLocal.b.a = 4; //right
    }
}