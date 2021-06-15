package br.unb.cic.analysis.samples.ioa;

public class CallGraphSample {

    public void m() {
        CallGraphInterface callGraphInterface = new CallGraphClassImplements();
        callGraphInterface.baz();
    }

}

class CallGraphClassImplements implements CallGraphInterface {
    public int baz;

    @Override
    public void baz() {
        this.baz = 3;
    }
}

interface CallGraphInterface {
    void baz();
}