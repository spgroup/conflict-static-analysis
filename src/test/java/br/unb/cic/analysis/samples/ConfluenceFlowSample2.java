package br.unb.cic.analysis.samples;

public class ConfluenceFlowSample2 {
    public void foo(){
        int x;
        int y;
        int z, w, a;
        w = 2;//left
        x = 1; //left
        y = 2; //right

        z = x+y+w; // a = x+y; z = a+w;

        System.out.println(z);
    }
}

