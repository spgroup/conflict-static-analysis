package br.unb.cic.analysis.df;

import br.unb.cic.analysis.model.Conflict;

import java.util.HashSet;
import java.util.Set;

class Collector {

    private Set<Conflict> conflicts;
    private static Collector instance;

    public Collector() {
        conflicts = new HashSet<>();
    }
    public static Collector instance() {
        if(instance == null) {
            instance = new Collector();
        }
        return instance;
    }


    public void addConflict(Conflict conflict) {
        conflicts.add(conflict);
    }

    public Set<Conflict> getConflicts() {
        return conflicts;
    }

    public void clear() {
        conflicts = new HashSet<>();
    }
}
