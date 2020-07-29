package br.unb.cic.analysis.svfa.confluence;

import br.unb.cic.soot.graph.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfluenceConflict {
    private List<Node> sourceNodePath;
    private List<Node> sinkNodePath;

    ConfluenceConflict(List<Node> sourceNodePath, List<Node> sinkNodePath) {
        // assume that flows have at least one source and one sink
        assert sourceNodePath.size() > 1;
        assert sinkNodePath.size() > 1;
        this.sourceNodePath = sourceNodePath;
        this.sinkNodePath = sinkNodePath;
    }

    @Override
    public String toString() {
        return "SOURCE=>BASE: "
                + pathToString(sourceNodePath) + "\n" +
                "SINK=>BASE: " + pathToString(sinkNodePath);
    }

    private String pathToString(List<Node> nodePath) {
        List<String> hashSet = nodePath.stream()
                .map(node -> nodeToString(node))
                .distinct()
                .collect(Collectors.toList());

        return String.join(" => ", hashSet);
    }

    private String nodeToString(Node node) {
        return "(" +
                String.join(",", node.className(), node.method(), Integer.toString(node.line()))
                + ")";
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }
}
