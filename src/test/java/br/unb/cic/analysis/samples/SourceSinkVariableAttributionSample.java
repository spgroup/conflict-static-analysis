package br.unb.cic.analysis.samples;

/**
 * with the current analysis setup Soot,
 * when optimizing the code, ends up erasing
 * variable references that have a constant
 * attributed to them, preventing the conflict
 * detection by the analyzer if such attribution
 * happens to be a line of interest(source or sink).
 * **/

public class SourceSinkVariableAttributionSample {
    public void foo() {
        int x = 0;
        int y = 0;
        int z = 10;

        x = 10;      //left

        y = z+2;    //right

        addThese(x, y); //Confluence Line

    }
    private int addThese(int a0, int a1){
        return a0 + a1;
    }
}
