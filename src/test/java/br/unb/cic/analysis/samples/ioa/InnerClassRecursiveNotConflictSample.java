package br.unb.cic.analysis.samples.ioa;

public class InnerClassRecursiveNotConflictSample {

    public static void main(String[] args) {
        AbstractClass.InnerClass1 abstractClass;
        if (args[0] == "A") {
            abstractClass = new AbstractClass.InnerClass1();
        } else if (args[0] == "B") {
            abstractClass = new AbstractClass.InnerClass2();
        } else if (args[0] == "C") {
            abstractClass = new AbstractClass.InnerClass3(new AbstractClass.InnerClass1());
        } else if (args[0] == "D") {
            abstractClass = new AbstractClass.InnerClass4();
        } else if (args[0] == "E") {
            abstractClass = new AbstractClass.InnerClass5();
        } else if (args[0] == "F") {
            abstractClass = new AbstractClass.InnerClass6();
        } else if (args[0] == "G") {
            abstractClass = new AbstractClass.InnerClass7();
        } else {
            abstractClass = new AbstractClass.InnerClass8();
        }
        abstractClass.match();
    }
}

