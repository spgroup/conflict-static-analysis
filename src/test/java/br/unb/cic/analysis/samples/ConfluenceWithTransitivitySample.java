package br.unb.cic.analysis.samples;

public class ConfluenceWithTransitivitySample {
    public void foo(){
        int x = 1;
        int y = 2;
        int z = 3;

        x = 10; //left

        z = x;

        y++;    //right

        System.out.println(z+y);
    }
}
