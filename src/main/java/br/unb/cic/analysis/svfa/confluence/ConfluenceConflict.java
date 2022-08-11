package br.unb.cic.analysis.svfa.confluence;

import br.ufpe.cin.soot.graph.LambdaNode;
import java.util.List;
import java.util.stream.Collectors;

public class ConfluenceConflict {
    private List<LambdaNode> sourceNodePath;
    private List<LambdaNode> sinkNodePath;

    ConfluenceConflict(List<LambdaNode> sourceNodePath, List<LambdaNode> sinkNodePath) {
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

    private String pathToString(List<LambdaNode> nodePath) {
        List<String> hashSet = nodePath.stream()
                .map(node -> nodeToString(node))
                .distinct()
                .collect(Collectors.toList());

        return String.join(" => ", hashSet);
    }

    private String nodeToString(LambdaNode node) {
        return "(" +
                node.show()
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
