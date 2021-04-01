package br.unb.cic.analysis.df;

import br.unb.cic.analysis.model.Conflict;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

class Collector {

    private Set<Conflict> conflicts;
    private Set<HashMap<String, InstanceFieldRef>> instanceFieldDictionary;
    private Set<HashMap<String, StaticFieldRef>> staticFieldDictionary;
    private static Collector instance;

    public Collector() {
        conflicts = new HashSet<>();
        instanceFieldDictionary = new HashSet<>();
        staticFieldDictionary = new HashSet<>();
    }

    public static Collector instance() {
        if(instance == null) {
            instance = new Collector();
        }
        return instance;
    }

    public Set<HashMap<String, InstanceFieldRef>> getHashInstanceField() {
        return instanceFieldDictionary;
    }

    public void addHashInstanceField(HashMap<String, InstanceFieldRef> hash) {
        instanceFieldDictionary.add(hash);
    }

    public Set<HashMap<String, StaticFieldRef>> getHashStaticField() {
        return staticFieldDictionary;
    }

    public void addHashStaticField(HashMap<String, StaticFieldRef> hash) {
        staticFieldDictionary.add(hash) ;
    }

    public void addConflict(Conflict conflict) {
        conflicts.add(conflict);
    }

    public Set<Conflict> getConflicts() {
        return conflicts;
    }

    public void clear() {
        instanceFieldDictionary = new HashSet<>();
        staticFieldDictionary = new HashSet<>();
        conflicts = new HashSet<>();
    }
}
