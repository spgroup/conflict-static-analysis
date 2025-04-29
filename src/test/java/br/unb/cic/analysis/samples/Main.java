package br.unb.cic.analysis.samples;

public class Main {
    static Example a = new Example();
    static Example b = new Example();
    public static void main(String[] args) {

        AddOne.PlusOne(a); //left
        System.out.println("divider");

        PlusOne.AddOne(b); //right
        int z = a.x + b.x;
        System.out.println(z);
    }
}