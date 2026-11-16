package br.unb.cic.analysis.samples;

public class ConfluenceFlowSample3 {
    public void foo(){
        int x;
        int y, a;
        int z, w;
        a = 3;
        x = 1; //left
        y = 2; //right

        w = x + y;
        z = x+y; //z = x+a;

//        System.out.println(z);
    }
}

//Left1 -> Base 1
//Left1 -> Base2
//Right1 -> Base2..

