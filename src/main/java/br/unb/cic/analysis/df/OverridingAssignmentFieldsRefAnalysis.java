package br.unb.cic.analysis.df;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.KeyAndFlowHash;
import br.unb.cic.analysis.model.Statement;
import soot.Body;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.internal.JInstanceFieldRef;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import java.util.*;

public class OverridingAssignmentFieldsRefAnalysis extends ReachDefinitionAnalysis {

    /**
     * Constructor of the DataFlowAnalysis class.
     * <p>
     * According to the SOOT architecture, the constructor for a
     * flow analysis must receive as an argument a graph, set up
     * essential information and call the doAnalysis method of the
     * super class.
     *
     * @param definition a set of conflict definitions.
     */
    public OverridingAssignmentFieldsRefAnalysis(Body methodBody, AbstractMergeConflictDefinition definition) {
        super(methodBody, definition);
    }

    @Override
    protected FlowSet<DataFlowAbstraction> gen(Unit u, FlowSet<DataFlowAbstraction> in) {
        FlowSet<DataFlowAbstraction> res = new ArraySparseSet<>();
        //Add JInstanceFieldRefs values in abstraction
        if (isSourceStatement(u) || isSinkStatement(u)) {
            u.getUseAndDefBoxes().stream().filter(v -> v.getValue() instanceof JInstanceFieldRef).forEach(v -> {
                Statement stmt = isSourceStatement(u) ? findSourceStatement(u) : findSinkStatement(u);
                res.add(new DataFlowAbstraction((JInstanceFieldRef) v.getValue(), stmt));
            });
        }else if (u.getDefBoxes().size() > 0) {
            u.getUseAndDefBoxes().stream().filter(v -> v.getValue() instanceof JInstanceFieldRef).forEach(v -> {
                res.add(new DataFlowAbstraction((JInstanceFieldRef) v.getValue(), getStatementBase(u)));
            });
        }
        return res;
    }

    @Override
    protected void flowThrough(FlowSet<DataFlowAbstraction> in, Unit u, FlowSet<DataFlowAbstraction> out) {
        detectConflict(in, u);
        //if IN has base elements, remove them
        FlowSet<DataFlowAbstraction> temp = difference(in, baseStatementIsIn(in, u));
        temp.union(gen(u, in), out);
    }

    //Make the difference between two FLOWSETs in relation to Statements
    private FlowSet<DataFlowAbstraction> difference(FlowSet<DataFlowAbstraction> in, FlowSet<DataFlowAbstraction> out){
        FlowSet<DataFlowAbstraction> aux = new ArraySparseSet<>();
        for (DataFlowAbstraction dataIn: in){
            boolean notEquals = true;
            for (DataFlowAbstraction dataOut: out){
                if (dataIn.getStmt().toString().equals(dataOut.getStmt().toString())) {
                    notEquals = false;
                }
            }
            //Add the elements that not are in OUT
            if (notEquals){
                aux.add(dataIn);
            }
        }
        return aux;
    }

    @Override
    protected void detectConflict(FlowSet<DataFlowAbstraction> in, Unit u){
        if (!(isSinkStatement(u) || isSourceStatement(u)) || in.size()==0 || u.getDefBoxes().size()==0){
            return ;
        }

        //Create the objects with your statement type
        KeyAndFlowHash leftObject = getKeyAndFlowsOfIn(in, Statement.Type.SOURCE);
        KeyAndFlowHash rightObject = getKeyAndFlowsOfIn(in, Statement.Type.SINK);

        List<String> inicialKeyLeft = leftObject.getKeys();
        List<String> inicialKeyRight = rightObject.getKeys();

        if (isSourceStatement(u)){
            inicialKeyLeft = getKey(u);
        }

        if (isSinkStatement(u)){
            inicialKeyRight = getKey(u);
        }

        List<KeyAndFlowHash> generatedLeftList = new ArrayList<>();
        List<KeyAndFlowHash> generatedRightList = new ArrayList<>();

        if (!inicialKeyLeft.isEmpty()){
            for (String key: inicialKeyLeft){
                generatedLeftList.add((getKeyAndElementsOfHashAndFlowList(key, leftObject.getHash(), leftObject.getFlow())));
            }
        }
        if (!inicialKeyRight.isEmpty()){
            for (String key: inicialKeyRight){
                generatedRightList.add((getKeyAndElementsOfHashAndFlowList(key, rightObject.getHash(), rightObject.getFlow())));
            }
        }

        checkConflicts(generatedLeftList, generatedRightList, u);
    }

    //Return a list with the keys of a Unit
    private List<String> getKey(Unit u){
        List<String> inicialKey = new ArrayList<>();
        for (ValueBox v: u.getDefBoxes()){
            if (v.getValue() instanceof JInstanceFieldRef){
                inicialKey.add(v.getValue().toString());
            }
        }
        return inicialKey;
    }

