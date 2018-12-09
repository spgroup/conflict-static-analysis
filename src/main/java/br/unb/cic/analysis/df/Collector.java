package br.unb.cic.analysis.df;

import java.util.ArrayList;
import java.util.List;

public class Collector {

    private List<String> conflicts;
    private static Collector instance;

    public Collector() {
        conflicts = new ArrayList<>();
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

    public List<String> getConflicts() {
        return conflicts;
    }
}
