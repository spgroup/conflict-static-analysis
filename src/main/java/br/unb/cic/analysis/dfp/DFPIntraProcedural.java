package br.unb.cic.analysis.dfp;


import br.unb.cic.analysis.AbstractMergeConflictDefinition;

public class DFPIntraProcedural extends DFPAnalysisSemanticConflicts {
    public DFPIntraProcedural(String classPath, AbstractMergeConflictDefinition definition) {
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

    @Override
    public final boolean isFieldSensitiveAnalysis() {
        return true;
    }
}