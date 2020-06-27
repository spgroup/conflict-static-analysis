package br.unb.cic.analysis.samples;

public class OverridingAssignmentLocalVariablesSample {

    public static void main(String[] args) {
        int x=0, y=0, w=1, z=3;

        x = 3; // left
        y = 3; // right
        x = x+1; //x = 3;// base
        y = 3; // left
        x = 21; // right

        System.out.println(x);
    }
}
/*
Algoritmo Geral:

Se for uma linha de sink ou source, adiciona em in
Senão, verifica se base alterou alguma variável que está em in
    Se algum elemento da expressão atribuída estiver em in, atualiza-o em in
    Senão, remove-o de in

detectConflicts()
    Se for uma linha de sink ou source

    Separa os elementos de in em duas listas: sinks e sources

    Se for a linha atual for de sink, verifica se este elemento está na lista source
    Se for a linha atual for de source, verifica se este elmeento está na lista sink

        Implementar outro killSet()

        Não gera conflito pois useBox é igual nos dois
        x = 3; // source
        x = 6; // base
        x = 3; // sink

        x = 3; //left guarda em in
        x = 1; // se def (x) estiver em in e não estiver em useBox, zera in
        x = 31; //right compara com in

        x = 3; //left guarda em in
        x = x+1; //se def(x) estiver em in, não faz nada
        x = 31; //right compara com in

        x = 3; //left guarda em in
        y = 1; //se def(y) não estiver em in, não faz nada
        x = 31; //right compara com in

Casos:
1. (OK) //x=3; //left x=31; //base x=21; //right // não daria conflito - ao passar a base para mustKill, o x é eliminado de in
2. (OK) //x=3; //left x=1; //base y=x; //right // não daria conflito - ao passar a base para mustKill, o x é eliminado de in e ao chegar em sink o y é diferente de in
3. (OK) //x=3; //left y=21; //base y=x; //right // não daria conflito - insere x em in, mas em sink y é diferente de x
4. (OK) //x=3; //left y=21; //base x=21; //right // daria conflito - insexe o x em in, y não está em in, não o insere, em sink x é igual ao in
5. (OK) //x=3; //left x=x+1; //base x=21; //right // daria conflito - insere o x em in, se UseBox de base tiver alterações de Source, então insere em in. Seriam 2 ou 1 erros?
6. (OK) //x=3; //left y=x+1; //base x=21; //right // daria conflito - insere o x em in, se UseBox de base tiver alterações de Source, insere-o, ao comparar o x de sink com in, encontra
 */

