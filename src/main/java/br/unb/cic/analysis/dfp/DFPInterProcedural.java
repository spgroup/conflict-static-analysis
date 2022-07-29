package br.unb.cic.analysis.dfp;


import br.unb.cic.analysis.AbstractMergeConflictDefinition;

public class DFPInterProcedural extends DFPAnalysis {
    public DFPInterProcedural(String classPath, AbstractMergeConflictDefinition definition) {
        super(classPath, definition);
    }
    @Override
    public boolean interprocedural() {
        return true;
    }

    @Override
    public boolean propagateObjectTaint() {
        return false;
    }
}