package br.unb.cic.analysis.samples.oa;

public class OverridingAssignmentArraysSample {

    public static void main(String[] args) {
        int[] arr = {0,0,0,0,0};

        arr[4] = 10; //left
        arr[3] = 3;  //right

        System.out.println(arr[5]);
    }
}
