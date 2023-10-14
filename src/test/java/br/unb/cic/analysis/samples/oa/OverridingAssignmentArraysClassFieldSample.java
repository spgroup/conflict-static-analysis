package br.unb.cic.analysis.samples.oa;

public class OverridingAssignmentArraysClassFieldSample {
    public static int[] y;

    public static void main(String[] args) {
        y[0] = 3; //left
        int x = y[1] + 1; //base
        y[3] = 4; //right
    }
}
