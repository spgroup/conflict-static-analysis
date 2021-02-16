package br.unb.cic.analysis.samples;
//1 conflict
public class RecursiveDefinitionClassAttributeOneConflictTwoLevels2Sample {
    int x;
    public void m() {
        a();
        b();
    }
    public void a() {
        o();// left
    }
    public void b() {
        n();//right
    }
    public void n() {
        System.out.println(x);
    }
    public void o() {
        x=0;
    }
}
