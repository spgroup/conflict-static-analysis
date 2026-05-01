package br.unb.cic.analysis.samples.MultiCG;

/**
 * Resolucao desta chamada virtual por algoritmo de Call Graph:
 * <p>
 * - CHA (Class Hierarchy Analysis): Resolve para A.m(), B.m(), C.m(), D.m()
 * Pois avalia apenas o tipo declarado 'A' e inclui todas as suas subclasses.
 * <p>
 * - RTA (Rapid Type Analysis): Resolve para A.m(), B.m(), C.m()
 * Filtra o CHA removendo classes que nunca recebem 'new' (a classe D e
 * removida).
 * <p>
 * - VTA (Variable Type Analysis): Resolve para A.m(), B.m()
 * Agrupa todos os acessos ao 'fieldA' em um unico no no grafo de propagacao.
 * Como 'a' e 'b' foram atribuidos a 'fieldA', 'result' adquire os tipos {A, B}.
 * <p>
 * - SPARK (Points-to Analysis): Resolve apenas para A.m()
 * Rastreia sites de alocacao exatos. Ele sabe que o 'fieldA' do 'container1'
 * aponta exclusivamente para a instancia 'a' (tipo A), isolando-o de
 * 'container2'.
 */
public class Main {
    public static void main(String[] args) {
        A container1 = new A();
        A container2 = new A();

        A a = new A();
        B b = new B();
        C c = new C();

        container1.fieldA = a;
        container2.fieldA = b;

        A result = container1.fieldA;

        result.m(a);

        a.n();
    }
}
