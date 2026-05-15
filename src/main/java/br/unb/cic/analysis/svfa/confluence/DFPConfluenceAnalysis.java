package br.unb.cic.analysis.svfa.confluence;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.Main;
import br.unb.cic.analysis.dfp.DFPAnalysisSemanticConflicts;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.soot.graph.StatementNode;
import br.unb.cic.soot.graph.VisitedMethods;
import com.google.common.base.Stopwatch;
import soot.G;
import soot.Scene;
import soot.Unit;
import soot.options.Options;

import java.util.*;

public class DFPConfluenceAnalysis {

    private String cp;
    private boolean interprocedural;
    private AbstractMergeConflictDefinition definition;
    private Set<ConfluenceConflict> confluentFlows = new HashSet<>();
    private int depthLimit;
    private List<String> entrypoints;
    private String getGraphSize;
    private int visitedMethods;

    /**
     * DFPConfluenceAnalysis constructor
     *
     * @param classPath       a classpath to the software under analysis
     * @param definition      a definition with the sources and sinks unities
     * @param interprocedural a flag indicating whether to consider interprocedural
     *                        analysis.
     * @param depthLimit      the depth limit for the analysis
     * @param entrypoints     the list of entry points for the analysis
     */
    public DFPConfluenceAnalysis(String classPath, AbstractMergeConflictDefinition definition, boolean interprocedural,
            int depthLimit, List<String> entrypoints) {
        this.cp = classPath;
        this.definition = definition;
        this.interprocedural = interprocedural;
        this.depthLimit = depthLimit;
        this.entrypoints = entrypoints;
    }

    public DFPConfluenceAnalysis(String classPath, AbstractMergeConflictDefinition definition,
            boolean interprocedural) {
        this(classPath, definition, interprocedural, 5, new ArrayList<>());
    }

    public Set<ConfluenceConflict> getConfluentConflicts() {
        return getConfluentConflicts(true);
    }

    /**
     * After the execute method has been called, it returns the confluent conflicts
     * returned by the algorithm
     *
     * @return a set of confluence conflicts
     */
    public Set<ConfluenceConflict> getConfluentConflicts(boolean filterDuplicates) {
        if (!filterDuplicates) {
            return this.confluentFlows;
        }

        List<ConfluenceConflict> conflicts = new ArrayList<>(this.confluentFlows);

        ConfluenceConflict toRemove = findFirstSubStack(conflicts);
        while (toRemove != null) {
            conflicts.remove(toRemove);
            toRemove = findFirstSubStack(conflicts);
        }

        return new HashSet<>(conflicts);
    }

    private ConfluenceConflict findFirstSubStack(List<ConfluenceConflict> conflicts) {

        for (int i = 0; i < conflicts.size(); i++) {
            ConfluenceConflict conflictA = conflicts.get(i);
            List<StatementNode> pathA = conflictA.getSourceNodePath();
            StatementNode confluenceA = pathA.get(pathA.size() - 1);

            for (int j = 0; j < conflicts.size(); j++) {
                if (i == j)
                    continue;

                ConfluenceConflict conflictB = conflicts.get(j);
                List<StatementNode> pathB = conflictB.getSourceNodePath();
                StatementNode confluenceB = pathB.get(pathB.size() - 1);

                if (!confluenceA.value().className().equals(confluenceB.value().className()) ||
                        confluenceA.value().line() != confluenceB.value().line()) {
                    continue;
                }

                if (pathA.size() > pathB.size()) {
                    continue;
                }

                for (int z = 0; z <= pathB.size() - pathA.size(); z++) {
                    boolean match = true;
                    for (int y = 0; y < pathA.size(); y++) {
                        StatementNode s = pathA.get(y);
                        StatementNode b = pathB.get(z + y);

                        if (!s.value().className().equals(b.value().className()) ||
                                !s.value().method().equals(b.value().method()) ||
                                s.value().line() != b.value().line()) {
                            match = false;
                            break;
                        }
                    }
                    if (match)
                        return conflictA;
                }
            }
        }
        return null;
    }

    public void setCallGraph(DFPAnalysisSemanticConflicts instance, String callGraph) {
        instance.setCallGraph(callGraph);
    }

