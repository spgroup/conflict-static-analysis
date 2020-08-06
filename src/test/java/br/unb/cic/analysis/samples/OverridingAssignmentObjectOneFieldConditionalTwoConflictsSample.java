package br.unb.cic.analysis.samples;

public class OverridingAssignmentObjectOneFieldConditionalTwoConflictsSample {
    public int a;
    public int b;

    public static void main(String[] args) {
        OverridingAssignmentObjectOneFieldConditionalTwoConflictsSample instance = new OverridingAssignmentObjectOneFieldConditionalTwoConflictsSample();
        int a =3, b=32;
        if (a>b){
            instance.b = 3; // left
        }else{
            instance.a = 4; //left
        }
        instance.b = 7; // right
        instance.a = 3; // right
    }
}