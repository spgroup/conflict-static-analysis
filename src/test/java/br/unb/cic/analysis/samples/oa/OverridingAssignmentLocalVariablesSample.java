package br.unb.cic.analysis.samples.oa;

public class OverridingAssignmentLocalVariablesSample {
    public static int x, y;
    public static void main(String[] args) {

        x = 1;   // left
        y = 2;   // right
        x = x+1; // base
        y = 3;   // left
        x = 4;   // right

        System.out.println(x);
    }
}
