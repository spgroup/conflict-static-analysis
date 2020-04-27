package br.unb.cic.analysis.samples;

public class ConfluenceWithTransitivitySample {
    public void foo(){
        int x = 1;
        int y = 2;
        int z = 3;

        x = 10; //left

        z = x+1;

        y++;    //right

        //addThese(z, y);
        System.out.println(z+y);
    }
    /*private int addThese(int a1, int a2){
        return a1+a2;
    }*/
}
