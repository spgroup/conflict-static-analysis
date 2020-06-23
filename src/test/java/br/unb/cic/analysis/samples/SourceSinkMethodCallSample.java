package br.unb.cic.analysis.samples;

public class SourceSinkMethodCallSample {
    public void foo(){
        int x = 10;
        int y = 20;

        x = one();      //right

        y = two();      //left

        System.out.println(x+y);
    }
    private int one(){return 1;}
    private int two(){return 2;}

}
