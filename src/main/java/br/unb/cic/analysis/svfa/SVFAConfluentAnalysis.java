package br.unb.cic.analysis.svfa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Pair;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.soot.graph.Node;

import java.util.*;

public class SVFAConfluentAnalysis {

    private String cp;
    private AbstractMergeConflictDefinition definition;
    private Set<ConfluenceConflict> confluentFlows;

    SVFAConfluentAnalysis(String classPath, AbstractMergeConflictDefinition definition) {
        this.cp = classPath;
        this.definition = definition;
    }

    public  Set<ConfluenceConflict> getConfluentConflicts() {
        return confluentFlows;
    }

    public void execute() {
        SVFAAnalysis sourceBaseAnalysis = sourceBaseAnalysis();
        sourceBaseAnalysis.buildSparseValueFlowGraph();
        System.out.println(sourceBaseAnalysis.svgToDotModel());
        Set<List<Node>> sourceBasePaths = sourceBaseAnalysis.findSourceSinkPaths();

        SVFAAnalysis sinkBaseAnalysis = sinkBaseAnalysis();
        sinkBaseAnalysis.buildSparseValueFlowGraph();
        System.out.println(sinkBaseAnalysis.svgToDotModel());
        Set<List<Node>> sinkBasePaths = sourceBaseAnalysis.findSourceSinkPaths();

        confluentFlows = getConfluenceConflicts(sourceBasePaths, sinkBasePaths);
    }

    private Set<ConfluenceConflict> getConfluenceConflicts(Set<List<Node>> paths1, Set<List<Node>> paths2) {
        Map<Node, List<Node>> pathEndHash = new HashMap<>();

        for (List<Node> path: paths1) {
            pathEndHash.put(getLastNode(path), path.subList(0, path.size() - 1));
        }

        Set<ConfluenceConflict> result = new HashSet<>();
        for (List<Node> path : paths2) {
            Node lastNode = getLastNode(path);
            if (pathEndHash.containsKey(lastNode)) {
                ConfluenceConflict conflict = new ConfluenceConflict(pathEndHash.get(lastNode), path.subList(0, path.size() - 1), lastNode);
                result.add(conflict);
            }
        }

        return result;
    }

    private Node getLastNode(List<Node> path) {
        assert path.size() > 1; // assume that all paths have at least one source and one sink
        return path.get(path.size() - 1);
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
