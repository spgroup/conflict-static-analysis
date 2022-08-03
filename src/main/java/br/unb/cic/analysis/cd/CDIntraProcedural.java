package br.unb.cic.analysis.cd;


import br.unb.cic.analysis.AbstractMergeConflictDefinition;

public class CDIntraProcedural extends CDAnalysis {
    public CDIntraProcedural(String classPath, AbstractMergeConflictDefinition definition) {
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
    public final boolean isFieldSensitiveAnalysis() {
        return true;
    }
}