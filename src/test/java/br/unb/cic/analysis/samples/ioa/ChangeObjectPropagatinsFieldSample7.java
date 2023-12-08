package br.unb.cic.analysis.samples.ioa;

// NOT Conflict
public class ChangeObjectPropagatinsFieldSample7 {

    public static void main(String[] args) {
        Point c = new Point();

        c.z.x = 6; // LEFT
        int base = 0;
        c.z.y = 5; // RIGHT
    }
}