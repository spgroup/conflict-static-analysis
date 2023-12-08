package br.unb.cic.analysis.samples.oa;

public class OverridingAssignmentObjectOneFieldZeroConflictSample {
    public int a;
    public int b;

    public static void main(String[] args) {
        OverridingAssignmentObjectOneFieldZeroConflictSample instanceLocal = new OverridingAssignmentObjectOneFieldZeroConflictSample();

        instanceLocal.b = instanceLocal.b + 3; // left
        instanceLocal.a = 3; // right
        instanceLocal.a = instanceLocal.b+3; // base
        instanceLocal.b = instanceLocal.b+3; // base
        instanceLocal.a = 4; //left
        instanceLocal.b = 4; //right
    }
}