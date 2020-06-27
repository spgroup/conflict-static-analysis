package br.unb.cic.analysis.samples;

public class OverridingAssignmentObjectFieldsSample {
    public OverridingAssignmentInstance a;
    public OverridingAssignmentInstance b;

    public static void main(String[] args) {
        OverridingAssignmentObjectFieldsSample instanceLocal = new OverridingAssignmentObjectFieldsSample();

        instanceLocal.a.a = 3; // left

//        instanceLocal.a =  3;

        instanceLocal.a.b = 4; //right

    }

}

/*
10 e 14
public OverridingAssignmentInstance a = new OverridingAssignmentInstance();
    public OverridingAssignmentInstance b = new OverridingAssignmentInstance();

    public static void main(String[] args) {
        OverridingAssignmentSample2 instanceLocal = new OverridingAssignmentSample2();

        instanceLocal.a.a = 3; // left

        instanceLocal.a.b =  3;

        instanceLocal.a.a = 4; //right

    }
instanceLocal = $stack2;

$stack3 = instanceLocal.<br.unb.cic.analysis.samples.OverridingAssignmentSample2: br.unb.cic.analysis.samples.OverridingAssignmentInstance a>;

$stack3.<br.unb.cic.analysis.samples.OverridingAssignmentInstance: int a> = 3;

$stack4 = instanceLocal.<br.unb.cic.analysis.samples.OverridingAssignmentSample2: br.unb.cic.analysis.samples.OverridingAssignmentInstance a>;

$stack4.<br.unb.cic.analysis.samples.OverridingAssignmentInstance: int b> = 3;

$stack5 = instanceLocal.<br.unb.cic.analysis.samples.OverridingAssignmentSample2: br.unb.cic.analysis.samples.OverridingAssignmentInstance a>;

$stack5.<br.unb.cic.analysis.samples.OverridingAssignmentInstance: int a> = 4;

 */