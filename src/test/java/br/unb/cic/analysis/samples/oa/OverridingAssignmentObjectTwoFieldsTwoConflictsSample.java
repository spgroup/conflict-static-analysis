package br.unb.cic.analysis.samples.oa;

public class OverridingAssignmentObjectTwoFieldsTwoConflictsSample {
    public OverridingAssignmentInstance a;
    public OverridingAssignmentInstance b;

    public static void main(String[] args) {
        OverridingAssignmentObjectTwoFieldsTwoConflictsSample $stack = new OverridingAssignmentObjectTwoFieldsTwoConflictsSample();

        $stack.b.a = $stack.b.a + 3; // left
        $stack.b.b = 3; // right

        $stack.b.b = 4; //left
        $stack.b.a = 4; //right
    }
}