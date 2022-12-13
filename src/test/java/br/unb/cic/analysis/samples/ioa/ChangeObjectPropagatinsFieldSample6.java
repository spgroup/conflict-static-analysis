package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, main():9] --> [right, main():11]
public class ChangeObjectPropagatinsFieldSample6 {

    public static void main(String[] args) {
        Point c = new Point();
        Point d = new Point();
        Point x = c.z;
        x.z = d; // LEFT
        Point b = c.z;
        Point a = b.z;
        a.y = new Integer(5); // RIGHT
    }
}