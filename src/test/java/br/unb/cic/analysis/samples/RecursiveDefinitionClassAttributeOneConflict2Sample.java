package br.unb.cic.analysis.samples;
//1 conflict
public class RecursiveDefinitionClassAttributeOneConflict2Sample {
    public int x;
    public void m() {
        x = 0; // left
        n(); //right
    }

    public void n() {
        System.out.println(x);
    }

}
