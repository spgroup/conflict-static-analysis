package br.unb.cic.analysis.samples.ioa.Inheritance;

class Poupanca extends Conta {
    @Override
    public void creditar(double valor) {
        // For example, savings account gives 10% bonus
        saldo += valor * 1.1;
    }
}
