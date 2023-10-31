package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, main():9] --> [right, main():11]
public class ContainmentSample {

    public static void main(String[] args) {
        Point x = new Point(); //LEFT
        int base = 0;
        x.y = 5; // RIGHT
    }
}