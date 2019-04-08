package br.unb.cic.analysis.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ConflictReport<T> implements Iterable<T> {
    public Set<T> conflicts;

    public ConflictReport() {
        conflicts = new HashSet<>();
    }


    public void  addConflict(T conflict) {
        conflicts.add(conflict);
    }

    @Override
    public Iterator<T> iterator() {
        return conflicts.iterator();
    }
}
