package br.unb.cic.analysis.samples;

public class OverridingAssignmentObjectOneFieldOneConflictSample {
    public int a;
    public int b;

    public static void main(String[] args) {
        OverridingAssignmentObjectOneFieldOneConflictSample instanceLocal = new OverridingAssignmentObjectOneFieldOneConflictSample();

        instanceLocal.b = instanceLocal.b + 3; // left
        instanceLocal.a = 3; // right
        instanceLocal.a = instanceLocal.b+3; // base
        instanceLocal.a = 4; //left
        instanceLocal.b = 4; //right
    }
}