    private void checkConflicts(List<KeyAndFlowHash> generatedLeftList, List<KeyAndFlowHash> generatedRightList, Unit u) {
        for (KeyAndFlowHash left: generatedLeftList){
            for (KeyAndFlowHash right: generatedRightList){
                if(left.getUniqueKey().equals(right.getUniqueKey())){
                    Statement stmt = null;
                    if (isSourceStatement(u)){
                        stmt = getStatement(right);
                    }
                    if (isSinkStatement(u)){
                        stmt = getStatement(left);
                    }
                    Conflict c = new Conflict(stmt, findStatement(u));
                    Collector.instance().addConflict(c);
                }
            }
        }
    }

    private Statement getStatement(KeyAndFlowHash statement){
        Iterator<DataFlowAbstraction> data = statement.getFlow().iterator();
        while (data.hasNext()){
             return data.next().getStmt();
        }
        return null;
    }

    //Returns the final key and elements of a hash with its flow list
    private KeyAndFlowHash getKeyAndElementsOfHashAndFlowList(String nextKey, Set<HashMap <String, JInstanceFieldRef>> hashValues, FlowSet<DataFlowAbstraction> flow){
        List<HashMap<String, JInstanceFieldRef>> auxValuesHasMap = new ArrayList<>();
        List<HashMap<String, JInstanceFieldRef>> listFlowRemoved = new ArrayList<>();
        auxValuesHasMap.addAll(hashValues);
        KeyAndFlowHash elements = new KeyAndFlowHash(nextKey, flow);

        //If nextKey not contain $stack is because simple key
        if (!nextKey.contains("$stack")){
            return elements;
        }

        JInstanceFieldRef auxField = null;

        //The second position is the field called
        String aux = "<"+nextKey.split(".<")[1];

        //The first key comes before. <If you have a stack as a substring
        nextKey = nextKey.split(".<")[0];
        boolean isNextKey = true;
        while(auxValuesHasMap.size()>0 && isNextKey) {
            if (nextKey.contains("$stack")) {
                isNextKey = false;
                for (HashMap<String, JInstanceFieldRef> auxMap : hashValues) {
                    for (String mapKey : auxMap.keySet()) {
                        if (mapKey.equals(nextKey)) {
                            auxField = auxMap.get(mapKey);
                            listFlowRemoved.add(auxMap);
                            isNextKey = true;
                            auxValuesHasMap.remove(auxMap);
                        }
                    }
                    if (isNextKey) {
                        break;
                    }
                }
            }else{
                isNextKey = false;
            }
            boolean auxF = auxField==null;
            if ((!auxF) && (!isNextKey)) {
                aux = nextKey + (aux.equals("") ? "" : ".") + aux;
            } else if (!auxF){
                aux = auxField.getFieldRef().toString() + (aux.equals("") ? "" : ".") + aux;
                nextKey = auxField.getBase().toString(); //Update the nextKey and repeat until the condition
            }
        }
        //If auxValuesHasMap is equal to zero and nextKey does not contain a "$stack", then it is the starting field of the object
        if (!nextKey.contains("$stack") && (isNextKey)){
            aux = nextKey + (aux.equals("") ? "" : ".") + aux;
        }

        //Update key and flow
        elements.setUniqueKey(aux);
        if (!flow.isEmpty()) {
            //Remove all the elements equals in listFlowRemoved and flow
            elements.setFlow(removeFlowSet(listFlowRemoved, flow));
        }
        return elements;
    }

    //Checks which elements have to be removed from the flowSet according with a HasMap
    private FlowSet<DataFlowAbstraction> removeFlowSet(List<HashMap<String, JInstanceFieldRef>> listFLowRemoved, FlowSet<DataFlowAbstraction> flow){
        FlowSet<DataFlowAbstraction> flowSetReturn = new ArraySparseSet<>();

        for (HashMap<String, JInstanceFieldRef> auxMap : listFLowRemoved){
            for (String mapKey : auxMap.keySet()) {
                for(DataFlowAbstraction data: flow) {
                    if (auxMap.get(mapKey).toString().equals(data.getJInstanceFieldRef().toString())) {
                        String def = "";
                        for (ValueBox i: data.getStmt().getUnit().getDefBoxes()){
                            def = i.getValue().toString();
                        }
                        if (def.equals(mapKey)) {
                            flowSetReturn.add(data);
                        }
                    }
                }
            }
        }
        return flowSetReturn;
    }

    //If ref field is null, then is a key definition of getDefUseBox()
    private boolean isJInstanceFieldRef(JInstanceFieldRef ref){
        return ref==null;
    }

