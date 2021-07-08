package br.unb.cic.analysis.samples.ioa;
public class CallGraphSample {
    private int[] arr;

    public void m() {
        CallGraphInterface callGraphInterface = new CallGraphClassImplements();
        callGraphInterface.baz();

        CallGraphClassImplements callGraphClassImplements = new CallGraphClassImplements();
        callGraphClassImplements.baz();

        CallGraphClass callGraphClass = new CallGraphClass();
        callGraphClass.bar();

        foo();

        int y = 1;
    }

    private void foo() {
        this.arr[1] = 1;
    }
}

class CallGraphClass {
    public int bar;

    public void bar() {
        bar = 2;
    }
}

class CallGraphClassImplements implements CallGraphInterface {
    public int baz;

    CallGraphClassImplements() {
        super();
        this.baz = 1;
    }

    @Override
    public void baz() {
        this.baz = 3;
    }
}

interface CallGraphInterface {
    void baz();
}