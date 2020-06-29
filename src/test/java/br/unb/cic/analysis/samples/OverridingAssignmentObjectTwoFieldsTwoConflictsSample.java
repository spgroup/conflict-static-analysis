package br.unb.cic.analysis.samples;

public class OverridingAssignmentObjectTwoFieldsTwoConflictsSample {
    public OverridingAssignmentInstance a;
    public OverridingAssignmentInstance b;

    public static void main(String[] args) {
        OverridingAssignmentObjectTwoFieldsTwoConflictsSample $stack = new OverridingAssignmentObjectTwoFieldsTwoConflictsSample();

        $stack.b.a = $stack.b.a + 3; // left
        $stack.b.b = 3; // right
//        instanceLocal.b.b = instanceLocal.b.a+3; // base
        $stack.b.b = 4; //left
        $stack.b.a = 4; //right
    }
}