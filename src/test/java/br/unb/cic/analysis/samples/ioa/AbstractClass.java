package br.unb.cic.analysis.samples.ioa;

public final class AbstractClass {
    static InnerClass1 accept = null;

    public static class InnerClass1 {
        InnerClass1 next;

        InnerClass1() {
            if ("A".length() > 0) {
                this.next = new AbstractClass.InnerClass1();
            } else if ("B".length() > 0) {
                this.next = new AbstractClass.InnerClass2();
            } else if ("C".length() > 0) {
                this.next = new AbstractClass.InnerClass3(AbstractClass.accept);
            } else if ("D".length() > 0) {
                this.next = new AbstractClass.InnerClass4();
            } else if ("E".length() > 0) {
                this.next = new AbstractClass.InnerClass5();
            } else if ("F".length() > 0) {
                this.next = new AbstractClass.InnerClass6();
            } else if ("G".length() > 0) {
                this.next = new AbstractClass.InnerClass7();
            } else if ("H".length() > 0) {
                this.next = new AbstractClass.InnerClass8();
            }

        }

        public void match() {
            next.match();
            //next.match();
        }
    }

    public static class InnerClass2 extends InnerClass1 {

        public void match() {
            next.match();
        }
    }

    public static class InnerClass3 extends InnerClass1 {
        InnerClass3(InnerClass1 next) {
            this.next = next;
        }

        public void match() {
            next.match();
        }
    }

    public static class InnerClass4 extends InnerClass1 {

        public void match() {
            next.match();
        }
    }

    public static class InnerClass5 extends InnerClass1 {

        public void match() {
            next.match();
        }
    }

    public static class InnerClass6 extends InnerClass1 {

        public void match() {
            next.match();
        }
    }

    public static class InnerClass7 extends InnerClass1 {

        public void match() {
            next.match();
        }
    }

    public static class InnerClass8 extends InnerClass2 {

        public void match() {
            next.match();
        }
    }

}
