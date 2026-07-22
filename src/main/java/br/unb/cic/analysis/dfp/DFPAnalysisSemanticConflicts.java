package br.unb.cic.analysis.dfp;

import br.ufpe.cin.soot.analysis.jimple.JDFP;
import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.SootWrapper;
import br.unb.cic.analysis.StatementsUtil;
import br.unb.cic.analysis.model.AnalysisRecord;
import br.unb.cic.soot.graph.*;
import br.unb.cic.soot.svfa.*;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.mutable.ListBuffer;
import soot.Scene;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;

import java.io.File;
import java.util.*;

/**
 * An analysis wrapper around the Sparse value
 * flow analysis implementation.
 */
public class DFPAnalysisSemanticConflicts extends JDFP {

    private String cp;
    private int depthLimit;
    private StatementsUtil statementsUtils;
    private CG callGraph = SPARK$.MODULE$;
    private List<br.unb.cic.analysis.model.Statement> pointerAnalysisMissingRefs = new ArrayList<>();
    private Set<SootMethod> checkedMethods = new HashSet<>();


    /**
     * DFPAnalysis constructor
     *
     * @param classPath   a classpath to the software under analysis
     * @param definition  a definition with the sources and sinks unities
     * @param depthLimit  the depth limit for the analysis
     * @param entrypoints the list of entry points for the analysis
     */
    public DFPAnalysisSemanticConflicts(String classPath, AbstractMergeConflictDefinition definition, int depthLimit,
            List<String> entrypoints) {
        this.cp = classPath;
        this.depthLimit = depthLimit;
        this.statementsUtils = new StatementsUtil(definition, entrypoints);
    }

    public DFPAnalysisSemanticConflicts(String classPath, AbstractMergeConflictDefinition definition) {
        this(classPath, definition, 5, new ArrayList<>());
    }

    public DFPAnalysisSemanticConflicts(String classPath, AbstractMergeConflictDefinition definition, int depthLimit) {
        this(classPath, definition, depthLimit, new ArrayList<>());
    }

    public DFPAnalysisSemanticConflicts(String classPath, AbstractMergeConflictDefinition definition,
            List<String> entrypoints) {
        this(classPath, definition, 5, entrypoints);
    }

    @Override
    public String sootClassPath() {
        // TODO: what is the role of soot classPath here??
        return cp;
    }

    @Override
    public Tuple2<String, Transform> createSceneTransform() {
        return new Tuple2<>("wjtp", new Transform("wjtp.svfa", new soot.SceneTransformer() {
            @Override
            protected void internalTransform(String phaseName, Map<String, String> options) {
                DFPAnalysisSemanticConflicts.this.pointsToAnalysis_$eq(Scene.v().getPointsToAnalysis());
                DFPAnalysisSemanticConflicts.this.initAllocationSites();
                List<SootMethod> methods = JavaConverters.seqAsJavaList(getAnalysisEntryPoints());
                methods.forEach(sootMethod -> traverse(sootMethod, new ListBuffer<>(), false));
                createAnalysisReportLog(SootWrapper.countEdges(Scene.v().getCallGraph()), methods);
            }
        }));
    }

    @Override
    public Tuple2<String, Transform> createSceneTransformDFP() {
        return new Tuple2<>("wjtp", new Transform("wjtp.dfp", new soot.SceneTransformer() {
            @Override
            protected void internalTransform(String phaseName, Map<String, String> options) {
                DFPAnalysisSemanticConflicts.this.pointsToAnalysis_$eq(Scene.v().getPointsToAnalysis());
                DFPAnalysisSemanticConflicts.this.initAllocationSites();
                System.out.println("countEdges: " + SootWrapper.countEdges(Scene.v().getCallGraph()));
                List<SootMethod> methods = JavaConverters.seqAsJavaList(getAnalysisEntryPoints());
                methods.forEach(sootMethod -> traverseDFP(sootMethod, new ListBuffer<>(), false));
                createAnalysisReportLog(SootWrapper.countEdges(Scene.v().getCallGraph()), methods);
            }
        }));
    }

    public void createAnalysisReportLog(int countEdges, List<SootMethod> methods) {
        new AnalysisRecord.Builder()
                .callGraphAlgorithm(SootWrapper.getCallGraphAlgorithm())
                .callGraphEdgeCount(countEdges)
                .depthLimit(this.depthLimit)
                .visitedMethodsCount(getNumberVisitedMethods())
                .callGraphBuildTimeMs(getPackageExecutionTimes())
                .callGraphEntryPoint(Scene.v().getEntryPoints())
                .analysisEntryPoint(methods)
                .build();
    }

