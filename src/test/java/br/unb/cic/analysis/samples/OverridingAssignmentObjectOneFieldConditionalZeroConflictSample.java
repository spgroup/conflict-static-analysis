package br.unb.cic.analysis.samples;

public class OverridingAssignmentObjectOneFieldConditionalZeroConflictSample {
    public int a;
    public int b;

    public static void main(String[] args) {
        OverridingAssignmentObjectOneFieldConditionalZeroConflictSample instance = new OverridingAssignmentObjectOneFieldConditionalZeroConflictSample();
        int a =3, b=32;
        if (a>b){
            instance.b = 3; // left
        }else{
            instance.b = 4; //right
        }
    }
}