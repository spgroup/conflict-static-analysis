package br.unb.cic.analysis.model;

import br.unb.cic.analysis.df.DataFlowAbstraction;
import soot.jimple.internal.JInstanceFieldRef;
import soot.toolkits.scalar.FlowSet;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class KeyAndFlowElements {

    private String key;
    private FlowSet<DataFlowAbstraction> flow;

    public KeyAndFlowElements(){
    }

    public KeyAndFlowElements(String key, FlowSet<DataFlowAbstraction> flow){
        this.key = key;
        this.flow = flow;
    }
    public String getKey(){
        return this.key;
    }

    public FlowSet<DataFlowAbstraction> getFlow(){
        return this.flow;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setFlow(FlowSet<DataFlowAbstraction> flow) {
        this.flow = flow;
    }


}