    public List<br.unb.cic.analysis.model.Statement> getPointerAnalysisMissingRefs() {
        return pointerAnalysisMissingRefs;
    }

    @Override
    public void traverse(SootMethod method, scala.collection.mutable.ListBuffer<VisitedMethods> methods,
                         boolean force) {
        checkMissingReferences(method);
        super.traverse(method, methods, force);
    }

    @Override
    public void traverseDFP(SootMethod method, scala.collection.mutable.ListBuffer<VisitedMethods> methods,
                            boolean force) {
        checkMissingReferences(method);
        super.traverseDFP(method, methods, force);
    }

    protected void checkMissingReferences(SootMethod sootMethod) {
        if (sootMethod == null || !sootMethod.hasActiveBody() || checkedMethods.contains(sootMethod)) {
            return;
        }
        checkedMethods.add(sootMethod);

        soot.jimple.toolkits.callgraph.CallGraph callGraph = Scene.v().getCallGraph();

        for (Unit unit : sootMethod.getActiveBody().getUnits()) {
            boolean isMissing = false;

            if (unit instanceof soot.jimple.AssignStmt) {
                soot.jimple.AssignStmt assignStmt = (soot.jimple.AssignStmt) unit;
                if (isMissingReference(assignStmt.getLeftOp()) || isMissingReference(assignStmt.getRightOp())) {
                    isMissing = true;
                }
                if (assignStmt.containsInvokeExpr()) {
                    if (!callGraph.edgesOutOf(unit).hasNext()) {
                        isMissing = true;
                    }
                }
            } else if (unit instanceof soot.jimple.InvokeStmt) {
                if (!callGraph.edgesOutOf(unit).hasNext()) {
                    isMissing = true;
                }
            }

            if (isMissing) {
                addMissingReference(sootMethod, unit);
            }
        }
    }

    private boolean isMissingReference(soot.Value value) {
        if (value instanceof soot.jimple.InstanceFieldRef) {
            return !hasPointsTo(((soot.jimple.InstanceFieldRef) value).getBase());
        } else if (value instanceof soot.jimple.ArrayRef) {
            return !hasPointsTo(((soot.jimple.ArrayRef) value).getBase());
        } else if (value instanceof soot.jimple.StaticFieldRef) {
            return !hasPointsTo(value);
        }
        return false;
    }

    private boolean hasPointsTo(soot.Value value) {
        if (value instanceof soot.Local) {
            soot.PointsToSet points = Scene.v().getPointsToAnalysis().reachingObjects((soot.Local) value);
            return points != null && !points.isEmpty();
        } else if (value instanceof soot.jimple.StaticFieldRef) {
            soot.PointsToSet points = Scene.v().getPointsToAnalysis()
                    .reachingObjects(((soot.jimple.StaticFieldRef) value).getField());
            return points != null && !points.isEmpty();
        }
        return true;
    }

    private void addMissingReference(SootMethod method, Unit unit) {
        br.unb.cic.analysis.model.Statement stmt = br.unb.cic.analysis.model.Statement.builder()
                .setClass(method.getDeclaringClass())
                .setMethod(method)
                .setUnit(unit)
                .setType(br.unb.cic.analysis.model.Statement.Type.IN_BETWEEN)
                .setSourceCodeLineNumber(unit.getJavaSourceStartLineNumber())
                .build();

        boolean exists = pointerAnalysisMissingRefs.stream()
                .anyMatch(s -> s.getUnit().equals(unit) && s.getSootMethod().equals(method));
        if (!exists) {
            this.pointerAnalysisMissingRefs.add(stmt);
        }
    }

    @Override
    public scala.collection.immutable.List<String> getIncludeList() {
        return JavaConverters.asScalaBuffer(Arrays.asList("")).toList();
    }

    /**
     * Computes the source-sink paths
     * 
     * @return a set with a list of nodes that together builds a source-sink path.
     */
    public Set<List<StatementNode>> findSourceSinkPaths() {
        Set<List<StatementNode>> paths = new HashSet<>();

        JavaConverters
                .asJavaCollection(svg().findConflictingPaths())
                .forEach(p -> {
                    System.out.println("[CONFLICT_FOUND]");
                    paths.add(new ArrayList(JavaConverters.asJavaCollection(p)));
                });

        return paths;
    }

    @Override
    public boolean interprocedural() {
        return true;
    }

