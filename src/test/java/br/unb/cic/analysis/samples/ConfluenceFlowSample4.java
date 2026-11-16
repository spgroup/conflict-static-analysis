package br.unb.cic.analysis.samples;

public class ConfluenceFlowSample4 {
    Example a = new Example();
    Example b = new Example();

    public void foo(){
        a.setX(1); //a.x = a.x + 1; //a.x = a.x + 1; //left

        System.out.println("divider");

        b.setX(2); //a.x = a.x + 1; //b.x = b.x + 1;  //right

        int z = a.x + b.x;

        System.out.println(z);
    }

}
