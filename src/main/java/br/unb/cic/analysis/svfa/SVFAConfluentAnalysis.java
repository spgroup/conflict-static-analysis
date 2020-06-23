package br.unb.cic.analysis.svfa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.Statement;
import scala.collection.JavaConverters;

import java.net.ServerSocket;
import java.util.List;
import java.util.stream.Collectors;

public class SVFAConfluentAnalysis {

    private String cp;
    private AbstractMergeConflictDefinition definition;

    SVFAConfluentAnalysis(String classPath, AbstractMergeConflictDefinition definition) {
        this.cp = classPath;
        this.definition = definition;
    }

    public void execute() {
        SVFAAnalysis sourceBaseAnalysis = sourceBaseAnalysis();
        sourceBaseAnalysis.buildSparseValueFlowGraph();
        System.out.println(sourceBaseAnalysis.svgToDotModel());

        SVFAAnalysis sinkBaseAnalysis = sinkBaseAnalysis();
        sinkBaseAnalysis.buildSparseValueFlowGraph();
        System.out.println(sinkBaseAnalysis.svgToDotModel());
    }

    private SVFAAnalysis sourceBaseAnalysis() {
        return new SVFAAnalysis(this.cp, this.definition) {

            @Override
            protected List<Statement> getSourceStatements() {
                return definition.getSourceStatements();
            }

            @Override
            protected List<Statement> getSinkStatements() {
                return definition.getInBetweenStatements();
            }
        };
    }

    private SVFAAnalysis sinkBaseAnalysis() {
        return new SVFAAnalysis(this.cp, this.definition) {
            @Override
            protected List<Statement> getSourceStatements() {
                return definition.getSinkStatements();
            }

            @Override
            protected List<Statement> getSinkStatements() {
                return definition.getInBetweenStatements();
            }
        };
    }

}
