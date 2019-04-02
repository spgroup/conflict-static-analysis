package br.unb.cic.analysis;

import br.unb.cic.analysis.model.Statement;

/**
 * A simple change in the source code, in terms of
 * a class and the source code line number.
 */
public class ClassChangeDefinition {
    private String className;
    private Statement.Type type;
    private Integer lineNumber;
    public ClassChangeDefinition(String className, Statement.Type type, Integer lineNumber) {
        this.className = className;
        this.lineNumber = lineNumber;
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Statement.Type getType() {
        return type;
    }

    public void setType(Statement.Type type) {
        this.type = type;
    }
}
