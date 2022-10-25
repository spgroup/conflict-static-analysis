package br.unb.cic.analysis.cd;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;

public class CDIntraProcedural extends CDAnalysisSemanticConflicts{
    public CDIntraProcedural(String classPath, AbstractMergeConflictDefinition definition) {
        super(classPath, definition);
    }

    @Override
    public boolean interprocedural() {
        return false;
    }

}