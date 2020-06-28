package br.unb.cic.analysis.model;

import br.unb.cic.analysis.df.DataFlowAbstraction;
import soot.jimple.internal.JInstanceFieldRef;
import soot.toolkits.scalar.FlowSet;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class KeyAndFlowHash {

    private String uniqueKey;
    private List<String> keys;
    private FlowSet<DataFlowAbstraction> flow;
    private Set<HashMap<String, JInstanceFieldRef>> hash;

    public KeyAndFlowHash(){
    }


    public KeyAndFlowHash(String uniqueKey, FlowSet<DataFlowAbstraction> flow){
        this.uniqueKey = uniqueKey;
        this.flow = flow;
    }

    public KeyAndFlowHash(List<String> keys, FlowSet<DataFlowAbstraction> flow, Set<HashMap<String, JInstanceFieldRef>> hash){
        this.keys = keys;
        this.flow = flow;
        this.hash = hash;
    }
    public List<String> getKeys(){
        return this.keys;
    }

    public String getUniqueKey(){
        return this.uniqueKey;
    }

    public void setUniqueKey(String uniqueKey){
        this.uniqueKey = uniqueKey;
    }

    public FlowSet<DataFlowAbstraction> getFlow(){
        return this.flow;
    }

    public Set<HashMap<String, JInstanceFieldRef>> getHash(){
        return this.hash;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public void setFlow(FlowSet<DataFlowAbstraction> flow) {
        this.flow = flow;
    }

    public void setHash(Set<HashMap<String, JInstanceFieldRef>> hash) {
        this.hash = hash;
    }

}
