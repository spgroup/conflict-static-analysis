package br.unb.cic.analysis.samples.oa;

public class OverridingAssignmentArraysSample2 {

    public static void main(String[] args) {
        int[] arr = {0,0,0,0,0};
        int[] aux = {0,0,0,0,0};

        arr[0] = 3; // left
        aux[3] = 3; // right
        arr[1] = aux[2] + 1; // base
        aux[3]= 3; // left
        arr[0] = 21; // right

        System.out.println(arr);
    }
}
