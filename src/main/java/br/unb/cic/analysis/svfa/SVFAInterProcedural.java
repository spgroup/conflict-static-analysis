package br.unb.cic.analysis.svfa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;

public class SVFAInterProcedural extends SVFAAnalysis {
    public SVFAInterProcedural(String classPath, AbstractMergeConflictDefinition definition) {
        super(classPath, definition);
    }
    @Override
    public boolean interprocedural() {
        return true;
    }

    @Override
    public boolean propagateObjectTaint() {
        return true;
    }
}