package br.unb.cic.analysis.samples.ioa.Inheritance;

class Conta {
    protected double saldo = 0;

    public void creditar(double valor) {
        saldo += valor;
    }
}
