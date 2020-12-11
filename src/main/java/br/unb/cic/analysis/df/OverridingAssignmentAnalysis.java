package br.unb.cic.analysis.df;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.Statement;
import soot.*;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JInstanceFieldRef;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class OverridingAssignmentAnalysis extends ReachDefinitionAnalysis {

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
    public OverridingAssignmentAnalysis(Body methodBody, AbstractMergeConflictDefinition definition) {
        super(methodBody, definition);
    }

    /**
     * Runs the algorithm analysis at a given statement (Unit d). Here we
     * manipulate and compute an out set from the income set in (foward analysis).
     * <p>
     * When left or right assign a variable it is added to the abstraction.
     * <p>
     * If there is an assignment coming from the base for the same variable that had
     * been assigned by left or right then the variable is removed from the abstraction.
     * <p>
     * In the case of arrays, the variable is added to the abstraction no matter if the
     * assignment is just for one of its components, as in array[1] = 2.
     * <p>
     * Conflicts are reported when the same variable is used by developer left and developer Right.
     *
     * @param in a set of abstractions that arrive at the statement d
     * @param u  a specific statement
     * @out the result of applying the analysis considering the income abstraction and the statement d
     */
    @Override
    protected void flowThrough(FlowSet<DataFlowAbstraction> in, Unit u, FlowSet<DataFlowAbstraction> out) {

        detectConflict(in, u);
        FlowSet<DataFlowAbstraction> temp = new ArraySparseSet<>();

        FlowSet<DataFlowAbstraction> killSet = new ArraySparseSet<>();

        for (DataFlowAbstraction item : in) {
            if (abstractionVariableIsInIUnitDefBoxes(item, u)) {
                killSet.add(item);
            }
        }
        in.difference(killSet, temp);
        temp.union(gen(u, in), out);
    }

    /*
     * Here we separate the Unit types to create the DataFlowAbstraction according to its specific constructor.
     */
    @Override
    protected FlowSet<DataFlowAbstraction> gen(Unit u, FlowSet<DataFlowAbstraction> in) {
        FlowSet<DataFlowAbstraction> res = new ArraySparseSet<>();

        generateFieldDictionary(u);

        if (isLeftStatement(u) || isRightStatement(u)) {
            Statement stmt = getStatementAssociatedWithUnit(u);
            u.getDefBoxes().forEach(valueBox -> {
                if (valueBox.getValue() instanceof Local){
                    res.add(new DataFlowAbstraction((Local) valueBox.getValue(), stmt));
                } else if (valueBox.getValue() instanceof JArrayRef){
                    res.add(new DataFlowAbstraction(getArrayName(valueBox), stmt));
                } else if (valueBox.getValue() instanceof  StaticFieldRef) {
                    res.add(new DataFlowAbstraction((StaticFieldRef) valueBox.getValue(), stmt));
                } else if (valueBox.getValue() instanceof JInstanceFieldRef){
                    res.add(new DataFlowAbstraction(getFieldName(valueBox), stmt));
                }
            });
        }
        return res;
    }

    /*
     * To detect conflicts in verified if "u" is owned by LEFT or RIGHT
     * and we fill in the "potentialConflictingAssignments" list with the changes from the other developer.
     *
     * We pass "u" and "potentialConflictingAssignments" to the checkConflits method
     * to see if Left assignments interfere with Right changes or
     * Right assignments interfere with Left changes.
     */
    @Override
    protected void detectConflict(FlowSet<DataFlowAbstraction> in, Unit u) {
        if (!(isRightStatement(u) || isLeftStatement(u))) {
            return;
        }

        List<DataFlowAbstraction> potentialConflictingAssignments = new ArrayList<>();

        if (isRightStatement(u)) {
            potentialConflictingAssignments = in.toList().stream().filter(DataFlowAbstraction::containsLeftStatement).collect(Collectors.toList());
        } else if (isLeftStatement(u)) {
            potentialConflictingAssignments = in.toList().stream().filter(DataFlowAbstraction::containsRightStatement).collect(Collectors.toList());
        }

        checkConflicts(u, potentialConflictingAssignments);

    }

    /*
     * Checks if there is a conflict and if so adds it to the conflict list.
     */
    private void checkConflicts(Unit u, List<DataFlowAbstraction> potentialConflictingAssignments) {
        for (DataFlowAbstraction dataFlowAbstraction : potentialConflictingAssignments) {
            if (abstractionVariableIsInIUnitDefBoxes(dataFlowAbstraction, u)) {
                Conflict c = new Conflict(dataFlowAbstraction.getStmt(), findStatement(u));
                Collector.instance().addConflict(c);
            }
        }
    }

    /*
     * Checks the equality of variable names between an
     * item in the abstraction list and all the UnitBox's ValueBox.
     */
    private boolean abstractionVariableIsInIUnitDefBoxes(DataFlowAbstraction dataFlowAbstraction, Unit u) {
        for (ValueBox valueBox : u.getDefBoxes()) {
            Object value = valueBox.getValue();
            if (value instanceof JInstanceFieldRef && dataFlowAbstraction.getFieldRef()!=null) {
                return dataFlowAbstraction.getFieldRef().toString().equals(getFieldsChain(value.toString()));
            } else if (value instanceof JArrayRef && value.toString().contains("$stack")) { // If contain $stack, contain a array chain
                return getVarNameFromAbstraction(dataFlowAbstraction).equals(getArrayChain(valueBox));
            } else{
                return getVarNameFromAbstraction(dataFlowAbstraction).equals(getVarNameFromValueBox(valueBox));
            }
        }
        return false;
    }

    /*
     * Returns a String containing the name of the variable given a DataFlowAbstraction
     */
    private String getVarNameFromAbstraction(DataFlowAbstraction dataFlowAbstraction) {
        String varNameDataFlowAbstraction = "";
        Object local = dataFlowAbstraction.getLocal();
        Object localStaticRef = dataFlowAbstraction.getLocalStaticRef();
        if (local != null) {
            varNameDataFlowAbstraction = getLocalName((Local) local);
        } else if (localStaticRef != null) {
            varNameDataFlowAbstraction = getStaticName((StaticFieldRef) localStaticRef);
        }
        return varNameDataFlowAbstraction;
    }

    /*
     * Returns a String containing the name of the variable given a ValueBox
     */
    private String getVarNameFromValueBox(ValueBox valueBox) {
        String varNameValueBox = "";
        if (valueBox.getValue() instanceof Local) {
            varNameValueBox = getLocalName((Local) valueBox.getValue());
        } else if (valueBox.getValue() instanceof StaticFieldRef) {
            varNameValueBox = getStaticName((StaticFieldRef) valueBox.getValue());
        } else if (valueBox.getValue() instanceof JArrayRef) {
            varNameValueBox = getArrayRefName((JArrayRef) valueBox.getValue()).toString();
        }
        return varNameValueBox;
    }

    /*
     * Returns the Statement type
     */
    private Statement getStatementAssociatedWithUnit(Unit u) {
        if (isLeftStatement(u)) {
            return findLeftStatement(u);
        } else if (isRightStatement(u)){
            return findRightStatement(u);
        }
        return findStatementBase(u);
    }

    /*
     * Divide the Local name into an array
     * separated by '#' to get just the name of the variable;
     * e.g: x#3 --> x
     */
    private String getLocalName(Local local) {
        return local.getName().split("#")[0];
    }

    /*
     * Returns a String containing the name
     * of the variable used to assign an array;
     * e.g:  int[] arr = {0, 1}; --> "arr"
     */
    private Value getArrayRefName(JArrayRef arrayRef) {
        return arrayRef.getBaseBox().getValue();
    }

    /*
     * If it's a InstanceField, returns your complete name from the hashMapInstanceField
     */
    private InstanceFieldRef getFieldName(ValueBox valueBox) {
        InstanceFieldRef fieldValue = (InstanceFieldRef) valueBox.getValue();
        if (fieldValue.toString().contains("$stack")) {
            String chainName = getFieldsChain(valueBox.getValue().toString()); //return the complete chain with the FieldRef
            ((Local) fieldValue.getBase()).setName(chainName.replace("." + fieldValue.getField().toString(), "")); //remove the double FieldRef from jInstanceFieldValue
        }
        return fieldValue;
    }

    /*
     * If it's a Static Array, returns your complete name from the dictionary
     * Else, returns the Array Name
     */
    private Local getArrayName(ValueBox valueBox){
        Local localName = (Local) getArrayRefName((JArrayRef) valueBox.getValue());
        if (localName.toString().contains("$stack")) {
            localName.setName(getArrayChain(valueBox));
        }
        return localName;
    }

    /*
     * Returns a String containing the name
     * of the variable used to assign a static variable;
     * e.g:  private static int x; --> "x"
     */
    private String getStaticName(StaticFieldRef staticField) {
        return staticField.getField().getName();
    }

    /*
     * Returns the name of the JArrayRef
     * The Jimple code is divided in others units
     * Example:
     * static int[] y;
     * y[0] = 3; //left
     *
     * Jimple code:
     *
     * $stack2 = <className: int[] y>;
     * $stack2[0] = 3; // $stack2 is the key
     *
     * The result is:
     * $stack2[0] -> $stack2 = <className: int[] y> -> (STOP) -> Result: <className: int[] y>
     */
    private String getArrayChain(ValueBox valueBox){
        String nextKey = ((JArrayRef) valueBox.getValue()).getBase().toString();
        for (HashMap<String, StaticFieldRef> auxMap : getHashMapStatic()) {
            for (String mapKey : auxMap.keySet()) {
                if (mapKey.equals(nextKey)) {
                    return auxMap.get(mapKey).toString();
                }
            }
        }
        return "";
    }

    /*
     * Returns the generated dictionary chain from a key
     * The Jimple code is divided in others units
     * Example:
     * Class object = new Class();
     * object.b.a.a = object.b.b.a + 3;
     * Jimple:

     * $stack2 = new Class;
     * object = $stack2;

     * $stack3 = object.b;
     * $stack8 = $stack3.a;
     * $stack7 = $stack6 + 3;

     * $stack8.a = $stack7; //$stack8.a is the nextKey of the call object.b.a.a

     * The result is:
     * $stack8.a -> $stack8 = $stack3.a; -> $stack3 = object.b; -> object.b; (STOP) -> Return object chain
     * InitialKey($stack8.a) -> nextKey ($stack8) -> nextKey ($stack3) -> object.b has no nextKey (STOP) -> Return object chain -> Return: object.b.a.a
     */
    private String getFieldsChain(String nextKey){
        List<HashMap<String, InstanceFieldRef>> auxValuesHashMap = new ArrayList<>();
        auxValuesHashMap.addAll(getHashMapJInstanceField());

        //If nextKey not contain $stack is because simple key
        if (!(nextKey.contains("$stack"))){
            return nextKey;
        }

        InstanceFieldRef currentField = null;

        //The second position is the field called
        String currentUniqueKey = "<"+nextKey.split(".<")[1];

        //The first key comes before ".<" if you have a stack as a substring
        nextKey = nextKey.split(".<")[0];
        boolean isNextKey = true;

        while (isNextKey) {
            isNextKey = false;

            Iterator iterator = auxValuesHashMap.iterator();
            while (iterator.hasNext() && !isNextKey){
                HashMap<String, InstanceFieldRef> auxMap = (HashMap<String, InstanceFieldRef>) iterator.next();
                for (String mapKey : auxMap.keySet()) {
                    if (mapKey.equals(nextKey)) {
                        currentField = auxMap.get(mapKey);
                        isNextKey = true;
                    }
                }
            }

            if (!isNextKey) {
                currentUniqueKey = nextKey + (currentUniqueKey.equals("") ? "" : ".") + currentUniqueKey;
            } else{
                currentUniqueKey = currentField.getFieldRef().toString() + (currentUniqueKey.equals("") ? "" : ".") + currentUniqueKey;
                nextKey = currentField.getBase().toString(); //Update the nextKey and repeat until the condition
            }
        }
        //If auxValuesHasMap is equal to zero, there is a next key and currentField is not null, then it is the starting field of the object
        if (!(currentField==null) && (isNextKey)){
            currentUniqueKey = nextKey + (currentUniqueKey.equals("") ? "" : ".") + currentUniqueKey;
        }

        return currentUniqueKey;
    }


    /*
     * Generates the dictionary with the UseBoxes instances of InstanceFieldRef or StaticFieldRef
     */
    public void generateFieldDictionary(Unit u){
        for (ValueBox valueBox: u.getUseBoxes()) {
            if (valueBox.getValue() instanceof InstanceFieldRef) {
                generateClassFieldDictionary(getStatementAssociatedWithUnit(u));
            } else if (valueBox.getValue() instanceof StaticFieldRef) {
                generateStaticRefDictionary(u, valueBox);
            }
        }
    }

    /*
     * Generates a dictionary to StaticFieldRef
     */
    private void generateStaticRefDictionary(Unit u, ValueBox valueBox){
        StringBuilder strKey = new StringBuilder();
        for (ValueBox c: u.getDefBoxes()){
            strKey.append(c.getValue().toString());
        }
        HashMap<String, StaticFieldRef> auxHashMap = new HashMap<>();
        auxHashMap.put(strKey.toString(), (StaticFieldRef) valueBox.getValue());
        Collector.instance().addHashStaticField(auxHashMap);
    }

    /*
     * Generates a dictionary to JInstanceFieldRef
     */
    private void generateClassFieldDictionary(Statement stmt){
        HashMap<String, InstanceFieldRef> auxHashMap = new HashMap<>();
        StringBuilder strKey = new StringBuilder();
        for (ValueBox valueBoxKey : stmt.getUnit().getDefBoxes()) {
            strKey.append(valueBoxKey.getValue().toString());
        }
        InstanceFieldRef currentFieldRef = null;
        for (ValueBox catchRef : stmt.getUnit().getUseBoxes()) {
            if (catchRef.getValue() instanceof InstanceFieldRef) {
                currentFieldRef = (InstanceFieldRef) catchRef.getValue();
                auxHashMap.put(strKey.toString(), currentFieldRef);
            }
        }
        if (auxHashMap.size() != 0) {
            Collector.instance().addHashInstanceField(auxHashMap);
        }
    }
}