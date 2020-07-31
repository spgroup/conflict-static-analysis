package br.unb.cic.analysis.svfa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;

public class SVFAIntraProcedural extends SVFAAnalysis {
    public SVFAIntraProcedural(String classPath, AbstractMergeConflictDefinition definition) {
        super(classPath, definition);
    }

    @Override
    public boolean interproceduralAnalysis() {
        return false;
    }
}