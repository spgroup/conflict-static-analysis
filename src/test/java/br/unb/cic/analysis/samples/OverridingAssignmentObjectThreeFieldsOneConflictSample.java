package br.unb.cic.analysis.samples;

public class OverridingAssignmentObjectThreeFieldsOneConflictSample {
    public OverridingAssignmentInstance2 a;
    public OverridingAssignmentInstance2 b;

    public static void main(String[] args) {
        OverridingAssignmentObjectThreeFieldsOneConflictSample instanceLocal = new OverridingAssignmentObjectThreeFieldsOneConflictSample();

        instanceLocal.b.a.a = instanceLocal.b.a.a + 3; // left
        instanceLocal.b.a.b = 3; // right in {instanceLocal.b.a.a, instanceLocal.b.a.b }
        instanceLocal.b.a.a = 7; // base
        instanceLocal.b.a.b = 4; //left
        instanceLocal.b.a.a = 4; //right
    }
}