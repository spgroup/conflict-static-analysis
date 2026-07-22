package br.unb.cic.analysis.dfp;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.soot.svfa.CG;
import br.unb.cic.soot.svfa.CHA$;
import br.unb.cic.soot.svfa.SPARK$;
import scala.collection.JavaConverters;
import java.util.Arrays;
import java.util.List;

public class DFPInterProcedural extends DFPAnalysisSemanticConflicts {

    public DFPInterProcedural(String classPath, AbstractMergeConflictDefinition definition, int depthLimit, List<String> entrypoints) {
        super(classPath, definition, depthLimit, entrypoints);
    }

    public DFPInterProcedural(String classPath, AbstractMergeConflictDefinition definition, int depthLimit) {
        super(classPath, definition, depthLimit);
    }

    public DFPInterProcedural(String classPath, AbstractMergeConflictDefinition definition) {
        super(classPath, definition);
    }

    @Override
    public boolean interprocedural() {
        return true;
    }

    @Override
    public scala.collection.immutable.List<String> getIncludeList() {
        return JavaConverters.asScalaBuffer(Arrays.asList("")).toList();
    }

}