package br.unb.cic.analysis.dfp;


import br.unb.cic.analysis.AbstractMergeConflictDefinition;

public class DFPInterProcedural extends DFPAnalysisSemanticConflicts {
    public DFPInterProcedural(String classPath, AbstractMergeConflictDefinition definition) {
        super(classPath, definition);
    }

    @Override
    public boolean interprocedural() {
        return true;
    }

    @Override
    public boolean intraprocedural() {
        return false;
    }

    @Override
    public boolean propagateObjectTaint() {
        return true;
    }

    @Override
    public final boolean isFieldSensitiveAnalysis() {
        return true;
    }
}