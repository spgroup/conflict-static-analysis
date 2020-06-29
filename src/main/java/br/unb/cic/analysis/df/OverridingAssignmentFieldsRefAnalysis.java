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
        FlowSet<DataFlowAbstraction> returnedFlowSet = new ArraySparseSet<>();
        for (DataFlowAbstraction dataIn: in){
            boolean notEquals = true;
            for (DataFlowAbstraction dataOut: out){
                if (dataIn.getStmt().toString().equals(dataOut.getStmt().toString())) {
                    notEquals = false;
                }
            }
            //Add the elements that not are in OUT
            if (notEquals){
                returnedFlowSet.add(dataIn);
            }
        }
        return returnedFlowSet;
    }

    @Override
    protected void detectConflict(FlowSet<DataFlowAbstraction> in, Unit u){
        if (!(isSinkStatement(u) || isSourceStatement(u)) || in.size()==0 || u.getDefBoxes().size()==0){
            return ;
        }

        //Create the objects with your statement type
        KeyAndFlowHash leftObject = getKeyAndFlows(in, Statement.Type.SOURCE);
        KeyAndFlowHash rightObject = getKeyAndFlows(in, Statement.Type.SINK);

        List<String> initialKeyLeft = leftObject.getKeys();
        List<String> initialKeyRight = rightObject.getKeys();

        if (isSourceStatement(u)){
            initialKeyLeft = getKey(u);
        }

        if (isSinkStatement(u)){
            initialKeyRight = getKey(u);
        }

        List<KeyAndFlowHash> generatedLeftList = new ArrayList<>();
        List<KeyAndFlowHash> generatedRightList = new ArrayList<>();

        //If the initial key is not empty, then, there is a possible conflict
        if (!initialKeyLeft.isEmpty()){
            for (String key: initialKeyLeft){
                generatedLeftList.add((getKeyAndElementsOfHashAndFlowList(key, leftObject.getHash(), leftObject.getFlow())));
            }
        }
        if (!initialKeyRight.isEmpty()){
            for (String key: initialKeyRight){
                generatedRightList.add((getKeyAndElementsOfHashAndFlowList(key, rightObject.getHash(), rightObject.getFlow())));
            }
        }

        checkConflicts(generatedLeftList, generatedRightList, u);
    }

    //Return a list with the keys of a Unit
    private List<String> getKey(Unit u){
        List<String> initialKey = new ArrayList<>();
        for (ValueBox v: u.getDefBoxes()){
            if (v.getValue() instanceof JInstanceFieldRef){
                initialKey.add(v.getValue().toString());
            }
        }
        return initialKey;
    }

    private void checkConflicts(List<KeyAndFlowHash> generatedLeftList, List<KeyAndFlowHash> generatedRightList, Unit u) {
        for (KeyAndFlowHash left: generatedLeftList){
            for (KeyAndFlowHash right: generatedRightList){
                if(left.getUniqueKey().equals(right.getUniqueKey())){
                    Statement stmt = null;
                    //Get the statement of the left or right flow
                    if (isSourceStatement(u)){
                        stmt = getStatement(right);
                    }else if (isSinkStatement(u)){
                        stmt = getStatement(left);
                    }
                    Conflict c = new Conflict(stmt, findStatement(u));
                    Collector.instance().addConflict(c);
                }
            }
        }
    }

    //Return the statement of a data flow abstraction
    private Statement getStatement(KeyAndFlowHash statement){
        Iterator<DataFlowAbstraction> data = statement.getFlow().iterator();
        while (data.hasNext()){
             return data.next().getStmt();
        }
        return null;
    }

    //Returns the final key and elements of a hash with its flow list
    private KeyAndFlowHash getKeyAndElementsOfHashAndFlowList(String nextKey, Set<HashMap <String, JInstanceFieldRef>> hash, FlowSet<DataFlowAbstraction> flow){
        List<HashMap<String, JInstanceFieldRef>> auxValuesHashMap = new ArrayList<>();
        List<HashMap<String, JInstanceFieldRef>> listFlowRemoved = new ArrayList<>();
        auxValuesHashMap.addAll(hash);
        KeyAndFlowHash elements = new KeyAndFlowHash(nextKey, flow);

        //If nextKey not contain $stack is because simple key
        if (!nextKey.contains("$stack")){
            return elements;
        }

        JInstanceFieldRef currentField = null;

        //The second position is the field called
        String currentUniqueKey = "<"+nextKey.split(".<")[1];

        //The first key comes before ".<" if you have a stack as a substring
        nextKey = nextKey.split(".<")[0];
        boolean isNextKey = true;
        while(auxValuesHashMap.size()>0 && isNextKey) {
            if (nextKey.contains("$stack")) {
                isNextKey = false;
                for (HashMap<String, JInstanceFieldRef> auxMap : hash) {
                    for (String mapKey : auxMap.keySet()) {
                        if (mapKey.equals(nextKey)) {
                            currentField = auxMap.get(mapKey);
                            listFlowRemoved.add(auxMap);
                            isNextKey = true;
                            auxValuesHashMap.remove(auxMap);
                        }
                    }
                    if (isNextKey) {
                        break;
                    }
                }
            }else{
                isNextKey = false;
            }
            boolean fieldIsNull = currentField==null;
            if ((!fieldIsNull) && (!isNextKey)) {
                currentUniqueKey = nextKey + (currentUniqueKey.equals("") ? "" : ".") + currentUniqueKey;
            } else if (!fieldIsNull){
                currentUniqueKey = currentField.getFieldRef().toString() + (currentUniqueKey.equals("") ? "" : ".") + currentUniqueKey;
                nextKey = currentField.getBase().toString(); //Update the nextKey and repeat until the condition
            }
        }
        //If auxValuesHasMap is equal to zero, there is a next key and currentField is not null, then it is the starting field of the object
        if (!(currentField==null) && (isNextKey)){
            currentUniqueKey = nextKey + (currentUniqueKey.equals("") ? "" : ".") + currentUniqueKey;
        }

        //Update key and flow
        elements.setUniqueKey(currentUniqueKey);
        if (!flow.isEmpty()) {
            //Remove all the elements equals in listFlowRemoved and flow
            elements.setFlow(removeFlowSet(listFlowRemoved, flow));
        }
        return elements;
    }

    //Checks which elements have to be removed from the flowSet according with a HasMap
    private FlowSet<DataFlowAbstraction> removeFlowSet(List<HashMap<String, JInstanceFieldRef>> listFLowRemoved, FlowSet<DataFlowAbstraction> flow){
        FlowSet<DataFlowAbstraction> flowSetReturn = new ArraySparseSet<>();

        for (HashMap<String, JInstanceFieldRef> map : listFLowRemoved){
            for (String mapKey : map.keySet()) {
                for(DataFlowAbstraction data: flow) {
                    if (map.get(mapKey).toString().equals(data.getJInstanceFieldRef().toString())) {
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

        KeyAndFlowHash leftObject = getKeyAndFlows(in, Statement.Type.SOURCE);
        KeyAndFlowHash rightObject = getKeyAndFlows(in, Statement.Type.SINK);
        KeyAndFlowHash baseObject = getKeyAndFlows(in, Statement.Type.IN_BETWEEN);

        List<String> initialKeyLeft = leftObject.getKeys();
        List<String> initialKeyRight = rightObject.getKeys();

        String initialKeyBase = "";

        if (u.getDefBoxes().size() > 0) {
            for (ValueBox v : u.getDefBoxes()) {
                if (v.getValue() instanceof JInstanceFieldRef) {
                    initialKeyBase = v.getValue().toString();
                }
            }
        }
        FlowSet<DataFlowAbstraction> flowSetReturn = new ArraySparseSet<>();

        if (!initialKeyRight.isEmpty()) {
            flowSetReturn = returnedFlow(initialKeyBase, baseObject.getHash(), baseObject.getFlow(), initialKeyRight, rightObject.getHash(), rightObject.getFlow());
        }
        if (!initialKeyLeft.isEmpty()){
            for (DataFlowAbstraction flow : returnedFlow(initialKeyBase, baseObject.getHash(), baseObject.getFlow(), initialKeyLeft, leftObject.getHash(), leftObject.getFlow())){
                flowSetReturn.add(flow);
            }
            return flowSetReturn;
        }

        return flowSetReturn;
    }

    //Returns the flows from two equals getKeyAndElementsOfHashAndFlowList, left ou right with base statement
    private FlowSet<DataFlowAbstraction> returnedFlow(String initialKeyBase, Set<HashMap<String, JInstanceFieldRef>> hashBase, FlowSet<DataFlowAbstraction> baseFlow,
                                                      List<String> comparedInitialKey, Set<HashMap<String, JInstanceFieldRef>> comparedHashMap, FlowSet<DataFlowAbstraction> comparedFlow){

        FlowSet<DataFlowAbstraction> flowSetReturn = new ArraySparseSet<>();
        FlowSet<DataFlowAbstraction> initialFlow = newInitialFlow();
        String generatedBase = "";
        if (initialKeyBase != "") {
            generatedBase = (getKeyAndElementsOfHashAndFlowList(initialKeyBase, hashBase, initialFlow)).getUniqueKey();
        }

        for (String key : comparedInitialKey) {
            String left = (getKeyAndElementsOfHashAndFlowList(key, comparedHashMap, initialFlow)).getUniqueKey();
            if (left.equals(generatedBase)) {
                for (DataFlowAbstraction flow : getKeyAndElementsOfHashAndFlowList(initialKeyBase, hashBase, baseFlow).getFlow()) {
                    flowSetReturn.add(flow);
                }

                for (DataFlowAbstraction flow : getKeyAndElementsOfHashAndFlowList(key, comparedHashMap, comparedFlow).getFlow()) {
                    flowSetReturn.add(flow);
                }
                return flowSetReturn;
            }
        }
        return flowSetReturn;

    }

    //Returns the final key and the flow of the IN elements
    private KeyAndFlowHash getKeyAndFlows(FlowSet<DataFlowAbstraction> in, Statement.Type statementType){

        List <String> initialKey = new ArrayList<>();
        FlowSet<DataFlowAbstraction> flow = new ArraySparseSet<>();
        Set<HashMap<String, JInstanceFieldRef>> returnedHashMap = new HashSet<>();

        for (DataFlowAbstraction filterIn : in) {
            HashMap<String, JInstanceFieldRef> auxHashMap = new HashMap<>();
            StringBuilder strKey = new StringBuilder();
            for (ValueBox valueBoxKey : filterIn.getStmt().getUnit().getDefBoxes()) {
                strKey.append(valueBoxKey.getValue().toString());
            }
            JInstanceFieldRef currentFieldRef = null;
            for (ValueBox catchRef : filterIn.getStmt().getUnit().getUseBoxes()) {
                if (catchRef.getValue() instanceof JInstanceFieldRef) {
                    currentFieldRef = (JInstanceFieldRef) catchRef.getValue();
                    auxHashMap.put(strKey.toString(), currentFieldRef);
                }
            }
            if (filterIn.getStmt().getType().equals(statementType)) {
                if (auxHashMap.size() != 0) returnedHashMap.add(auxHashMap);
                flow.add(filterIn);
                if (isJInstanceFieldRef(currentFieldRef)) {
                    initialKey.add(strKey.toString());
                }
            }
        }
        return new KeyAndFlowHash(initialKey, flow, returnedHashMap);
    }
}