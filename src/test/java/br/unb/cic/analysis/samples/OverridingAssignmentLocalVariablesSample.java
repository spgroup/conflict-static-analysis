package br.unb.cic.analysis.samples;

public class OverridingAssignmentLocalVariablesSample {
    public static int x;
    public static void main(String[] args) {
        int y=0;

        x = 1;   // left
        y = 2;   // right
        x = x+1; // base
        y = 3;   // left
        x = 4;   // right

        System.out.println(x);
    }
}
