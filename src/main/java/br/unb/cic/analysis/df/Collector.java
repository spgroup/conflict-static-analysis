package br.unb.cic.analysis.df;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Collector {

    private Set<String> conflicts;
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


    public void addConflict(String conflict) {
        conflicts.add(conflict);
    }

    public Set<String> getConflicts() {
        return conflicts;
    }

    public void clear() {
        conflicts = new HashSet<>();
    }
}
