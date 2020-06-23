package br.unb.cic.analysis.samples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayDataFlowSample {

    public static void main(String[] args) {
        int[] arr = {0,0,0,0,0};

        arr = populate(arr.length); //source 1

        arr[4] = 10;        //source 2

        int b = arr[4];     //sink 2

        for(int a : arr)
            System.out.println(a); //sink 1

    }
    private static int[] populate(int size) {
        int[] res = new int[size];

        for(int i = 0; i < size; i++)
            res[i] = i+1;

        return res;
    }

}