    @Override
    public final scala.collection.immutable.List<String> applicationClassPath() {
        String[] array = cp.split(File.pathSeparator);
        return JavaConverters.asScalaBuffer(Arrays.asList(array)).toList();
    }

    @Override
    public final scala.collection.immutable.List<SootMethod> getEntryPoints() {

        // return this.statementsUtils.getEntryPoints(); // DEVELOP

        this.statementsUtils.getDefinition().loadSourceStatements();
        this.statementsUtils.getDefinition().loadSinkStatements();

        scala.collection.immutable.List<SootMethod> entrypoints = this.statementsUtils.getCallgraphEntryPoints();
        return entrypoints;

    }

    public final scala.collection.immutable.List<SootMethod> getAnalysisEntryPoints() {
        this.statementsUtils.getDefinition().loadSourceStatements();
        this.statementsUtils.getDefinition().loadSinkStatements();

        scala.collection.immutable.List<SootMethod> entrypoints = this.statementsUtils.getEntryPoints();
        return entrypoints;
    }

    @Override
    public final NodeType analyze(Unit unit) {
        if (isSource(unit)) {
            return SourceNode.instance();
        } else if (isSink(unit)) {
            return SinkNode.instance();
        }
        return SimpleNode.instance();
    }

    protected boolean isSource(Unit unit) {
        return this.statementsUtils.getDefinition().isSourceStatement(unit);
    }

    protected boolean isSink(Unit unit) {
        return this.statementsUtils.getDefinition().isSinkStatement(unit);
    }

    @Override
    public boolean propagateObjectTaint() {
        return true;
    }

    @Override
    public final boolean isFieldSensitiveAnalysis() {
        return true;
    }

    @Override
    public int maxDepth() {
        return this.depthLimit;
    }

    public int getDepthLimit() {
        return this.depthLimit;
    }

    public void setDepthLimit(int depthLimit) {
        this.depthLimit = depthLimit;
    }

    public List<String> reportDFConflicts() {
        Set<List<StatementNode>> conflicts = findSourceSinkPaths();
        List<String> report = new ArrayList<>();

        for (List<StatementNode> conflict : conflicts) {
            buildConflictReport(conflict).ifPresent(report::addAll);
        }

        if (!report.isEmpty()) {
            System.out.println(report.get(0));
        }
        return report;
    }

    private Optional<List<String>> buildConflictReport(List<StatementNode> conflict) {

        if (!isValidConflict(conflict)) {
            return Optional.empty();
        }

        StatementNode src = conflict.get(0);
        StatementNode sink = conflict.get(conflict.size() - 1);

        VisitedMethods srcStep = src.getPathVisitedMethods().head();
        VisitedMethods sinkStep = sink.getPathVisitedMethods().head();

        int lineSrc = srcStep.line();
        int lineSink = sinkStep.line();

        return Optional.of(
                Collections.singletonList(
                        String.join("\n",
                                "DF interference in " + srcStep.getMethod().method(),
                                "Data flows from execution of line " + lineSrc + " to " + lineSink +
                                        ", defined in " + src.unit() + " and propagated in " + sink.unit(),
                                "Caused by line " + lineSrc + " flow: " + src.pathVisitedMethodsToString(),
                                "Caused by line " + lineSink + " flow: " + sink.pathVisitedMethodsToString())));

    }

    private boolean isValidConflict(List<StatementNode> conflict) {

        if (conflict == null || conflict.isEmpty()) {
            return false;
        }

        StatementNode src = conflict.get(0);
        StatementNode sink = conflict.get(conflict.size() - 1);

        if (src == null || sink == null) {
            return false;
        }

        return hasValidPath(src) && hasValidPath(sink);
    }

    private boolean hasValidPath(StatementNode node) {
        return node.getPathVisitedMethods() != null &&
                !node.getPathVisitedMethods().isEmpty();
    }

    @Override
    public CG callGraph() {
        return this.callGraph;
    }

    @Override
    public void configureCallGraphPhase() {
        SootWrapper.enableCallGraph(this.callGraph().toString());
    }

    public void setCallGraph(String callGraph) {
        String cg = callGraph.toUpperCase();

        if (cg.contains("SPARK")) {
            this.callGraph = SPARK$.MODULE$;
        } else if (cg.contains("VTA")) {
            this.callGraph = VTA$.MODULE$;
        } else if (cg.contains("RTA")) {
            this.callGraph = RTA$.MODULE$;
        } else {
            this.callGraph = CHA$.MODULE$;
        }
    }

}
