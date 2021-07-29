package br.unc.cic.analysis.test;

public class Marking {
    String className;
    int sourceLines[];
    int sinkLines[];

    public Marking(String className, int[] sourceLines, int[] sinkLines) {
        this.className = className;
        this.sourceLines = sourceLines;
        this.sinkLines = sinkLines;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int[] getSourceLines() {
        return sourceLines;
    }

    public void setSourceLines(int[] sourceLines) {
        this.sourceLines = sourceLines;
    }

    public int[] getSinkLines() {
        return sinkLines;
    }

    public void setSinkLines(int[] sinkLines) {
        this.sinkLines = sinkLines;
    }
}