    /**
     * Executes both source -> base and sink -> base SVFA analysis intersects then
     * populating
     * the confluentFlows attribute with the results
     */
    public void execute(boolean depthMethodsVisited, String callGraph) {
        DFPAnalysisSemanticConflicts sourceBaseAnalysis = sourceBaseAnalysis(interprocedural);
        String type_analysis;
        if (this.interprocedural) {
            type_analysis = "Inter";
        } else {
            type_analysis = "Intra";
        }

        Main m = new Main();
        Main.stopwatch = Stopwatch.createStarted();
        sourceBaseAnalysis.setPrintDepthVisitedMethods(depthMethodsVisited);

        setCallGraph(sourceBaseAnalysis, callGraph);

        sourceBaseAnalysis.configureSoot();

        System.out.println("CallGraph: " + sourceBaseAnalysis.callGraph());

        Options.v().ignore_resolution_errors();
        m.saveExecutionTime("Configure Soot Confluence 1 " + type_analysis);

        Main.stopwatch = Stopwatch.createStarted();

        sourceBaseAnalysis.buildDFP();
        Set<List<StatementNode>> sourceBasePaths = sourceBaseAnalysis.findSourceSinkPaths();

        m.saveExecutionTime("Time to perform Confluence 1 " + type_analysis);

        Main.stopwatch = Stopwatch.createStarted();

        G.reset();

        DFPAnalysisSemanticConflicts sinkBaseAnalysis = sinkBaseAnalysis(this.interprocedural);
        sinkBaseAnalysis.setPrintDepthVisitedMethods(depthMethodsVisited);

        setCallGraph(sinkBaseAnalysis, callGraph);

        sinkBaseAnalysis.configureSoot();

        System.out.println("CallGraph: " + sourceBaseAnalysis.callGraph());

        m.saveExecutionTime("Configure Soot Confluence 2 " + type_analysis);

        Main.stopwatch = Stopwatch.createStarted();

        sinkBaseAnalysis.buildDFP();

        Set<List<StatementNode>> sinkBasePaths = sinkBaseAnalysis.findSourceSinkPaths();

        this.confluentFlows = intersectPathsByLastNode(sourceBasePaths, sinkBasePaths);

        this.pointerAnalysisMissingRefs.clear();
        this.pointerAnalysisMissingRefs.addAll(sourceBaseAnalysis.getPointerAnalysisMissingRefs());
        this.pointerAnalysisMissingRefs.addAll(sinkBaseAnalysis.getPointerAnalysisMissingRefs());

        m.saveExecutionTime("Time to perform Confluence 2 " + type_analysis);

        System.out.println("Visited methods: "
                + (sourceBaseAnalysis.getNumberVisitedMethods() + sinkBaseAnalysis.getNumberVisitedMethods()));
        setVisitedMethods(sourceBaseAnalysis.getNumberVisitedMethods() + sinkBaseAnalysis.getNumberVisitedMethods());
        setGraphSize(sourceBaseAnalysis, sinkBaseAnalysis);

        createAnalysisReportLog(sourceBaseAnalysis);
    }

    private void createAnalysisReportLog(DFPAnalysisSemanticConflicts analysis) {
        int countEdges = Scene.v().getCallGraph().size();
        List<soot.SootMethod> methods = scala.collection.JavaConverters
                .seqAsJavaList(analysis.getAnalysisEntryPoints());

        new br.unb.cic.analysis.model.AnalysisRecord.Builder()
                .callGraphAlgorithm(br.unb.cic.analysis.SootWrapper.getCallGraphAlgorithm())
                .callGraphEdgeCount(countEdges)
                .depthLimit(this.depthLimit)
                .visitedMethodsCount(getVisitedMethods())
                .callGraphBuildTimeMs(analysis.getPackageExecutionTimes())
                .callGraphEntryPoint(Scene.v().getEntryPoints())
                .analysisEntryPoint(methods)
                .build();
    }

    private List<br.unb.cic.analysis.model.Statement> pointerAnalysisMissingRefs = new ArrayList<>();

    public List<br.unb.cic.analysis.model.Statement> getPointerAnalysisMissingRefs() {
        return pointerAnalysisMissingRefs;
    }

    public List<String> reportConflictsConfluence() {
        List<String> report = new ArrayList<>();

        for (ConfluenceConflict conflict : this.confluentFlows) {
            buildConfluenceConflictReport(conflict).ifPresent(report::addAll);

        }
        if (!report.isEmpty()) {
            System.out.println(report.get(0));
        }

        return report;
    }

