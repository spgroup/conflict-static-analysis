package br.unb.cic.analysis.samples.ioa;

public class Point {
    public Integer x;
    public Integer y;
    public Point z;

    public Point() {
        this.x = 0;
        this.y = 0;
        this.z = new Point();
    }
}
