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

    public List<StatementNode> getSourceNodePath() {
        return sourceNodePath;
    }

    public void setSourceNodePath(List<StatementNode> sourceNodePath) {
        this.sourceNodePath = sourceNodePath;
    }

    public List<StatementNode> getSinkNodePath() {
        return sinkNodePath;
    }

    public void setSinkNodePath(List<StatementNode> sinkNodePath) {
        this.sinkNodePath = sinkNodePath;
    }

    @Override
    public String toString() {
        return "SOURCE=>BASE: "
                + pathToString(sourceNodePath) + "\n" +
                "SINK=>BASE: " + pathToString(sinkNodePath);
    }

    public String toJSON() {
        return this.formatJSON("CONFLUENCE", "CF conflict");
    }

    protected String formatJSON(String type, String label) {
        StatementNode sourceNode = sourceNodePath.get(0);
        StatementNode sinkNode = sinkNodePath.get(0);
        StatementNode baseNode = sourceNodePath.get(sourceNodePath.size() - 1);
        return String.format(
                "{" + "\n" +
                        "\t" + "\"type\": \"%s\"," + "\n" +
                        "\t" + "\"label\": \"%s\"," + "\n" +
                        "\t" + "\"body\": {" + "\n" +
                        "\t\t" + "\"description\": \"%s\"," + "\n" +
                        "\t\t" + "\"interference\": [" + "\n" +
                        "\t\t\t" + "{" + "\n" +
                        "\t\t\t\t" + "\"type\": \"source1\"," + "\n" +
                        "\t\t\t\t" + "\"branch\": \"L\"," + "\n" +
                        "\t\t\t\t" + "\"text\": \"%s\"," + "\n" +
                        "\t\t\t\t" + "\"location\": {" + "\n" +
                        "\t\t\t\t\t" + "\"file\": \"\"," + "\n" +
                        "\t\t\t\t\t" + "\"class\": \"%s\"," + "\n" +
                        "\t\t\t\t\t" + "\"method\": \"%s\"," + "\n" +
                        "\t\t\t\t\t" + "\"line\": %d" + "\n" +
                        "\t\t\t\t" + "}," + "\n" +
                        "\t\t\t\t" + "\"stackTrace\": [" + sinkNodePath.stream().map(this::nodeToJSON).collect(Collectors.joining(",")) + "]" + "\n" +
                        "\t\t\t" + "}," + "\n" +
                        "\t\t\t" + "{" + "\n" +
                        "\t\t\t\t" + "\"type\": \"source2\"," + "\n" +
                        "\t\t\t\t" + "\"branch\": \"R\"," + "\n" +
                        "\t\t\t\t" + "\"text\": \"%s\"," + "\n" +
                        "\t\t\t\t" + "\"location\": {" + "\n" +
                        "\t\t\t\t\t" + "\"file\": \"\"," + "\n" +
                        "\t\t\t\t\t" + "\"class\": \"%s\"," + "\n" +
                        "\t\t\t\t\t" + "\"method\": \"%s\"," + "\n" +
                        "\t\t\t\t\t" + "\"line\": %d" + "\n" +
                        "\t\t\t\t" + "}," + "\n" +
                        "\t\t\t\t" + "\"stackTrace\": [" + sourceNodePath.stream().map(this::nodeToJSON).collect(Collectors.joining(",")) + "]" + "\n" +
                        "\t\t\t" + "}," + "\n" +
                        "\t\t\t" + "{" + "\n" +
                        "\t\t\t\t" + "\"type\": \"confluence\"," + "\n" +
                        "\t\t\t\t" + "\"branch\": \"B\"," + "\n" +
                        "\t\t\t\t" + "\"text\": \"%s\"," + "\n" +
                        "\t\t\t\t" + "\"location\": {" + "\n" +
                        "\t\t\t\t\t" + "\"file\": \"\"," + "\n" +
                        "\t\t\t\t\t" + "\"class\": \"%s\"," + "\n" +
                        "\t\t\t\t\t" + "\"method\": \"%s\"," + "\n" +
                        "\t\t\t\t\t" + "\"line\": %d" + "\n" +
                        "\t\t\t\t" + "}" + "\n" +
//                        "\t\t\t\t" + "\"stackTrace\": []" + "\n" +
                        "\t\t\t" + "}" + "\n" +
                        "\t\t" + "]" + "\n" +
                        "\t" + "}" + "\n" +
                        "}",
                type, label, this.toString().replaceAll("\n", " "),
                sinkNode.value().sootUnit().toString().replaceAll("\"", "'"), sinkNode.value().className(), sinkNode.value().method(), sinkNode.value().line(),
                sourceNode.value().sootUnit().toString().replaceAll("\"", "'"), sourceNode.value().className(), sourceNode.value().method(), sourceNode.value().line(),
                baseNode.value().sootUnit().toString().replaceAll("\"", "'"), baseNode.value().className(), baseNode.value().method(), baseNode.value().line()
        );
    }

    private String nodeToJSON(StatementNode node) {
        return String.format(
                "{" + "\n" +
                        "\t" + "\"class\": \"%s\"," + "\n" +
                        "\t" + "\"method\": \"%s\"," + "\n" +
                        "\t" + "\"line\": %d" + "\n" +
                        "}",
                node.value().className(), node.value().method(), node.value().line()
        );
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
