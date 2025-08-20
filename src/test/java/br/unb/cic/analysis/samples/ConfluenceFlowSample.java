package br.unb.cic.analysis.samples;

public class ConfluenceFlowSample {
    public void foo(){
        int x = 1;
        int y = 2;
        int z, w = 3;

        x = 10; //left

        z = x+1;

        y++;    //right

        w = x+y;
    }
}
