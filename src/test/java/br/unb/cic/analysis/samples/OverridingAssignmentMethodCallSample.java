package br.unb.cic.analysis.samples;

public class OverridingAssignmentMethodCallSample {
    private int x;

    public void method(OverridingAssignmentMethodCallSample obj) {
        obj.m(3); //left
        obj.m(43);
        obj.m(7); //right
        obj.m(87); //left
    }

    public void m(int x){
        this.x = x;
    }
}
