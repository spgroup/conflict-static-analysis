package br.unb.cic.analysis.samples.oa;

public class OverridingAssignmentArraysClassFieldSample {
    public int[] y;

    public void main(String[] args) {
        OverridingAssignmentArraysClassFieldSample test = new OverridingAssignmentArraysClassFieldSample();
        test.y[0] = 3; //left
        int x = y[1] + 1; //base
        test.y[3] = 4; //right
    }
}
