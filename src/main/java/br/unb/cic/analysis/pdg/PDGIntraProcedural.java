package br.unb.cic.analysis.pdg;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.soot.svfa.CG;
import br.unb.cic.soot.svfa.CHA$;
import br.unb.cic.soot.svfa.SPARK$;

public class PDGIntraProcedural extends PDGAnalysisSemanticConflicts {

    public PDGIntraProcedural(String classPath, AbstractMergeConflictDefinition definition) {
        super(classPath, definition);
    }

    @Override
    public boolean interprocedural() {
        return false;
    }
}