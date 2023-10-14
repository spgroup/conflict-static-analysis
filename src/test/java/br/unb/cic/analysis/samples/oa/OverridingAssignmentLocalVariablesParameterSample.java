package br.unb.cic.analysis.samples.oa;

public class OverridingAssignmentLocalVariablesParameterSample {

    public static void main(String[] args) {
        m(1);
    }
    private static void m(int y) {
        y = 3; //left
        int x = y + 1; //base
        y = 4; //right

        System.out.println(x);
    }
}
