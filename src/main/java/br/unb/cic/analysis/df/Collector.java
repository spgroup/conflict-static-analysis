package br.unb.cic.analysis.df;

import br.unb.cic.analysis.model.Conflict;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.JInstanceFieldRef;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

class Collector {

    private Set<Conflict> conflicts;
    private Set<HashMap<String, JInstanceFieldRef>> hashMap;
    private Set<HashMap<String, StaticFieldRef>> hashMapStatic;
    private static Collector instance;

    public Collector() {
        conflicts = new HashSet<>();
        hashMap = new HashSet<>();
        hashMapStatic = new HashSet<>();
    }

    public static Collector instance() {
        if(instance == null) {
            instance = new Collector();
        }
        return instance;
    }

    public Set<HashMap<String, JInstanceFieldRef>> getHash() {
        return hashMap;
    }

    public void addHash(HashMap<String, JInstanceFieldRef> hash) {
        hashMap.add(hash) ;
    }

    public Set<HashMap<String, StaticFieldRef>> getHashStatic() {
        return hashMapStatic;
    }

    public void addHashStatic(HashMap<String, StaticFieldRef> hash) {
        hashMapStatic.add(hash) ;
    }

    public void addConflict(Conflict conflict) {
        conflicts.add(conflict);
    }

    public Set<Conflict> getConflicts() {
        return conflicts;
    }

    public void clear() {
        hashMap = new HashSet<>();
        hashMapStatic = new HashSet<>();
        conflicts = new HashSet<>();
    }
}
