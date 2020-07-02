package br.unb.cic.analysis.svfa.confluence;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.analysis.svfa.SVFAAnalysis;
import br.unb.cic.analysis.svfa.confluence.ConfluenceConflict;
import br.unb.cic.soot.graph.Node;

import java.util.*;

public class SVFAConfluenceAnalysis {

    private String cp;
    private AbstractMergeConflictDefinition definition;
    private Set<ConfluenceConflict> confluentFlows = new HashSet<>();

    SVFAConfluenceAnalysis(String classPath, AbstractMergeConflictDefinition definition) {
        this.cp = classPath;
        this.definition = definition;
    }

    /**
     * After the execute method has been called, it returns the confluent conflicts returned by the algorithm
     * @return a set of confluence conflicts
     */
    public Set<ConfluenceConflict> getConfluentConflicts() {
        return confluentFlows;
    }

    /**
     * Executes both source -> base and sink -> base SVFA analysis intersects then populating
     * the confluentFlows attribute with the results
     */
    public void execute() {
        SVFAAnalysis sourceBaseAnalysis = sourceBaseAnalysis();
        sourceBaseAnalysis.buildSparseValueFlowGraph();
        System.out.println(sourceBaseAnalysis.svgToDotModel());
        Set<List<Node>> sourceBasePaths = sourceBaseAnalysis.findSourceSinkPaths();

        SVFAAnalysis sinkBaseAnalysis = sinkBaseAnalysis();
        sinkBaseAnalysis.buildSparseValueFlowGraph();
        System.out.println(sinkBaseAnalysis.svgToDotModel());
        Set<List<Node>> sinkBasePaths = sinkBaseAnalysis.findSourceSinkPaths();

        confluentFlows = intersectPathsByLastNode(sourceBasePaths, sinkBasePaths);
    }

    /**
     * Intersects the list of paths looking for paths that have the same last nodes
     * also ignores redundant node (that represent different jimple lines but the same Java line)
     * @param paths1 A set of lists of nodes with at least 2 nodes
     * @param paths1 A set of lists of nodes with at least 2 nodes
     * @return A set of confluence conflicts
     */
    private Set<ConfluenceConflict> intersectPathsByLastNode(Set<List<Node>> paths1, Set<List<Node>> paths2) {
        Map<Node, List<Node>> pathEndHash = new HashMap<>();

        for (List<Node> path: paths1) {
            pathEndHash.put(getLastNode(path), path);
        }

        Set<ConfluenceConflict> result = new HashSet<>();
        for (List<Node> path : paths2) {
            Node lastNode = getLastNode(path);
            if (pathEndHash.containsKey(lastNode)) {
                result.add(new ConfluenceConflict(pathEndHash.get(lastNode), path));
            }
        }

        return result;
    }

    /**
     * @param path A list of nodes with at least 2 nodes
     * @return The last node of the list
     */
    private Node getLastNode(List<Node> path) {
        assert path.size() > 1; // assume that all paths have at least one source and one sink
        return path.get(path.size() - 1);
    }

    /**
     * @return A instance of a child class of the SVFAAnalysis class that redefine source and sink as source and base
     */
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

    /**
     * @return A instance of a child class of the SVFAAnalysis class that redefine source and sink as source and base
     */
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
