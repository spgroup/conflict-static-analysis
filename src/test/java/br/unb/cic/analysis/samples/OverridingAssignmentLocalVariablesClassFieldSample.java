package br.unb.cic.analysis.samples;

public class OverridingAssignmentLocalVariablesClassFieldSample {
    public static int y;

    public static void main(String[] args) {
        y = 3; //left
        int x = y + 1; //base
        y = 4; //right

    }
}
