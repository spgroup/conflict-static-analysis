package br.unb.cic.analysis.ioa;

import br.unb.cic.analysis.model.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Information wee keep while traversing
 * the control flow.
 */
public class OverrideAssignmentAbstraction implements Cloneable {

    List<Statement> leftAbstraction;
    List<Statement> rightAbstraction;

    public OverrideAssignmentAbstraction() {
        this.leftAbstraction = new ArrayList<Statement>();
        this.rightAbstraction = new ArrayList<Statement>();
    }

    public OverrideAssignmentAbstraction(List<Statement> leftAbstraction, List<Statement> rightAbstraction) {
        this.leftAbstraction = leftAbstraction;
        this.rightAbstraction = rightAbstraction;
    }

    public void add(Statement stmt) {
        List<Statement> abstraction = getListForType(stmt.getType());
        if (abstraction != null) {
            abstraction.add(stmt);
        }
    }

    public void remove(Statement stmt) {
        List<Statement> abstraction = getListForType(stmt.getType());
        if (abstraction != null) {
            abstraction.remove(stmt);
        } else {
            getLeftAbstraction().remove(stmt);
            getRightAbstraction().remove(stmt);
        }

    }


    public List<List<Statement>> getLists() {
        List<List<Statement>> lists = new ArrayList<>();
        lists.add(getLeftAbstraction());
        lists.add(getRightAbstraction());
        return lists;
    }

    public List<Statement> getListForType(Statement.Type type) {
        if (type.equals(Statement.Type.SOURCE)) {
            return leftAbstraction;
        } else if (type.equals(Statement.Type.SINK)) {
            return rightAbstraction;
        }
        return null;
    }

    public List<Statement> getLeftAbstraction() {
        return leftAbstraction;
    }

    public void setLeftAbstraction(List<Statement> leftAbstraction) {
        this.leftAbstraction = leftAbstraction;
    }

    public List<Statement> getRightAbstraction() {
        return rightAbstraction;
    }

    public void setRightAbstraction(List<Statement> rightAbstraction) {
        this.rightAbstraction = rightAbstraction;
    }

    public void union(OverrideAssignmentAbstraction overrideAssignmentAbstraction) {
        // Obtém as listas da instância que está sendo passada como argumento
        List<Statement> otherList1 = overrideAssignmentAbstraction.getLeftAbstraction();
        List<Statement> otherList2 = overrideAssignmentAbstraction.getRightAbstraction();

        // Percorre a lista1 da outra instância e adiciona elementos únicos à lista1 atual
        for (Statement element : otherList1) {
            if (!this.getLeftAbstraction().contains(element)) {
                this.getLeftAbstraction().add(element);
            }
        }

        // Percorre a lista2 da outra instância e adiciona elementos únicos à lista2 atual
        for (Statement element : otherList2) {
            if (!this.getRightAbstraction().contains(element)) {
                this.getRightAbstraction().add(element);
            }
        }
    }

    // Implementação do método clone()
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