    private Optional<List<String>> buildConfluenceConflictReport(ConfluenceConflict conflict) {

        if (!isValidConfluenceConflict(conflict)) {
            return Optional.empty();
        }

        StatementNode df1 = conflict.getSourceNodePath().get(0);
        StatementNode df2 = conflict.getSinkNodePath().get(0);

        List<StatementNode> sinkPath = conflict.getSinkNodePath();
        StatementNode confluence = sinkPath.get(sinkPath.size() - 1);

        int leftLine = df1.line();
        int rightLine = df2.line();
        int cfLine = confluence.line();

        Optional<VisitedMethods> df1VM = safeHead(df1);
        Optional<VisitedMethods> df2VM = safeHead(df2);
        Optional<VisitedMethods> cfVM = safeHead(confluence);

        // Método (igual ao srcStep.getMethod().method())
        String methodName = (df1VM.isPresent() && df1VM.get().getMethod() != null)
                ? String.valueOf(df1VM.get().getMethod().method())
                : "<unknown method>";

        // Units (equivalente a src.unit() / sink.unit())
        String leftUnit = df1.value().sootUnit().toString();
        String rightUnit = df2.value().sootUnit().toString();
        String confluenceUnit = (cfVM.isPresent() && cfVM.get().getUnit() != null)
                ? cfVM.get().getUnit().toString()
                : confluence.value().toString();

        // Path (equivalente a pathVisitedMethodsToString())
        String leftPath = df1VM.isPresent()
                ? df1.pathVisitedMethodsToString()
                : "<no path>";

        String rightPath = df2VM.isPresent()
                ? df2.pathVisitedMethodsToString()
                : "<no path>";

        return Optional.of(
                Collections.singletonList(
                        String.join("\n",
                                "Confluence interference in " + methodName,

                                "Data flows from execution of lines " +
                                        leftLine + " and " + rightLine +
                                        " to " + cfLine +
                                        ", defined in " + leftUnit +
                                        " and " + rightUnit +
                                        " and propagated in " + confluenceUnit,

                                "Caused by line " + leftLine +
                                        " flow: " + leftPath,

                                "Caused by line " + rightLine +
                                        " flow: " + rightPath)));
    }

    private boolean isValidConfluenceConflict(ConfluenceConflict conflict) {

        if (conflict == null) {
            return false;
        }

        if (conflict.getSourceNodePath() == null ||
                conflict.getSinkNodePath() == null) {
            return false;
        }

        if (conflict.getSourceNodePath().isEmpty() ||
                conflict.getSinkNodePath().isEmpty()) {
            return false;
        }

        StatementNode df1 = conflict.getSourceNodePath().get(0);
        StatementNode df2 = conflict.getSinkNodePath().get(0);

        return hasValidPath(df1) && hasValidPath(df2);
    }

    private boolean hasValidPath(StatementNode node) {
        return node.getPathVisitedMethods() != null &&
                !node.getPathVisitedMethods().isEmpty();
    }

