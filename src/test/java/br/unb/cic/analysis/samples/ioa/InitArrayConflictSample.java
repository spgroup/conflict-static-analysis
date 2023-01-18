package br.unb.cic.analysis.samples.ioa;

import java.util.Arrays;
import java.util.HashSet;

public class InitArrayConflictSample {
    public static void main(){
        HashSet hash = new HashSet<>(Arrays.asList(
                1,
                2,  //LEFT
                3,
                4,  //RIGHT
                5
        ));
    }

}
