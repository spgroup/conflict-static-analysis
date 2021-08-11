package br.unb.cic.analysis.samples.ioa;

// Conflict: [{left, CallGraphClassImplements.baz():43] --> [right, CallGraphClassImplements<init>():38},
// {left, CallGraphClassImplements<init>():38] --> [right, CallGraphClassImplements.baz():43}]

public class CallGraphSample {
    private static int[] arr;

    public static void main(String[] args) {
        CallGraphInterface callGraphInterface = new CallGraphClassImplements(); // LEFT
        callGraphInterface.baz(); // RIGHT

        CallGraphClassImplements callGraphClassImplements = new CallGraphClassImplements(); // LEFT
        callGraphClassImplements.baz(); // RIGHT

        CallGraphClass callGraphClass = new CallGraphClass(); // LEFT
        callGraphClass.bar(); // RIGHT

        foo();

        int y = 1;
    }

    private static void foo() {
        arr[1] = 1;
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