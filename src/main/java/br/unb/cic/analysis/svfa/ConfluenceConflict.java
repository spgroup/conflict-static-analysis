package br.unb.cic.analysis.svfa;

import br.unb.cic.soot.graph.Node;

import java.util.List;

public class ConfluenceConflict {
    private List<Node> sourceNodePath;
    private List<Node> sinkNodePath;
    private Node targetNode;

    ConfluenceConflict(List<Node> sourceNodePath, List<Node> sinkNodePath, Node targetNode) {
        this.sourceNodePath = sourceNodePath;
        this.sinkNodePath = sinkNodePath;
        this.targetNode = targetNode;
    }
}
