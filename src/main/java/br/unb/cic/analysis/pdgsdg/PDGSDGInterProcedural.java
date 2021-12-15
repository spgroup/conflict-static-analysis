package br.unb.cic.analysis.pdgsdg;


import br.unb.cic.analysis.AbstractMergeConflictDefinition;

public class PDGSDGInterProcedural extends PDGSDGAnalysis {
    public PDGSDGInterProcedural(String classPath, AbstractMergeConflictDefinition definition) {
        super(classPath, definition);
    }
    @Override
    public boolean interprocedural() {
        return true;
    }
}