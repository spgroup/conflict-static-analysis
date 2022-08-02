package br.unb.cic.analysis.pdg;


import br.unb.cic.analysis.AbstractMergeConflictDefinition;

public class PDGIntraProcedural extends PDGAnalysis {
    public PDGIntraProcedural(String classPath, AbstractMergeConflictDefinition definition) {
        super(classPath, definition);
    }

    @Override
    public boolean interprocedural() {
        return false;
    }

    @Override
    public boolean intraprocedural() {
        return true;
    }


    @Override
    public boolean propagateObjectTaint() {
        return false;
    }
}