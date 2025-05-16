package br.unb.cic.analysis.samples.ioa;
// Nesse caso, como p1 e p2 tem pointTo = vazio. A analise entao compara como se fosse sem PA.
// Not conflict
public class PointsToDifferentObjectFromParametersSample {

    public void m(Point p1, Point p2) {
        p1.x = new Integer(10); // LEFT
        String s = "base";
        p2.x = new Integer(20);  // RIGHT
    }
}