package br.unb.cic.analysis.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OAConflictReport {

    private Set<Conflict> conflicts;

    public OAConflictReport() {
        this.conflicts = new HashSet<>();
    }

    public Set<Conflict> getConflicts() {
        return conflicts;
    }

    public void setConflicts(Set<Conflict> conflicts) {
        this.conflicts = conflicts;
    }

    public void addConflict(Conflict conflict) {
        conflicts.add(conflict);
    }

    public Set<Conflict> filterConflictsWithSameRoot(Set<Conflict> conflictsResults) {
        Set<Conflict> conflictsFilter = new HashSet<>();

        for (Conflict conflict : conflictsResults) {

            if (!hasConflictWithSameSourceAndSinkRootTraversedLine(conflictsFilter, conflict)
                    && !conflict.getSourceUnit().getDefBoxes().get(0).getValue().toString().contains("$stack")) {
                conflictsFilter.add(conflict);
            }
        }
        return conflictsFilter;
    }

    private boolean hasConflictWithSameSourceAndSinkRootTraversedLine(Set<Conflict> conflictsFilter, Conflict conflict) {
        for (Conflict c : conflictsFilter) {
            if (c.conflictsHaveSameSourceAndSinkRootTraversedLine(conflict)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        this.conflicts.clear();
    }

    public boolean contains(Conflict conflict) {
        return getConflicts().contains(conflict);
    }

    public void report() {
        for (Conflict conflict : filterConflictsWithSameRoot(getConflicts())) {
            System.out.printf("OA interference in class %s, method %s, execution of line %s overrides %s, assigning to variable %s, \nCaused by line %s flow:\n%sAnd line %s flow:\n%s %n",
                    conflict.getSourceTraversedLine().get(0).getSootClass().getShortJavaStyleName(),
                    conflict.getSourceTraversedLine().get(0).getSootMethod().getSubSignature(),
                    conflict.getSourceTraversedLine().get(0).getLineNumber(),
                    conflict.getSinkTraversedLine().get(0).getLineNumber(),
                    conflict.getSourceUnit().getDefBoxes().get(0).getValue(),
                    conflict.getSourceTraversedLine().get(0).getLineNumber(),
                    reportTraversedLine(conflict.getSourceTraversedLine()),
                    conflict.getSinkTraversedLine().get(0).getLineNumber(),
                    reportTraversedLine(conflict.getSinkTraversedLine()));
        }
    }

    private String reportTraversedLine(List<TraversedLine> traversedLineList) {
        StringBuilder formatedReport = new StringBuilder();
        for (TraversedLine traversedLine : traversedLineList) {
            formatedReport.append(String.format("%s\n", traversedLine.toString()));
        }
        return formatedReport.toString();
    }
}
