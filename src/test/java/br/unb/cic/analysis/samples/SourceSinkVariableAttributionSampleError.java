package br.unb.cic.analysis.samples;

public class SourceSinkVariableAttributionSampleError {
    public void foo() {
        int x = 0;
        int y = 0;
        int z = 10;

        x = 10;      //left
        x++; // base
        y = z+2;    //right

        addThese(x, y); //Confluence Line

    }
    private int addThese(int a0, int a1){
        return a0 + a1;
    }
}
