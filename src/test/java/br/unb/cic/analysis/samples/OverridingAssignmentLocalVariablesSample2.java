package br.unb.cic.analysis.samples;

public class OverridingAssignmentLocalVariablesSample2 {

    public static void main(String[] args) {
        int x=0, y=0;

        x = 1;   // left
        y = 2;   // right
        x = 3;   // base
        y = x+1; // left
        x = 21;  // right

        System.out.println(x);
    }
}
