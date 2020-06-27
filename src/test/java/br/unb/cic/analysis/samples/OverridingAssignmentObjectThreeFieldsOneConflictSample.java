package br.unb.cic.analysis.samples;

public class OverridingAssignmentObjectThreeFieldsOneConflictSample {
    public OverridingAssignmentInstance2 a;
    public OverridingAssignmentInstance2 b;

    public static void main(String[] args) {
        OverridingAssignmentObjectThreeFieldsOneConflictSample instanceLocal = new OverridingAssignmentObjectThreeFieldsOneConflictSample();

        instanceLocal.b.a.a = instanceLocal.b.a.a + 3; // left
        instanceLocal.b.a.b = 3; // right in {instanceLocal.b.a.a, instanceLocal.b.a.b }
        instanceLocal.b.a.a = 7; // base
        instanceLocal.b.a.b = 4; //left
        instanceLocal.b.a.a = 4; //right
    }
}

/*

Se base igual a aux1 ou aux 2
    retorne false
se aux1 == aux 2
    retorne true
instanceLocal = $stack2;


        $stack3 = instanceLocal.<br.unb.cic.analysis.samples.OverridingAssignmentObjectFieldsSample: br.unb.cic.analysis.samples.OverridingAssignmentInstance b>;

        $stack8 = $stack3.<br.unb.cic.analysis.samples.OverridingAssignmentInstance: br.unb.cic.analysis.samples.OverridingAssignmentInstance2 a>;

        $stack4 = instanceLocal.<br.unb.cic.analysis.samples.OverridingAssignmentObjectFieldsSample: br.unb.cic.analysis.samples.OverridingAssignmentInstance b>;

        $stack5 = $stack4.<br.unb.cic.analysis.samples.OverridingAssignmentInstance: br.unb.cic.analysis.samples.OverridingAssignmentInstance2 a>;

        $stack6 = $stack5.<br.unb.cic.analysis.samples.OverridingAssignmentInstance2: int a>;

        $stack7 = $stack6 + 3;

        $stack8.<br.unb.cic.analysis.samples.OverridingAssignmentInstance2: int a> = $stack7;

        $stack3 = i.b;

        $stack8 = i.b.a;

        $stack4 = i.b;

        $stack5 = i.b.a;

        $stack6 = i.b.a.a;

        $stack7 = i.b.a.a + 3;

        $stack8.a = $stack7;

        $stack9 = instanceLocal.<br.unb.cic.analysis.samples.OverridingAssignmentObjectFieldsSample: br.unb.cic.analysis.samples.OverridingAssignmentInstance a>;

        $stack10 = $stack9.<br.unb.cic.analysis.samples.OverridingAssignmentInstance: br.unb.cic.analysis.samples.OverridingAssignmentInstance2 a>;

        $stack10.<br.unb.cic.analysis.samples.OverridingAssignmentInstance2: int a> = 4;

        instanceLocal.<br.unb.cic.analysis.samples.OverridingAssignmentObjectFieldsSample: br.unb.cic.analysis.samples.OverridingAssignmentInstance b>.<br.unb.cic.analysis.samples.OverridingAssignmentInstance: br.unb.cic.analysis.samples.OverridingAssignmentInstance2 a>.<br.unb.cic.analysis.samples.OverridingAssignmentInstance2: int a>
        instanceLocal.<br.unb.cic.analysis.samples.OverridingAssignmentObjectFieldsSample: br.unb.cic.analysis.samples.OverridingAssignmentInstance b>.<br.unb.cic.analysis.samples.OverridingAssignmentInstance: br.unb.cic.analysis.samples.OverridingAssignmentInstance2 a>.<br.unb.cic.analysis.samples.OverridingAssignmentInstance2: int a>

        $stack5 = instanceLocal.<br.unb.cic.analysis.samples.OverridingAssignmentObjectFieldsSample: br.unb.cic.analysis.samples.OverridingAssignmentInstance a>;

        $stack6 = $stack5.<br.unb.cic.analysis.samples.OverridingAssignmentInstance: br.unb.cic.analysis.samples.OverridingAssignmentInstance2 a>;

        $stack6.<br.unb.cic.analysis.samples.OverridingAssignmentInstance2: int a> = 4;


10 e 14
public OverridingAssignmentInstance a = new OverridingAssignmentInstance();
    public OverridingAssignmentInstance b = new OverridingAssignmentInstance();

    public static void main(String[] args) {
        OverridingAssignmentSample2 instanceLocal = new OverridingAssignmentSample2();

        instanceLocal.b.b = 3; // left



        instanceLocal.a.a = 4; //right

    }
x = $stack2;

y = x.a;

y.b = 3;

{y, x.a, y.b}  {<x.a, y.b>, (y, x.a)}
               {<x.a, z.a>, (z, x.a)}
               {<useBox (field), defBox (field)>, (defBox, useBox)}
               {<useBox (field), defBox (field)>, (defBox, useBox)}

- Criar estrutura de abstração
    * hash map ou pair?

    {(x.a, .b), (x.a, .b)}

z = x.a;

z.a = 4;

{z, x.a, z.a}

 */