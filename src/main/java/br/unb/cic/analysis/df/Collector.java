package br.unb.cic.analysis.df;

import br.unb.cic.analysis.model.Conflict;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.JInstanceFieldRef;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

class Collector {

    private Set<Conflict> conflicts;
    private Set<HashMap<String, JInstanceFieldRef>> hashMapJInstanceField;
    private Set<HashMap<String, StaticFieldRef>> hashMapStaticField;
    private static Collector instance;

    public Collector() {
        conflicts = new HashSet<>();
        hashMapJInstanceField = new HashSet<>();
        hashMapStaticField = new HashSet<>();
    }

    public static Collector instance() {
        if(instance == null) {
            instance = new Collector();
        }
        return instance;
    }

    public Set<HashMap<String, JInstanceFieldRef>> getHashJInstanceField() {
        return hashMapJInstanceField;
    }

    public void addHashJInstanceField(HashMap<String, JInstanceFieldRef> hash) {
        hashMapJInstanceField.add(hash) ;
    }

    public Set<HashMap<String, StaticFieldRef>> getHashStaticField() {
        return hashMapStaticField;
    }

    public void addHashStaticField(HashMap<String, StaticFieldRef> hash) {
        hashMapStaticField.add(hash) ;
    }

    public void addConflict(Conflict conflict) {
        conflicts.add(conflict);
    }

    public Set<Conflict> getConflicts() {
        return conflicts;
    }

    public void clear() {
        hashMapJInstanceField = new HashSet<>();
        hashMapStaticField = new HashSet<>();
        conflicts = new HashSet<>();
    }
}
