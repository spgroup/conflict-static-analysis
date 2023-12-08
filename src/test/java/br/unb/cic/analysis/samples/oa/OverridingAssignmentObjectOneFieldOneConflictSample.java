package br.unb.cic.analysis.samples.oa;

public class OverridingAssignmentObjectOneFieldOneConflictSample {
    public int a;
    public int b;

    public static void main(String[] args) {
        OverridingAssignmentObjectOneFieldOneConflictSample instance = new OverridingAssignmentObjectOneFieldOneConflictSample();

        instance.b = instance.b + 3; // left
        instance.a = 3; // right
        instance.a = instance.b+3; // base
        instance.a = 4; //left
        instance.b = 4; //right
    }
}