    private Optional<VisitedMethods> safeHead(StatementNode node) {
        if (node == null) {
            return Optional.empty();
        }

        if (node.getPathVisitedMethods() == null ||
                node.getPathVisitedMethods().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(node.getPathVisitedMethods().head());
    }

    /**
     * Intersects the list of paths looking for paths that have the same last nodes
     * also ignores redundant node (that represent different jimple lines but the
     * same Java line)
     *
     * @param paths1 A set of lists of nodes with at least 2 nodes
     * @param paths2 A set of lists of nodes with at least 2 nodes
     * @return A set of confluence conflicts
     */
    private Set<ConfluenceConflict> intersectPathsByLastNode(Set<List<StatementNode>> paths1,
            Set<List<StatementNode>> paths2) {
        Map<StatementNode, List<StatementNode>> pathEndHash = new TreeMap<>(
                Comparator.comparing((StatementNode o) -> o.value().className()).thenComparing(o -> o.value().method())
                        .thenComparingInt(o -> o.value().line()));

        for (List<StatementNode> path : paths1) {
            pathEndHash.put(getLastNode(path), path);
        }

        Set<ConfluenceConflict> result = new HashSet<>();
        for (List<StatementNode> path : paths2) {
            StatementNode lastNode = getLastNode(path);
            if (pathEndHash.containsKey(lastNode)) {
                System.out.println("[CONFLICT_FOUND]");
                result.add(new ConfluenceConflict(pathEndHash.get(lastNode), path));
            }
        }

        return result;
    }

    public StatementNode containsKey(Map<StatementNode, List<StatementNode>> pathEndHash, StatementNode lastNode) {
        for (StatementNode stmt : pathEndHash.keySet()) {
            if (lastNode.value().line() == stmt.value().line() &&
                    lastNode.value().method().equals(stmt.value().method()) &&
                    lastNode.value().className().equals(stmt.value().className())) {
                return stmt;
            }
        }
        return null;
    }

    // private Set<ConfluenceConflict>
    // intersectPathsByLastNode(Set<List<StatementNode>> paths1,
    // Set<List<StatementNode>> paths2) {
    // List<PathEntry> pathEndList = new ArrayList<>();
    //
    // System.out.println("===================================Intersecting
    // paths=====================================");
    // System.out.println("============ paths1:\n");
    // for (List<StatementNode> path: paths1) {
    // System.out.println("(paths1) -> ");
    // StatementNode firstNode = path.get(0);
    // StatementNode lastNode = path.get(path.size() - 1);
    // System.out.println(firstNode.value().className() + ":" +
    // firstNode.value().line() + " / " + firstNode.value().sootUnit() + " -> " +
    // lastNode.value().className() + ":" + lastNode.value().line() + " / " +
    // lastNode.value().sootUnit());
    // }
    // System.out.println("============ paths2:\n");
    // for (List<StatementNode> path: paths2) {
    // System.out.println("(paths2) -> ");
    // StatementNode firstNode = path.get(0);
    // StatementNode lastNode = path.get(path.size() - 1);
    // System.out.println(firstNode.value().className() + ":" +
    // firstNode.value().line() + " / " + firstNode.value().sootUnit() + " -> " +
    // lastNode.value().className() + ":" + lastNode.value().line() + " / " +
    // lastNode.value().sootUnit());
    // }
    // System.out.println("========================================================================================");
    //
    // for (List<StatementNode> path : paths1) {
    // StatementNode lastNode = getLastNode(path);
    // pathEndList.add(new PathEntry(lastNode, path));
    // }
    //
    // Set<ConfluenceConflict> result = new HashSet<>();
    //
    // for (List<StatementNode> path : paths2) {
    // StatementNode lastNode = getLastNode(path);
    //
    // for (PathEntry entry : pathEndList) {
    // if (lastNode.equals(entry.endNode)) {
    // result.add(new ConfluenceConflict(entry.path, path));
    // }
    // }
    // }
    //
    // return result;
    // }
    //
    // private static class PathEntry {
    // public final StatementNode endNode;
    // public final List<StatementNode> path;
    //
    // public PathEntry(StatementNode endNode, List<StatementNode> path) {
    // this.endNode = endNode;
    // this.path = path;
    // }
    // }

    /**
     * @param path A list of nodes with at least 2 nodes
     * @return The last node of the list
     */
    private StatementNode getLastNode(List<StatementNode> path) {
        int pathSize = path.size();
        assert pathSize > 1; // assume that all paths have at least one source and one sink
        return path.get(pathSize - 1);
    }

    /**
     * @return A instance of a child class of the JDFPAnalysis class that redefine
     *         source and sink as source and base
     */
    private br.unb.cic.analysis.dfp.DFPAnalysisSemanticConflicts sourceBaseAnalysis(boolean interprocedural) {
        return new br.unb.cic.analysis.dfp.DFPAnalysisSemanticConflicts(this.cp, this.definition, this.depthLimit,
                this.entrypoints) {

            /**
             * As in this case we want to detect flows between source and base, this methods
             * defines isSink as all units
             * that are neither source nor sink and are inside a method body
             */
            @Override
            protected boolean isSink(Unit unit) {
                return isInMethodBody(unit) && isNotSourceOrSink(unit);
            }

            /**
             * @return true, if using inter-procedural mode.
             */
            @Override
            public boolean interprocedural() {
                return interprocedural;
            }

        };
    }

    /**
     * @return A instance of a child class of the SVFAAnalysis class that redefine
     *         source and sink as source and base
     */
    private DFPAnalysisSemanticConflicts sinkBaseAnalysis(boolean interprocedural) {
        return new DFPAnalysisSemanticConflicts(this.cp, this.definition, this.depthLimit, this.entrypoints) {
            /**
             * As in this case we want to detect flows between sink and base, this methods
             * defines isSource as all units
             * that are initially defined as sink
             */
            @Override
            protected boolean isSource(Unit unit) {
                return definition.isSinkStatement(unit);
            }

            /**
             * As in this case we want to detect flows between sink and base, this methods
             * defines isSink as all units
             * that are neither source nor sink and are inside a method body
             */
            @Override
            protected boolean isSink(Unit unit) {
                return isInMethodBody(unit) && isNotSourceOrSink(unit);
            }

            /**
             * @return true, if using inter-procedural mode.
             */
            @Override
            public boolean interprocedural() {
                return interprocedural;
            }
        };
    }

    private boolean isInMethodBody(Unit unit) {
        /**
         * Some Jimple units actually don't represent real lines and are not in inside
         * the method body.
         */
        return unit.getJavaSourceStartLineNumber() > 0;
    }

    private boolean isNotSourceOrSink(Unit unit) {
        return !this.definition.isSourceStatement(unit) && !this.definition.isSinkStatement(unit);
    }

    public int getVisitedMethods() {
        return this.visitedMethods;
    }

    public void setVisitedMethods(int visitedMethods) {
        this.visitedMethods = visitedMethods;
    }

    public void setGraphSize(DFPAnalysisSemanticConflicts source, DFPAnalysisSemanticConflicts sink) {
        this.getGraphSize = (source.svg().graph().size() + "," + source.svg().edges().size() + ","
                + sink.svg().graph().size() + "," + sink.svg().edges().size());
    }

    public String getGraphSize() {
        return this.getGraphSize;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public int getDepthLimit() {
        return this.depthLimit;
    }
}
