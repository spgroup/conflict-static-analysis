package br.unb.cic.analysis.samples;

public class OverridingAssignmentObjectOneFieldTwoConflictsSample {
    public int a;
    public int b;

    public static void main(String[] args) {
        OverridingAssignmentObjectOneFieldTwoConflictsSample instanceLocal = new OverridingAssignmentObjectOneFieldTwoConflictsSample();

        instanceLocal.b = instanceLocal.b + 3; // left
        instanceLocal.a = 3; // right

        instanceLocal.a = 4; //left
        instanceLocal.b = 4; //right
    }
}