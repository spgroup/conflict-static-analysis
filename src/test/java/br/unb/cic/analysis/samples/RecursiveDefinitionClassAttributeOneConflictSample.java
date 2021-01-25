package br.unb.cic.analysis.samples;
//1 conflict
public class RecursiveDefinitionClassAttributeOneConflictSample {
    private int x;
    public void m() {
        o(); // left
        System.out.println(x); //right
    }
    public void o() {
        x=10;
    }

}
