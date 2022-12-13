package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, main():9] --> [right, main():11]
public class ChangeObjectPropagatinsFieldSample5 {

    public static void main(String[] args) {
        Point c = new Point();
        Point d = new Point();
        c.z.z = d; // LEFT
        int base = 0;
        c.z.z.y = 5; // RIGHT
    }
}