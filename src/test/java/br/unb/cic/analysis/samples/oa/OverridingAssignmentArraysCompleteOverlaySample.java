package br.unb.cic.analysis.samples.oa;

public class OverridingAssignmentArraysCompleteOverlaySample {

    public static void main(String[] args) {

        int[] aux = {1, 2, 3, 4, 5}; //right
        int[] arr = {0,0,0,0,0};

        arr[4] = 10;
        arr[5] = 10; //left

        arr = aux; //right

        System.out.println(arr[5]);
    }
}
