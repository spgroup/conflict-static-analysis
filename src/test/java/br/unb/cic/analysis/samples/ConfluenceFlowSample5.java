package br.unb.cic.analysis.samples;

public class ConfluenceFlowSample5 {
    public int a;
    public void foo(){
        ConfluenceFlowSample5 inst = new ConfluenceFlowSample5();
        int x;
        int y;
        x = 1; //left
        y = 2; //right

        inst.out(x);
        inst.out(y);
    }
    public ConfluenceFlowSample5(){}

    public void out(int msg){
        System.out.println(msg);
    }

}
