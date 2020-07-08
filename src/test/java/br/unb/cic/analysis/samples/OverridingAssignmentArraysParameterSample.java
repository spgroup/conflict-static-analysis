package br.unb.cic.analysis.samples;

public class OverridingAssignmentArraysParameterSample {

    public static void m(int[] y) {
        y[0] = 3; //left
        int x = y[0] + 1; //base
        y[0] = 4; //right

        System.out.println(x);
    }
}
