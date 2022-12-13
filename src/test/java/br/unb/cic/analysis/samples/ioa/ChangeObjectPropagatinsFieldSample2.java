package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, main():10] --> [right, main():11]
public class ChangeObjectPropagatinsFieldSample2 {

    public static void main(String[] args) {
        Point c = new Point();
        Point d = new Point();
        c = d.z; // LEFT
        int base = 0;
        c.y = 5; // RIGHT
    }

}