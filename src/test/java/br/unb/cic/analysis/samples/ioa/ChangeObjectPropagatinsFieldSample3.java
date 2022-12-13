package br.unb.cic.analysis.samples.ioa;

// Not Conflict
public class ChangeObjectPropagatinsFieldSample3 {

    public static void main(String[] args) {
        Point c = new Point();
        Point d = new Point();
        c.y = 5; // LEFT
        int base = 0;
        c = d; // RIGHT
    }

}