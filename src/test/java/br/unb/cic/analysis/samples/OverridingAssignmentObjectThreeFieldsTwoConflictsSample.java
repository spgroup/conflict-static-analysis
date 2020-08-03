package br.unb.cic.analysis.samples;

public class OverridingAssignmentObjectThreeFieldsTwoConflictsSample {
    public OverridingAssignmentInstance2 a;
    public OverridingAssignmentInstance2 b;

    public static void main(String[] args) {
        OverridingAssignmentObjectThreeFieldsTwoConflictsSample instanceLocal = new OverridingAssignmentObjectThreeFieldsTwoConflictsSample();

        instanceLocal.b.a.a = instanceLocal.b.a.a + 3; // left
        instanceLocal.b.a.b = 3; // right

        instanceLocal.b.a.b = 4; //left
        instanceLocal.b.a.a = 4; //right
    }
}