    //if IN has base elements, remove them
    private FlowSet<DataFlowAbstraction> baseStatementIsIn(FlowSet<DataFlowAbstraction> in, Unit u) {

        if ((isSinkStatement(u) || isSourceStatement(u))) {
            return new ArraySparseSet<>();
        }

        KeyAndFlowHash leftObject = getKeyAndFlowsOfIn(in, Statement.Type.SOURCE);
        KeyAndFlowHash rightObject = getKeyAndFlowsOfIn(in, Statement.Type.SINK);
        KeyAndFlowHash baseObject = getKeyAndFlowsOfIn(in, Statement.Type.IN_BETWEEN);

        List<String> inicialKeyLeft = leftObject.getKeys();
        List<String> inicialKeyRight = rightObject.getKeys();

        String inicialKeyBase = "";

        if (u.getDefBoxes().size() > 0) {
            for (ValueBox v : u.getDefBoxes()) {
                if (v.getValue() instanceof JInstanceFieldRef) {
                    inicialKeyBase = v.getValue().toString();
                }
            }
        }
        FlowSet<DataFlowAbstraction> flowSetReturn = new ArraySparseSet<>();

        if (!inicialKeyRight.isEmpty()) {
            flowSetReturn = flowReturned(inicialKeyBase, baseObject.getHash(), baseObject.getFlow(), inicialKeyRight, rightObject.getHash(), rightObject.getFlow());
        }
        if (!inicialKeyLeft.isEmpty()){
            for (DataFlowAbstraction flow : flowReturned(inicialKeyBase, baseObject.getHash(), baseObject.getFlow(), inicialKeyLeft, leftObject.getHash(), leftObject.getFlow())){
                flowSetReturn.add(flow);
            }
            return flowSetReturn;
        }

        return flowSetReturn;
    }

    //Returns the flows from two equals getKeyAndElementsOfHashAndFlowList, left ou right with base statement
    private FlowSet<DataFlowAbstraction> flowReturned(String inicialKeyBase, Set<HashMap<String, JInstanceFieldRef>> hashBase, FlowSet<DataFlowAbstraction> baseFlow,
                                                        List<String> inicialKeyLeft, Set<HashMap<String, JInstanceFieldRef>> hashLeft, FlowSet<DataFlowAbstraction> leftFlow){

        FlowSet<DataFlowAbstraction> flowSetReturn = new ArraySparseSet<>();
        FlowSet<DataFlowAbstraction> inicialFlow = newInitialFlow();
        String generatedBase = "";
        if (inicialKeyBase != "") {
            generatedBase = (getKeyAndElementsOfHashAndFlowList(inicialKeyBase, hashBase, inicialFlow)).getUniqueKey();
        }

        for (String key : inicialKeyLeft) {
            String left = (getKeyAndElementsOfHashAndFlowList(key, hashLeft, inicialFlow)).getUniqueKey();
            if (left.equals(generatedBase)) {
                for (DataFlowAbstraction flow : getKeyAndElementsOfHashAndFlowList(inicialKeyBase, hashBase, baseFlow).getFlow()) {
                    flowSetReturn.add(flow);
                }

                for (DataFlowAbstraction flow : getKeyAndElementsOfHashAndFlowList(key, hashLeft, leftFlow).getFlow()) {
                    flowSetReturn.add(flow);
                }
                return flowSetReturn;
            }
        }
        return flowSetReturn;

    }

    //Returns the final key and the flow of the IN elements
    private KeyAndFlowHash getKeyAndFlowsOfIn(FlowSet<DataFlowAbstraction> in, Statement.Type type){

        List <String> inicialKey = new ArrayList<>();
        FlowSet<DataFlowAbstraction> flow = new ArraySparseSet<>();
        Set<HashMap<String, JInstanceFieldRef>> auxHash = new HashSet<>();

        for (DataFlowAbstraction filterIn : in) {
            HashMap<String, JInstanceFieldRef> aux = new HashMap<>();
            String strKey = "";
            for (ValueBox valueBoxKey : filterIn.getStmt().getUnit().getDefBoxes()) {
                strKey += valueBoxKey.getValue().toString();
            }
            JInstanceFieldRef auxRef = null;
            for (ValueBox catchRef : filterIn.getStmt().getUnit().getUseBoxes()) {
                if (catchRef.getValue() instanceof JInstanceFieldRef) {
                    auxRef = (JInstanceFieldRef) catchRef.getValue();
                    aux.put(strKey, auxRef);
                }
            }
            if (filterIn.getStmt().getType().equals(type)) {
                if (aux.size() != 0) auxHash.add(aux);
                flow.add(filterIn);
                if (isJInstanceFieldRef(auxRef)) {
                    inicialKey.add(strKey);
                }
            }
        }
        return new KeyAndFlowHash(inicialKey, flow, auxHash);
    }
}