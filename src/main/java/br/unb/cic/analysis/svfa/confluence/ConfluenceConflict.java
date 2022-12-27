package br.unb.cic.analysis.svfa.confluence;

import br.unb.cic.soot.graph.StatementNode;

import java.util.List;
import java.util.stream.Collectors;

public class ConfluenceConflict {
    private List<StatementNode> sourceNodePath;
    private List<StatementNode> sinkNodePath;

    ConfluenceConflict(List<StatementNode> sourceNodePath, List<StatementNode> sinkNodePath) {
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

    private String pathToString(List<StatementNode> nodePath) {
        List<String> hashSet = nodePath.stream()
                .map(node -> nodeToString(node))
                .distinct()
                .collect(Collectors.toList());

        return String.join(" => ", hashSet);
    }

    private String nodeToString(StatementNode node) {
        return formatConflict(node.toString());
    }

    public String formatConflict(String p){
        return p.replace("), Node", ") => Node");
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
