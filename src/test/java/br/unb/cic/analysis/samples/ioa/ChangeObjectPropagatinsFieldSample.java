package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, main():9] --> [right, main():11]
public class ChangeObjectPropagatinsFieldSample {

    public static void main(String[] args) {
        Point c = new Point();
        Point d = new Point();
        c = d; // LEFT
        int base = 0;
        c.y = 5; // RIGHT
    }
}