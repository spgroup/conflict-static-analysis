package br.unb.cic.analysis.oa;

import br.unb.cic.analysis.AbstractAnalysis;
import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.SootWrapper;
import br.unb.cic.analysis.StatementsUtil;
import br.unb.cic.analysis.model.*;
import scala.collection.JavaConverters;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.Chain;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public abstract class OverrideAssignment extends SceneTransformer implements AbstractAnalysis {
    private String classpath;
    private final Boolean interprocedural;
    private static int fallbackCounter;
    private static int pointToCounter;
    private static final List<ComparisonAssignment> fallbackComparisons = new ArrayList<>();
    private static final List<ComparisonAssignment> pointToComparisons = new ArrayList<>();
    protected List<Statement> pointerAnalysisMissingRefs;
    private int depthLimit;
    private OAConflictReport oaConflictReport;
    private TraversedMethodsWrapper<SootMethod> traversedMethodsWrapper;
    private List<TraversedLine> stacktraceList;
    private StatementsUtil statementsUtils;

    public OverrideAssignment(AbstractMergeConflictDefinition definition, int depthLimit, Boolean interprocedural,
            List<String> entrypoints, String classpath) {
        this.depthLimit = depthLimit;
        this.interprocedural = interprocedural;
        this.statementsUtils = new StatementsUtil(definition, entrypoints);
        this.pointerAnalysisMissingRefs = new ArrayList<>();
        this.classpath = classpath;

        initDefaultFields();
    }

    public OverrideAssignment(AbstractMergeConflictDefinition definition, int depthLimit, Boolean interprocedural,
            List<String> entrypoints) {
        this(definition, depthLimit, interprocedural, entrypoints, "ScenarioJAR not defined");
    }

    public OverrideAssignment(AbstractMergeConflictDefinition definition, int depthLimit, Boolean interprocedural) {
        this(definition, depthLimit, interprocedural, new ArrayList<>());
    }

    public OverrideAssignment(AbstractMergeConflictDefinition definition) {
        this(definition, 5, true);
    }

    /**
     * Compara duas referências de campo em diferentes instruções para verificar se
     * elas representam o mesmo campo,
     * desconsiderando casos onde ambas estão associadas ao mesmo construtor.
     * <p>
     * O método verifica se:
     * <ul>
     * <li>As referências de campo ({@code valueInAbs} e {@code valueInFlow})
     * apontam para o mesmo campo real.</li>
     * <li>Os tipos dos objetos base das referências são iguais.</li>
     * <li>As instruções não pertencem ao mesmo construtor (verificado por
     * {@code isBothAreSameConstructor}).</li>
     * </ul>
     * Retorna {@code true} apenas se os dois primeiros critérios forem verdadeiros
     * e o terceiro for falso.
     * </p>
     *
     * @param stmtInAbs   a primeira instrução a ser comparada.
     * @param stmtInFlow  a segunda instrução a ser comparada.
     * @param valueInAbs  o valor associado à primeira instrução, esperado como
     *                    {@code InstanceFieldRef}.
     * @param valueInFlow o valor associado à segunda instrução, esperado como
     *                    {@code InstanceFieldRef}.
     * @return {@code true} se as referências de campo forem semanticamente
     *         equivalentes e não forem parte do mesmo construtor; caso contrário,
     *         {@code false}.
     */
    static boolean areFieldReferencesEqual(Statement stmtInAbs, Statement stmtInFlow, Value valueInAbs,
            Value valueInFlow) {
        InstanceFieldRef abstractFieldRef = (InstanceFieldRef) valueInAbs;
        InstanceFieldRef flowFieldRef = (InstanceFieldRef) valueInFlow;

        boolean isSameOrSubtype = isSameOrSubtype(valueInAbs, valueInFlow);
        boolean isSameFieldReference = abstractFieldRef.getFieldRef().equals(flowFieldRef.getFieldRef());
        boolean bothAreSameConstructor = isBothAreSameConstructor(stmtInAbs, stmtInFlow);

        registerFallbackComparison(valueInAbs, valueInFlow);
        return isSameOrSubtype && isSameFieldReference && !bothAreSameConstructor;
    }

    /**
     * Compara duas referências de array em diferentes instruções para verificar se
     * elas apontam para o mesmo elemento de array,
     * considerando o índice e o tipo base, e excluindo casos onde ambas as
     * referências estão no mesmo construtor
     * ou são variáveis locais.
     * <p>
     * O método verifica se:
     * <ul>
     * <li>Os índices das referências de array ({@code valueInAbs} e
     * {@code valueInFlow}) são iguais.</li>
     * <li>Os tipos base dos arrays são iguais ou são subtipos.</li>
     * <li>As instruções não pertencem ao mesmo construtor (verificado por
     * {@code isBothAreSameConstructor}).</li>
     * <li>As instruções não são originadas de variáveis locais em ambos os
     * casos.</li>
     * </ul>
     * Retorna {@code true} somente se todas essas condições forem atendidas.
     * </p>
     *
     * @param stmtInAbs   a primeira instrução a ser comparada.
     * @param stmtInFlow  a segunda instrução a ser comparada.
     * @param valueInAbs  o valor associado à primeira instrução, esperado como
     *                    {@code ArrayRef}.
     * @param valueInFlow o valor associado à segunda instrução, esperado como
     *                    {@code ArrayRef}.
     * @return {@code true} se as referências de array forem semanticamente
     *         equivalentes e não forem parte do mesmo construtor nem ambas
     *         variáveis locais; caso contrário, {@code false}.
     */
    static boolean areArrayReferencesEqual(Statement stmtInAbs, Statement stmtInFlow, Value valueInAbs,
            Value valueInFlow) {
        ArrayRef abstractArrayRef = (ArrayRef) valueInAbs;
        ArrayRef flowArrayRef = (ArrayRef) valueInFlow;

        boolean isSameOrSubtype = isSameOrSubtype(abstractArrayRef, flowArrayRef);
        boolean isSameIndexReference = abstractArrayRef.getIndex().equals(flowArrayRef.getIndex());
        boolean bothAreSameConstructor = isBothAreSameConstructor(stmtInAbs, stmtInFlow);

        return isSameOrSubtype && isSameIndexReference && !bothAreSameConstructor;
    }

    public static boolean isSameOrSubtype(Value valueInAbs, Value valueInFlow) {
        Type typeInAbs = extractType(valueInAbs);
        Type typeInFlow = extractType(valueInFlow);

        // Verificação direta
        if (typeInAbs.equals(typeInFlow)) {
            return true;
        }

        FastHierarchy hierarchy = Scene.v().getOrMakeFastHierarchy();

        // Se ambos são RefType (ex: objetos)
        if (typeInAbs instanceof RefType && typeInFlow instanceof RefType) {
            return hierarchy.canStoreType(typeInAbs, typeInFlow)
                    || hierarchy.canStoreType(typeInFlow, typeInAbs);
        }

        // Se ambos são ArrayType (ex: String[], Object[], etc.)
        if (typeInAbs instanceof ArrayType && typeInFlow instanceof ArrayType) {
            Type baseA = ((ArrayType) typeInAbs).baseType;
            Type baseB = ((ArrayType) typeInFlow).baseType;

            // Se ambos são arrays de tipos referenciais
            if (baseA instanceof RefType && baseB instanceof RefType) {
                return hierarchy.canStoreType(baseA, baseB)
                        || hierarchy.canStoreType(baseB, baseA);
            }

            // Arrays com tipos primitivos devem ser exatamente iguais
            return baseA.equals(baseB);
        }

        return false; // Tipos incompatíveis
    }

    private static Type extractType(Value val) {
        if (val instanceof ArrayRef) {
            return ((ArrayRef) val).getBase().getType();
        }
        if (val instanceof FieldRef) {
            return ((FieldRef) val).getField().getType();
        }
        return null;
    }

    protected static void getPointToFromBase(Value value, Statement stmt) {
        PointsToAnalysis pointsToAnalysis = Scene.v().getPointsToAnalysis();
        PointsToSet points = pointsToAnalysis.reachingObjects((Local) value);
        stmt.setPointsTo(points);
    }

    protected static void getPointToFromStaticField(SootField fieldRef, Statement stmt) {
        PointsToAnalysis pointsToAnalysis = Scene.v().getPointsToAnalysis();
        PointsToSet points = pointsToAnalysis.reachingObjects(fieldRef);
        stmt.setPointsTo(points);
    }

    private static boolean isBothAreSameConstructor(Statement stmtInAbs, Statement stmtInFlow) {
        boolean bothAreSameConstructor = stmtInAbs.getSootMethod().isConstructor()
                && stmtInFlow.getSootMethod().isConstructor()
                && stmtInAbs.getSootMethod().equals(stmtInFlow.getSootMethod());
        return bothAreSameConstructor;
    }

    public void printCallGraph(CallGraph cg) {
        if (cg == null) {
            cg = Scene.v().getCallGraph();
        }
        List<String> graphEdges = new ArrayList<>();

        // Percorre todos os nós do Call Graph e imprime em formato DOT
        for (Edge edge : cg) {
            SootMethod source = edge.src();
            SootMethod target = edge.tgt();
            graphEdges.add("    \"" + source.getSignature() + "\" -> \"" + target.getSignature() + "\";");
        }
        exportCallGraphToDot(graphEdges, "callgraph.dot");
    }

    protected abstract void gen(OverrideAssignmentAbstraction in, Statement stmt);

    protected abstract boolean isSameStateElement(Statement stmtInAbs, Statement stmtInFlow);

    private void initDefaultFields() {
        this.oaConflictReport = new OAConflictReport();
        this.traversedMethodsWrapper = new TraversedMethodsWrapper<>();
        this.stacktraceList = new ArrayList<>();
        fallbackCounter = 0;
        pointToCounter = 0;
        fallbackComparisons.clear();
        pointToComparisons.clear();
    }

    @Override
    public void clear() {
        oaConflictReport.clear();
    }

    @Override
    public Set<Conflict> getConflicts() {
        return oaConflictReport.getConflicts();
    }

    public Set<Conflict> getFilteredConflicts() {
        return oaConflictReport.filterConflictsWithSameRoot(oaConflictReport.getConflicts());
    }

    @Override
    protected void internalTransform(String s, Map<String, String> map) {
        CallGraph callGraph = Scene.v().getCallGraph();
        int countEdges = SootWrapper.countEdges(callGraph);
        System.out.println("countEdges: " + countEdges);
        // printCallGraph(callGraph);
        // List<SootMethod> methods = Scene.v().getEntryPoints();
        scala.collection.immutable.List<SootMethod> scalaList = this.statementsUtils.getEntryPoints();
        List<SootMethod> methods = new ArrayList<>(JavaConverters.seqAsJavaList(scalaList));
        // System.out.println("Methods to analyze: " + methods);
        methods.forEach(
                sootMethod -> traverse(new OverrideAssignmentAbstraction(), sootMethod, Statement.Type.IN_BETWEEN));

        oaConflictReport.report();
        printComparisonSummary();

        createAnalysisReportLog(countEdges, methods);
    }

    private static void registerFallbackComparison(Value valueInAbs, Value valueInFlow) {
        fallbackCounter += 1;
        fallbackComparisons.add(new ComparisonAssignment(valueInAbs, valueInFlow));
    }

    private static void registerPointsToComparison(Value valueInAbs, Value valueInFlow) {
        pointToCounter += 1;
        pointToComparisons.add(new ComparisonAssignment(valueInAbs, valueInFlow));
    }

    private static void printComparisonSummary() {
        printComparisonDetails("fallbacks due to missing points-to information. ", fallbackCounter,
                fallbackComparisons);
        printComparisonDetails("comparisons made using points-to information: ", pointToCounter, pointToComparisons);
    }

    private static void printComparisonDetails(String label, int counter, List<ComparisonAssignment> comparisons) {
        System.out.println(label + counter);
        if (comparisons.isEmpty()) {
            System.out.println("assignments: []");
            return;
        }

        System.out.println("assignments:");
        int limit = Math.min(3, comparisons.size());

        for (int i = 0; i < limit; i++) {
            // System.out.println(" [" + (i + 1) + "] " + comparisons.get(i));
        }

        // Se houver mais itens além dos exibidos
        if (comparisons.size() > limit) {
            // System.out.println(" ...");
        }
    }

    private void createAnalysisReportLog(int countEdges, List<SootMethod> methods) {
        new AnalysisRecord.Builder()
                .callGraphAlgorithm(SootWrapper.getCallGraphAlgorithm())
                .callGraphEdgeCount(countEdges)
                .depthLimit(this.depthLimit)
                .visitedMethodsCount(getVisitedMethodsCount())
                .callGraphBuildTimeMs(SootWrapper.getPackageExecutionTimes())
                .callGraphEntryPoint(Scene.v().getEntryPoints()) // se quiser só 1 método
                .analysisEntryPoint(methods)
                .build();
    }

    public void configureEntryPoints() {
        scala.collection.immutable.List<SootMethod> scalaList = this.statementsUtils.getCallgraphEntryPoints();
        List<SootMethod> entryPoints = new ArrayList<>(JavaConverters.seqAsJavaList(scalaList));
        // List<SootMethod> methods = new
        // ArrayList<>(Collections.singleton(entryPoints.get(1).getDeclaringClass().getMethodByName("main")));
        // System.out.println("CG Entry points: " + entryPoints);
        Scene.v().setEntryPoints(entryPoints);
    }

    /**
     * This method captures the safe body of the current method and delegates the
     * analysis function to units of (LEFT or RIGHT) or units of BASE.
     *
     * @param sootMethod    Current method to be traversed;
     * @param flowChangeTag This parameter identifies whether the unit under
     *                      analysis is in the flow of any statement already marked.
     *                      Initially it receives the value IN_BETWEEN but changes
     *                      if the call to the current method (sootMethod) has been
     *                      marked as SOURCE or SINK.
     *                      The remaining statements of the current method that have
     *                      no markup will be marked according to the flowChangeTag.
     * @return the result of applying the analysis considering the income
     *         abstraction (in) and the sootMethod
     */
    private OverrideAssignmentAbstraction traverse(OverrideAssignmentAbstraction in, SootMethod sootMethod,
            Statement.Type flowChangeTag) {

        if (shouldSkip(sootMethod)) {
            return in;
        }

        this.traversedMethodsWrapper.add(sootMethod);

        // System.out.println(sootMethod + " - " + this.traversedMethodsWrapper.size());
        Body body = this.statementsUtils.getDefinition().retrieveActiveBodySafely(sootMethod);

        if (body != null) {
            for (Unit unit : body.getUnits()) {
                TraversedLine traversedLine = new TraversedLine(sootMethod, unit.getJavaSourceStartLineNumber());
                Statement stmt = getStatementAssociatedWithUnit(sootMethod, unit, flowChangeTag);

                this.stacktraceList.add(traversedLine);
                in = runAnalysis(in, stmt);
                this.stacktraceList.remove(traversedLine);
            }
        }

        this.traversedMethodsWrapper.remove(sootMethod);
        return in;
    }

    private boolean shouldSkip(SootMethod sootMethod) {
        boolean hasRelativeBeenTraversed = this.traversedMethodsWrapper.hasRelativeBeenTraversed(sootMethod);
        boolean isSizeGreaterThanDepthLimit = this.traversedMethodsWrapper.size() >= this.depthLimit;
        boolean isPhantom = sootMethod.isPhantom();
        boolean isMethodInObjectClass = isMethodDefinedInObject(sootMethod);
        // boolean isJavaLibraryMethod = sootMethod.isJavaLibraryMethod();

        return hasRelativeBeenTraversed || isSizeGreaterThanDepthLimit || isPhantom || isMethodInObjectClass;
    }

    private boolean isMethodDefinedInObject(SootMethod sootMethod) {
        String methodName = sootMethod.getName();
        return (methodName.equals("toString") ||
                methodName.equals("hashCode") ||
                methodName.equals("equals") ||
                methodName.equals("getClass") ||
                methodName.equals("notify") ||
                methodName.equals("notifyAll") ||
                methodName.equals("wait"));
    }

    private boolean isTagged(Statement.Type flowChangeTag, Unit unit) {
        return (isLeftUnit(unit) || isRightUnit(unit))
                || (isInLeftStatementFlow(flowChangeTag)
                        || isInRightStatementFlow(flowChangeTag)
                        || isInLeftAndRightStatementFlow(flowChangeTag));
    }

    private OverrideAssignmentAbstraction runAnalysis(OverrideAssignmentAbstraction in, Statement stmt) {
        /*
         * Are there other possible cases? Yes, see follow links:
         * https://soot-build.cs.uni-paderborn.de/public/origin/develop/soot/soot-
         * develop/jdoc/soot/jimple/Stmt.html
         * https://github.com/PAMunb/JimpleFramework/blob/
         * d585caefa8d5f967bfdbeb877346e0ff316e0b5e/src/main/rascal/lang/jimple/core/
         * Syntax.rsc#L77-L95
         */

        if (stmt.getUnit() instanceof AssignStmt) {
            /*
             * Does AssignStmt check contain objects, arrays or other types?
             * Yes, AssignStmt handles assignments, and they can be of any type as long as
             * they follow the structure: variable = value
             */
            AssignStmt assignStmt = (AssignStmt) stmt.getUnit();

            if (this.interprocedural && assignStmt.containsInvokeExpr()) {
                return calculateMergedOverrideAssignment(in, stmt);
            }

            if (isTagged(stmt.getType(), stmt.getUnit())) {
                in = runAnalysisWithTaggedUnit(in, stmt);
            } else {
                in = runAnalysisWithBaseUnit(in, stmt);
            }

            /*
             * Check case: x = foo() + bar()
             * In this case, this condition will be executed for the call to the foo()
             * method and then another call to the bar() method.
             */

            /*
             * Check treatment in case 'for'
             * - Jimple does not exist for. The command is done using the goto.
             * - The variables of the force are marked as IN_BETWEEN, so they do not enter
             * the abstraction.
             * - The goto instructions have the following format "if i0> = 1 goto label2;"
             * in this case,
             * they are treated as "IfStmt" and do not enter either the
             * "if(unit instanceof AssignStmt)" nor the
             * "else if(unit instanceof InvokeStmt)".
             */

            /*
             * InvokeStmt involves builder?
             * Yes. InvokeStmt also involves builders. What changes is the corresponding
             * InvokeExpression.
             * For builders, InvokeExpression is an instance of InvokeSpecial
             */

        } else if (this.interprocedural && stmt.getUnit() instanceof InvokeStmt) {
            SootMethod sm = ((InvokeStmt) stmt.getUnit()).getInvokeExpr().getMethod();
            if (sm.isConstructor()) {
                handleConstructor(in, sm, stmt.getType());
            }
            return calculateMergedOverrideAssignment(in, stmt);
        }

        return in;
    }

    private OverrideAssignmentAbstraction runAnalysisWithTaggedUnit(OverrideAssignmentAbstraction in, Statement stmt) {
        stmt.setTraversedLine(new ArrayList<>(this.stacktraceList));
        gen(in, stmt);
        checkConflict(in, stmt);
        return in;
    }

    private OverrideAssignmentAbstraction runAnalysisWithBaseUnit(OverrideAssignmentAbstraction in, Statement stmt) {
        List<Statement> statementsToRemove = new ArrayList<>();
        List<List<Statement>> abstractions = in.getLists();

        abstractions.forEach(abstraction -> {
            addToRemovalListAndFindConflicts(abstraction, stmt, statementsToRemove, false);
            removeAll(in, statementsToRemove);
        });

        return in;
    }

    public void checkConflict(OverrideAssignmentAbstraction in, Statement stmt) {
        List<Statement> statementsToRemove = new ArrayList<>();
        if (stmt.isLefAndRightStatement()) {
            addConflict(stmt, stmt);
        } else if (stmt.isLeftStatement()) {
            addToRemovalListAndFindConflicts(in.getRightAbstraction(), stmt, statementsToRemove, true);
        } else if (stmt.isRightStatement()) {
            addToRemovalListAndFindConflicts(in.getLeftAbstraction(), stmt, statementsToRemove, true);
        }
        removeAll(in, statementsToRemove);
    }

    private void addToRemovalListAndFindConflicts(List<Statement> abstraction, Statement stmt,
            List<Statement> statementsToRemove, boolean addConflict) {
        abstraction.forEach(statement -> {
            if (isSameStateElement(statement, stmt)) {
                if (addConflict) {
                    addConflict(statement, stmt);
                }
                // Adiciona o statement principal
                statementsToRemove.add(statement);

                // Adiciona todos os statements com a mesma linha de código
                int line = statement.getSourceCodeLineNumber();
                abstraction.forEach(other -> {
                    if (!statementsToRemove.contains(other) && other.getSourceCodeLineNumber() == line) {
                        statementsToRemove.add(other);
                    }
                });
            }
        });
    }

    private void removeAll(OverrideAssignmentAbstraction in, List<Statement> statementsToRemove) {
        statementsToRemove.forEach(statement -> kill(in, statement));
    }

    protected boolean isSameLocal(Statement stmtInAbs, Statement stmtInFlow, Value valueInAbs, Value valueInFlow) {
        // Se as variaveis são locais, devem ser do mesmo metodo, se forem de metodos
        // diferentes, não há interferencia.
        if (!stmtInAbs.getSootMethod().equals(stmtInFlow.getSootMethod())) {
            return false;
        }
        if (isLikelyRedundantLocalAssignment(stmtInAbs, stmtInFlow)) {
            return false;
        }
        String normalizedValueInAbs = normalizeValue(valueInAbs);
        String normalizedValueInFlow = normalizeValue(valueInFlow);

        boolean isSameValue = normalizedValueInAbs.equals(normalizedValueInFlow);
        boolean hasCommonTargets = stmtInAbs.getPointsTo().hasNonEmptyIntersection(stmtInFlow.getPointsTo());

        return isSameValue || hasCommonTargets;
    }

    /**
     * Verifica se uma possível interferência entre duas atribuições é, na verdade,
     * um falso positivo.
     * <p>
     * Essa situação ocorre quando ambas as atribuições são locais (isto é,
     * atribuídas dentro do mesmo fluxo de execução)
     * e apontam para a mesma linha de código. Um exemplo típico ocorre durante
     * alterações simultâneas nos dois lados do diff:
     *
     * <pre>
     *     foo(); // left
     *     ...
     *     foo(); // right
     *
     *     void foo() {
     *         x = 1; // atribuição local
     *     }
     * </pre>
     * <p>
     * Nesse caso, como ambas as chamadas percorrem mais de uma linha (indicando
     * execução de métodos),
     * e a linha final da atribuição é a mesma, é provável que se trate de um falso
     * positivo.
     *
     * @param stmtInAbs  a instrução do mapeamento abstrato (lado esquerdo ou
     *                   original)
     * @param stmtInFlow a instrução dentro do fluxo alterado (lado direito ou
     *                   modificado)
     * @return {@code false} se for detectado como falso positivo; {@code true} caso
     *         contrário
     */
    public boolean isLikelyRedundantLocalAssignment(Statement stmtInAbs, Statement stmtInFlow) {
        boolean bothTraverseMultipleLines = stmtInAbs.getTraversedLine().size() > 1
                && stmtInFlow.getTraversedLine().size() > 1;

        boolean sameTargetLine = stmtInAbs.getSourceCodeLineNumber()
                .equals(stmtInFlow.getSourceCodeLineNumber());

        return bothTraverseMultipleLines && sameTargetLine;
    }

    /**
     * Normaliza o valor fornecido extraindo apenas o nome simples do identificador.
     * <p>
     * Este método converte o objeto {@code Value} em uma string e remove qualquer
     * conteúdo após o caractere '#' (inclusive), mantendo apenas a parte antes
     * dele.
     * Isso é útil para extrair o nome curto de URIs ou identificadores compostos.
     * </p>
     *
     * @param value o objeto {@code Value} a ser normalizado.
     * @return uma string contendo apenas a parte anterior ao caractere '#' na
     *         representação do valor.
     */
    private String normalizeValue(Value value) {
        return value.toString().replaceAll("^(\\w+)#.*$", "$1");
    }

    /**
     * Verifica se duas referências de campo, associadas a instruções diferentes,
     * referenciam o mesmo campo de instância.
     * <p>
     * O método realiza uma comparação que considera os "points-to" das instruções
     * para
     * determinar se as referências são equivalentes no contexto da análise de
     * fluxo.
     * <ul>
     * <li>Se a primeira instrução ({@code stmtInAbs}) possui "points-to" e a
     * segunda ({@code stmtInFlow}) não,
     * tenta obter os "points-to" da base da referência de fluxo.</li>
     * <li>Se qualquer instrução não possuir "points-to", é feita uma comparação
     * básica (sem usar point-to) e a instrução é adicionada a um contador
     * {@code pointerAnalysisMissingRefs} para fins de debug.</li>
     * <li>Se ambas possuem "points-to", verifica se possuem interseção não vazia,
     * se os campos são iguais e se não pertencem ao mesmo construtor.</li>
     * </ul>
     * </p>
     *
     * @param stmtInAbs   a primeira instrução a ser comparada.
     * @param stmtInFlow  a segunda instrução a ser comparada.
     * @param valueInAbs  o valor associado à primeira instrução, esperado como
     *                    {@code InstanceFieldRef}.
     * @param valueInFlow o valor associado à segunda instrução, esperado como
     *                    {@code InstanceFieldRef}.
     * @return {@code true} se as referências de campo forem consideradas
     *         equivalentes conforme as condições acima; {@code false} caso
     *         contrário.
     */

    protected boolean isSameFieldRef(Statement stmtInAbs, Statement stmtInFlow, Value valueInAbs, Value valueInFlow) {
        InstanceFieldRef abstractFieldRef = (InstanceFieldRef) valueInAbs;
        InstanceFieldRef flowFieldRef = (InstanceFieldRef) valueInFlow;

        // Garante que stmtInFlow tenha points-to se stmtInAbs já tiver
        if (hasPointsTo(stmtInAbs) && !hasPointsTo(stmtInFlow)) {
            getPointToFromBase(flowFieldRef.getBase(), stmtInFlow);
        }

        // Se qualquer um dos dois não tem points-to, usa comparação básica e adiciona
        // ao pointerAnalysisMissingRefs
        if (!hasPointsTo(stmtInFlow)) {
            pointerAnalysisMissingRefs.add(stmtInFlow);
            return areFieldReferencesEqual(stmtInAbs, stmtInFlow, abstractFieldRef, flowFieldRef);
        }

        if (!hasPointsTo(stmtInAbs)) {
            pointerAnalysisMissingRefs.add(stmtInAbs);
            return areFieldReferencesEqual(stmtInAbs, stmtInFlow, abstractFieldRef, flowFieldRef);
        }

        // Comparação completa
        boolean bothAreSameConstructor = isBothAreSameConstructor(stmtInAbs, stmtInFlow);
        boolean isSameFieldReference = abstractFieldRef.getField().equals(flowFieldRef.getField());
        boolean hasCommonTargets = stmtInAbs.getPointsTo().hasNonEmptyIntersection(stmtInFlow.getPointsTo());

        registerPointsToComparison(valueInAbs, valueInFlow);
        return hasCommonTargets && isSameFieldReference && !bothAreSameConstructor;
    }

    private boolean hasPointsTo(Statement stmt) {
        return stmt.getPointsTo() != null && !stmt.getPointsTo().isEmpty();
    }

    /**
     * Verifica se duas referências de array, associadas a instruções diferentes,
     * referenciam o mesmo elemento de array.
     * <p>
     * O método considera os "points-to" das instruções para determinar se as
     * referências são equivalentes.
     * <ul>
     * <li>Se a primeira instrução ({@code stmtInAbs}) possui "points-to" e a
     * segunda ({@code stmtInFlow}) não,
     * tenta obter os "points-to" da base da referência de array do fluxo.</li>
     * <li>Se qualquer uma das instruções não possuir "points-to", realiza uma
     * comparação básica(sem usar point-to) e adiciona a instrução a um contador
     * {@code pointerAnalysisMissingRefs} para fins de debug.</li>
     * <li>Se ambas possuem "points-to", verifica se há interseção não vazia entre
     * os alvos, se os índices são iguais e se não pertencem ao mesmo
     * construtor.</li>
     * </ul>
     * </p>
     *
     * @param stmtInAbs   a primeira instrução a ser comparada.
     * @param stmtInFlow  a segunda instrução a ser comparada.
     * @param valueInAbs  o valor associado à primeira instrução, esperado como
     *                    {@code ArrayRef}.
     * @param valueInFlow o valor associado à segunda instrução, esperado como
     *                    {@code ArrayRef}.
     * @return {@code true} se as referências de array forem consideradas
     *         equivalentes conforme as condições acima; {@code false} caso
     *         contrário.
     */
    protected boolean isSameArrayRef(Statement stmtInAbs, Statement stmtInFlow, Value valueInAbs, Value valueInFlow) {
        ArrayRef abstractArrayRef = (ArrayRef) valueInAbs;
        ArrayRef flowArrayRef = (ArrayRef) valueInFlow;

        // Garante que stmtInFlow tenha points-to se stmtInAbs já tiver
        if (hasPointsTo(stmtInAbs) && !hasPointsTo(stmtInFlow)) {
            getPointToFromBase(flowArrayRef.getBase(), stmtInFlow);
        }

        // Se qualquer um dos dois não tem points-to, usa comparação básica e adiciona
        // ao pointerAnalysisMissingRefs
        if (!hasPointsTo(stmtInFlow)) {
            pointerAnalysisMissingRefs.add(stmtInFlow);
            return areArrayReferencesEqual(stmtInAbs, stmtInFlow, abstractArrayRef, flowArrayRef);
        }

        if (!hasPointsTo(stmtInAbs)) {
            pointerAnalysisMissingRefs.add(stmtInAbs);
            return areArrayReferencesEqual(stmtInAbs, stmtInFlow, abstractArrayRef, flowArrayRef);
        }

        boolean bothAreSameConstructor = isBothAreSameConstructor(stmtInAbs, stmtInFlow);
        boolean isSameIndexReference = abstractArrayRef.getIndex().equals(flowArrayRef.getIndex());
        boolean hasCommonTargets = stmtInAbs.getPointsTo().hasNonEmptyIntersection(stmtInFlow.getPointsTo());

        // Comparação completa
        return hasCommonTargets && isSameIndexReference && !bothAreSameConstructor;
    }

    protected boolean areArrayAndLocalCompatible(Statement stmtInAbs, Statement stmtInFlow, Value valueInAbs,
            Value valueInFlow) {
        if (!stmtInAbs.getSootMethod().equals(stmtInFlow.getSootMethod())) {
            return false;
        }

        // Garante que stmtInFlow tenha points-to se stmtInAbs já tiver
        if (hasPointsTo(stmtInAbs) && !hasPointsTo(stmtInFlow)) {
            getPointToFromBase(valueInFlow, stmtInFlow);
        }

        // Se nenhum dos dois tem points-to, só compara por nome
        if (!hasPointsTo(stmtInAbs) || !hasPointsTo(stmtInFlow)) {
            pointerAnalysisMissingRefs.add(stmtInFlow);
            return valueInAbs.toString().contains(valueInFlow.toString());
        }
        boolean isPointToIntersection = stmtInAbs.getPointsTo().hasNonEmptyIntersection(stmtInFlow.getPointsTo());
        boolean containsSameName = valueInAbs.toString().contains(valueInFlow.toString());

        return isPointToIntersection && containsSameName;
    }

    protected boolean isLocalAndArrayCompatible(Statement stmtInAbs, Statement stmtInFlow, Local valueInAbs,
            ArrayRef valueInFlow) {
        return areArrayAndLocalCompatible(stmtInFlow, stmtInAbs, valueInFlow, valueInAbs);
    }

    /**
     * Verifica se duas referências a campos estáticos representam o mesmo campo.
     * O método {@code equals}, aplicado a {@code FieldRef} (implementado por
     * {@code AbstractSootFieldRef}),
     * considera os seguintes critérios de igualdade:
     * <ul>
     * <li><strong>declaringClass</strong> – a classe que declara o campo</li>
     * <li><strong>isStatic</strong> – se o campo é estático</li>
     * <li><strong>name</strong> – o nome do campo</li>
     * <li><strong>type</strong> – o tipo do campo</li>
     * </ul>
     * Se todos esses atributos forem iguais (ou ambos {@code null}, quando
     * aplicável), os {@code FieldRef}
     * são considerados iguais.
     *
     * @param valueInAbs  o primeiro valor a ser comparado
     * @param valueInFlow o segundo valor a ser comparado
     * @return {@code true} se ambos os {@code FieldRef} forem considerados iguais;
     *         {@code false} caso contrário
     */
    protected boolean isSameStaticFieldRef(Value valueInAbs, Value valueInFlow) {
        StaticFieldRef abstractFieldRef = (StaticFieldRef) valueInAbs;
        StaticFieldRef flowFieldRef = (StaticFieldRef) valueInFlow;

        return abstractFieldRef.getFieldRef().equals(flowFieldRef.getFieldRef());
    }

    private void addConflict(Statement left, Statement right) {
        Conflict conflict = new OAConflict(left, right, this.interprocedural, this.classpath);
        if (!this.oaConflictReport.contains(conflict)) {
            this.oaConflictReport.addConflict(conflict);

        }
    }

    private void kill(OverrideAssignmentAbstraction in, Statement stmt) {
        in.remove(stmt);
    }

    private void handleConstructor(OverrideAssignmentAbstraction in, SootMethod sm, Statement.Type type) {
        Chain<SootField> sootFieldsInClass = sm.getDeclaringClass().getFields();
        // Chain<SootField> sootFieldsInClass =
        // stmt.getSootMethod().getDeclaringClass().getFields();
        // Attributes declared as final in Java can only have a single assignment, which
        // means that their value cannot be changed after they are defined during their
        // initialization.
        List<SootField> nonFinalFields = filterNonFinalFieldsInClass(sootFieldsInClass);
        nonFinalFields.forEach(sootField -> transformFieldsIntoStatements(in, sm, type, sootField));
    }

    private List<SootField> filterNonFinalFieldsInClass(Chain<SootField> sootFieldsInClass) {
        List<SootField> nonFinalFields = new ArrayList<>();
        for (SootField field : sootFieldsInClass) {
            if (!field.isFinal()) {
                nonFinalFields.add(field);
            }
        }
        return nonFinalFields;
    }

    private void transformFieldsIntoStatements(OverrideAssignmentAbstraction in, SootMethod sm, Statement.Type type,
            SootField sootField) {
        String declaringClassShortName = sootField.getDeclaringClass().getShortName();
        JimpleLocal base = new JimpleLocal(declaringClassShortName, RefType.v(sootField.getDeclaringClass()));
        SootFieldRef fieldRef = Scene.v().makeFieldRef(sootField.getDeclaringClass(), sootField.getName(),
                sootField.getType(), sootField.isStatic());

        Value value = createFieldValueReference(base, fieldRef);
        Unit unit = new JAssignStmt(value, NullConstant.v());

        Statement stmt = getStatementAssociatedWithUnit(sm, unit, type);
        if (isTagged(stmt.getType(), stmt.getUnit())) {
            runAnalysisWithTaggedUnit(in, stmt);
        } else {
            runAnalysisWithBaseUnit(in, stmt);
        }
    }

    private OverrideAssignmentAbstraction calculateMergedOverrideAssignment(
            OverrideAssignmentAbstraction inputAbstraction, Statement currentStatement) {
        List<OverrideAssignmentAbstraction> flowSetList = new ArrayList<>();
        List<String> graphEdges = new ArrayList<>();

        CallGraph callGraph = Scene.v().getCallGraph();
        Iterator<Edge> edges = callGraph.edgesOutOf(currentStatement.getUnit());

        if (!edges.hasNext()) {
            handleEdgesNotFound(inputAbstraction, currentStatement, flowSetList);
        } else {
            processEdges(inputAbstraction, currentStatement, edges, flowSetList, graphEdges, callGraph);
        }

        // exportCallGraphToDot(graphEdges, "calculateMergedOverrideAssignment.dot");

        if (flowSetList.isEmpty()) {
            return inputAbstraction;
        }

        OverrideAssignmentAbstraction newOverrideAssignmentAbstraction = new OverrideAssignmentAbstraction();
        flowSetList.forEach(newOverrideAssignmentAbstraction::union);

        return newOverrideAssignmentAbstraction;
    }

    private void handleEdgesNotFound(OverrideAssignmentAbstraction inputAbstraction, Statement currentStatement,
            List<OverrideAssignmentAbstraction> flowSetList) {
        try {
            pointerAnalysisMissingRefs.add(currentStatement);
            SootMethod targetMethod = ((Stmt) currentStatement.getUnit()).getInvokeExpr().getMethod();
            cloneAndTraverse(inputAbstraction, currentStatement, flowSetList, targetMethod);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    private void processEdges(OverrideAssignmentAbstraction inputAbstraction, Statement currentStatement,
            Iterator<Edge> edges, List<OverrideAssignmentAbstraction> flowSetList, List<String> graphEdges,
            CallGraph callGraph) {
        int callGraphEdgesSize = 0;
        while (edges.hasNext()) {
            callGraphEdgesSize++;
            Edge edge = edges.next();
            SootMethod srcMethod = edge.getSrc().method();
            SootMethod targetMethod = edge.getTgt().method();

            graphEdges.add("\"" + srcMethod.getSignature() + "\" -> \"" + targetMethod.getSignature() + "\";");
            try {
                cloneAndTraverse(inputAbstraction, currentStatement, flowSetList, targetMethod);
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void cloneAndTraverse(OverrideAssignmentAbstraction inputAbstraction, Statement currentStatement,
            List<OverrideAssignmentAbstraction> flowSetList, SootMethod targetMethod)
            throws CloneNotSupportedException {
        OverrideAssignmentAbstraction clonedAbstraction = (OverrideAssignmentAbstraction) inputAbstraction.clone();
        OverrideAssignmentAbstraction traverseResult = traverse(clonedAbstraction, targetMethod,
                currentStatement.getType());
        flowSetList.add(traverseResult);
    }

    // Método para exportar o grafo para um arquivo DOT
    private void exportCallGraphToDot(List<String> graphEdges, String filename) {
        try (PrintWriter out = new PrintWriter(filename)) {
            out.println("digraph CallGraph {");
            for (String edge : graphEdges) {
                out.println("    " + edge);
            }
            out.println("}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Value createFieldValueReference(JimpleLocal base, SootFieldRef fieldRef) {
        Value value;
        if (fieldRef.isStatic()) {
            value = Jimple.v().newStaticFieldRef(fieldRef);
        } else {
            value = Jimple.v().newInstanceFieldRef(base, fieldRef);
        }
        return value;
    }

    private Statement getStatementAssociatedWithUnit(SootMethod sootMethod, Unit u, Statement.Type flowChangeTag) {
        if (isLeftAndRightUnit(u) || isInLeftAndRightStatementFlow(flowChangeTag)
                || isBothUnitOrBothStatementFlow(u, flowChangeTag)) {
            return this.statementsUtils.getDefinition().createStatement(sootMethod, u, Statement.Type.SOURCE_SINK);
        } else if (isLeftUnit(u)) {
            return findLeftStatement(u);
        } else if (isRightUnit(u)) {
            return findRightStatement(u);
        } else if (isInLeftStatementFlow(flowChangeTag)) {
            return this.statementsUtils.getDefinition().createStatement(sootMethod, u, flowChangeTag);
        } else if (isInRightStatementFlow(flowChangeTag)) {
            return this.statementsUtils.getDefinition().createStatement(sootMethod, u, flowChangeTag);
        }
        return this.statementsUtils.getDefinition().createStatement(sootMethod, u, Statement.Type.IN_BETWEEN);
    }

    private boolean isBothUnitOrBothStatementFlow(Unit u, Statement.Type flowChangeTag) {
        return (isRightUnit(u) && isInLeftStatementFlow(flowChangeTag))
                || (isLeftUnit(u) && isInRightStatementFlow(flowChangeTag));
    }

    private boolean isLeftUnit(Unit u) {
        return this.statementsUtils.getDefinition().getSourceStatements().stream().map(Statement::getUnit)
                .collect(Collectors.toList()).contains(u);
    }

    private boolean isRightUnit(Unit u) {
        return this.statementsUtils.getDefinition().getSinkStatements().stream().map(Statement::getUnit)
                .collect(Collectors.toList()).contains(u);
    }

    private boolean isInRightStatementFlow(Statement.Type flowChangeTag) {
        return flowChangeTag.equals(Statement.Type.SINK);
    }

    private boolean isInLeftStatementFlow(Statement.Type flowChangeTag) {
        return flowChangeTag.equals(Statement.Type.SOURCE);
    }

    private boolean isInLeftAndRightStatementFlow(Statement.Type flowChangeTag) {
        return flowChangeTag.equals(Statement.Type.SOURCE_SINK);
    }

    private boolean isLeftAndRightUnit(Unit u) {
        return isLeftUnit(u) && isRightUnit(u);
    }

    private Statement findRightStatement(Unit u) {
        return this.statementsUtils.getDefinition().getSinkStatements().stream().filter(s -> s.getUnit().equals(u))
                .findFirst().get();
    }

    private Statement findLeftStatement(Unit u) {
        return this.statementsUtils.getDefinition().getSourceStatements().stream().filter(s -> s.getUnit().equals(u))
                .findFirst().get();
    }

    public int getVisitedMethodsCount() {
        return this.traversedMethodsWrapper.getVisitedMethodsCount();
    }

    public int getDepthLimit() {
        return this.depthLimit;
    }

    public void setDepthLimit(int depthLimit) {
        this.depthLimit = depthLimit;
    }

    public List<Statement> getPointerAnalysisMissingRefs() {
        return pointerAnalysisMissingRefs;
    }
}
