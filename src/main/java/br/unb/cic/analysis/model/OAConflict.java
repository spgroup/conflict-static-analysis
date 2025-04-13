package br.unb.cic.analysis.model;

import java.util.stream.Collectors;

public class OAConflict extends Conflict {

    protected Boolean interprocedural;

    public OAConflict(Statement source, Statement sink, Boolean interprocedural) {
        super(source, sink);
        this.interprocedural = interprocedural;
    }

    @Override
    public String toJSON() {
        return this.formatJSON("OA" + (interprocedural ? "INTER" : "INTRA"), "OA conflict");
    }

    @Override
    protected String formatJSON(String type, String label) {
        return String.format(
                "{" + "\n" +
                    "\t" + "\"type\": \"%s\"," + "\n" +
                    "\t" + "\"label\": \"%s\"," + "\n" +
                    "\t" + "\"body\": {" + "\n" +
                        "\t\t" + "\"description\": \"%s - %s\"," + "\n" +
                        "\t\t" + "\"interference\": [" + "\n" +
                            "\t\t\t" + "{" + "\n" +
                                "\t\t\t\t" + "\"type\": \"declaration\"," + "\n" +
                                "\t\t\t\t" + "\"branch\": \"L\"," + "\n" +
                                "\t\t\t\t" + "\"text\": \"%s\"," + "\n" +
                                "\t\t\t\t" + "\"location\": {" + "\n" +
                                    "\t\t\t\t\t" + "\"file\": \"\"," + "\n" +
                                    "\t\t\t\t\t" + "\"class\": \"%s\"," + "\n" +
                                    "\t\t\t\t\t" + "\"method\": \"%s\"," + "\n" +
                                    "\t\t\t\t\t" + "\"line\": %d" + "\n" +
                                "\t\t\t\t" + "}," + "\n" +
                                "\t\t\t\t" + "\"stackTrace\": [" + sourceTraversedLine.stream().map(TraversedLine::toJSON).collect(Collectors.joining(",")) + "]" + "\n" +
                            "\t\t\t" + "}," + "\n" +
                            "\t\t\t" + "{" + "\n" +
                                "\t\t\t\t" + "\"type\": \"override\"," + "\n" +
                                "\t\t\t\t" + "\"branch\": \"R\"," + "\n" +
                                "\t\t\t\t" + "\"text\": \"%s\"," + "\n" +
                                "\t\t\t\t" + "\"location\": {" + "\n" +
                                    "\t\t\t\t\t" + "\"file\": \"\"," + "\n" +
                                    "\t\t\t\t\t" + "\"class\": \"%s\"," + "\n" +
                                    "\t\t\t\t\t" + "\"method\": \"%s\"," + "\n" +
                                    "\t\t\t\t\t" + "\"line\": %d" + "\n" +
                                "\t\t\t\t" + "}," + "\n" +
                                "\t\t\t\t" + "\"stackTrace\": [" + sinkTraversedLine.stream().map(TraversedLine::toJSON).collect(Collectors.joining(",")) + "]" + "\n" +
                            "\t\t\t" + "}" + "\n" +
                        "\t\t" + "]" + "\n" +
                    "\t" + "}" + "\n" +
                "}",
                type, label, sourceUnit.getDefBoxes().get(0).getValue(), sinkUnit.getDefBoxes().get(0).getValue(),
                sourceUnit.toString().replaceAll("\"", "'"), sourceClassName, sourceMethodName, sourceLineNumber,
                sinkUnit.toString().replaceAll("\"", "'"), sinkClassName, sinkMethodName, sinkLineNumber
        );
    }
}
