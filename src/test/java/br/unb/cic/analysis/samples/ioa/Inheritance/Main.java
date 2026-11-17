package br.unb.cic.analysis.samples.ioa.Inheritance;

public class Main {
    public static void main(String[] args) {
        Conta c = new Conta();
        Poupanca p = new Poupanca();

        p.creditar(5);
        c = p;
        c.creditar(3);
    }
}
