package br.unb.cic.analysis.samples;

public class OverridingAssignmentLocalVariablesSample {

    public static void main(String[] args) {
        int x=0, y=0, w=1, z=3;

        x = 3; // left
        y = 3; // right
        x = x+1; //x = 3;// base
        y = 3; // left
        x = 21; // right

        System.out.println(x);
    }
}
