package br.unb.cic.analysis.svfa.confluence;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.Main;
import br.unb.cic.analysis.dfp.DFPAnalysisSemanticConflicts;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.analysis.model.TraversedLine;
import br.unb.cic.soot.graph.StatementNode;
import ch.qos.logback.core.net.SyslogOutputStream;
import com.google.common.base.Stopwatch;
import soot.AbstractSootFieldRef;
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
    private String getGraphSize;

    private int visitedMethods;

    public DFPConfluenceAnalysis(String classPath, AbstractMergeConflictDefinition definition, boolean interprocedural) {
        this.cp = classPath;
        this.definition = definition;
        this.interprocedural = interprocedural;
        this.depthLimit = 5;
    }

    public DFPConfluenceAnalysis(String classPath, AbstractMergeConflictDefinition definition, boolean interprocedural, int depthLimit) {
        this.cp = classPath;
        this.definition = definition;
        this.interprocedural = interprocedural;
        this.depthLimit = depthLimit;
    }

    /**
     * After the execute method has been called, it returns the confluent conflicts returned by the algorithm
     * @return a set of confluence conflicts
     */
    public Set<ConfluenceConflict> getConfluentConflicts() {
        return confluentFlows;
    }

    /**
     * Executes both source -> base and sink -> base SVFA analysis intersects then populating
     * the confluentFlows attribute with the results
     */
    public void execute(boolean depthMethodsVisited) {
        DFPAnalysisSemanticConflicts sourceBaseAnalysis = sourceBaseAnalysis(interprocedural);
        String type_analysis;
        if (interprocedural) {
            type_analysis = "Inter";
        } else {
            type_analysis = "Intra";
        }
        Main m = new Main();
        m.stopwatch = Stopwatch.createStarted();
        sourceBaseAnalysis.setPrintDepthVisitedMethods(depthMethodsVisited);

        sourceBaseAnalysis.configureSoot();
        Options.v().ignore_resolution_errors();
        m.saveExecutionTime("Configure Soot Confluence 1 "+type_analysis);

        m.stopwatch = Stopwatch.createStarted();

        sourceBaseAnalysis.buildDFP();
        Set<List<StatementNode>> sourceBasePaths = sourceBaseAnalysis.findSourceSinkPaths();

        m.saveExecutionTime("Time to perform Confluence 1 "+type_analysis);

        m.stopwatch = Stopwatch.createStarted();

        G.v().reset();

        DFPAnalysisSemanticConflicts sinkBaseAnalysis = sinkBaseAnalysis(interprocedural);
        sinkBaseAnalysis.setPrintDepthVisitedMethods(depthMethodsVisited);

        sinkBaseAnalysis.configureSoot();

        m.saveExecutionTime("Configure Soot Confluence 2 "+type_analysis);

        m.stopwatch = Stopwatch.createStarted();

        sinkBaseAnalysis.buildDFP();

        Set<List<StatementNode>> sinkBasePaths = sinkBaseAnalysis.findSourceSinkPaths();

        confluentFlows = intersectPathsByLastNode(sourceBasePaths, sinkBasePaths);

        m.saveExecutionTime("Time to perform Confluence 2 "+type_analysis);

        System.out.println("Visited methods: "+ (sourceBaseAnalysis.getNumberVisitedMethods()+sinkBaseAnalysis.getNumberVisitedMethods()));
        setVisitedMethods(sourceBaseAnalysis.getNumberVisitedMethods()+sinkBaseAnalysis.getNumberVisitedMethods());
        setGraphSize(sourceBaseAnalysis, sinkBaseAnalysis);
    }

     public List<String> reportConflictsConfluence(){
        List<String> conflicts_report = new ArrayList<>();
         List<Integer> left_lines = new ArrayList<>();
         List<Integer> right_lines = new ArrayList<>();
         List<Integer> cf_lines = new ArrayList<>();

         for (ConfluenceConflict conflict: confluentFlows){

            StatementNode df1 = conflict.getSourceNodePath().get(0);
            StatementNode df2 = conflict.getSinkNodePath().get(0);

            StatementNode confluence = conflict.getSinkNodePath().get(conflict.getSinkNodePath().size()-1);

            Integer left_line = df1.getPathVisitedMethods().head().line();
            Integer right_line = df2.getPathVisitedMethods().head().line();
            Integer cf_line = confluence.getPathVisitedMethods().head().line();

            Boolean contains_lines = left_lines.contains(left_line)
                    && right_lines.contains(right_line)
                    && cf_lines.contains(cf_line);

            if (!contains_lines){
                System.out.println("Confluence interference in "+ df1.getPathVisitedMethods().head().getMethod().method());
                System.out.println("Confluence flows from execution of lines "+df1.getPathVisitedMethods().head().line()+" and "+df2.getPathVisitedMethods().head().line()+
                        " to line "+confluence.getPathVisitedMethods().head().line()+", defined in "+df1.getPathVisitedMethods().head().getUnit()+" and "+df2.getPathVisitedMethods().head().getUnit()+" and used in "+confluence.getPathVisitedMethods().head().getUnit());
                System.out.println("Caused by line "+left_line+ " flow: "+df1.pathVisitedMethodsToString());
                System.out.println("Caused by line "+right_line+ " flow: "+df2.pathVisitedMethodsToString());
                System.out.println("Caused by line "+cf_line+ " flow: "+confluence.pathVisitedMethodsToString());

                conflicts_report.add("Confluence interference in "+ df1.getPathVisitedMethods().head().getMethod().method());
                conflicts_report.add("Confluence flows from execution of lines "+df1.getPathVisitedMethods().head().line()+" and "+df2.getPathVisitedMethods().head().line()+
                        " to line "+confluence.getPathVisitedMethods().head().line()+", defined in "+df1.getPathVisitedMethods().head().getUnit()+" and "+df2.getPathVisitedMethods().head().getUnit()+" and used in "+confluence.getPathVisitedMethods().head().getUnit());
                conflicts_report.add("Caused by line "+left_line+ " flow: "+df1.pathVisitedMethodsToString());
                conflicts_report.add("Caused by line "+right_line+ " flow: "+df2.pathVisitedMethodsToString());
                conflicts_report.add("Caused by line "+cf_line+ " flow: "+confluence.pathVisitedMethodsToString()+"\n");

                left_lines.add(left_line);
                right_lines.add(right_line);
                cf_lines.add(cf_line);
            }


         }
        return conflicts_report;
    }

    /**
     * Intersects the list of paths looking for paths that have the same last nodes
     * also ignores redundant node (that represent different jimple lines but the same Java line)
     * @param paths1 A set of lists of nodes with at least 2 nodes
     * @param paths1 A set of lists of nodes with at least 2 nodes
     * @return A set of confluence conflicts
     */
    private Set<ConfluenceConflict> intersectPathsByLastNode(Set<List<StatementNode>> paths1, Set<List<StatementNode>> paths2) {
        Map<StatementNode, List<StatementNode>> pathEndHash = new HashMap<>();

        for (List<StatementNode> path: paths1) {
            pathEndHash.put(getLastNode(path), path);
        }

        Set<ConfluenceConflict> result = new HashSet<>();
        for (List<StatementNode> path : paths2) {
            StatementNode lastNode = getLastNode(path);

            StatementNode stmt = containsKey(pathEndHash, lastNode);
            if (stmt!= null) {
                result.add(new ConfluenceConflict(pathEndHash.get(stmt), path));
            }
        }

        return result;
    }


    public StatementNode containsKey(Map<StatementNode, List<StatementNode>> pathEndHash, StatementNode lastNode){
        for (StatementNode stmt: pathEndHash.keySet()){
            if (lastNode.equals(stmt)) {
                return stmt;
            }
        }
        return null;
    }

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
     * @return A instance of a child class of the JDFPAnalysis class that redefine source and sink as source and base
     */
    private br.unb.cic.analysis.dfp.DFPAnalysisSemanticConflicts sourceBaseAnalysis(boolean interprocedural) {
        return new br.unb.cic.analysis.dfp.DFPAnalysisSemanticConflicts(this.cp, this.definition, depthLimit) {

            /**
             * Here we define the list of source statements for the SVFA analysis as the confluence analysis source statements,
             * this interferes with the isSource method, that will be used to determine if a Unit is a source and also
             * will be used at the isSink method.
             */
            @Override
            protected List<Statement> getSourceStatements() {
                return definition.getSourceStatements();
            }

            /**
             * As in this case we want to detect flows between source and base, this methods defines isSink as all units
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
     * @return A instance of a child class of the SVFAAnalysis class that redefine source and sink as source and base
     */
    private DFPAnalysisSemanticConflicts sinkBaseAnalysis(boolean interprocedural) {
        return new DFPAnalysisSemanticConflicts(this.cp, this.definition, depthLimit) {

            /**
             * Here we define the list of source statements for the SVFA analysis as the confluence analysis sink statements,
             * this interferes with the isSource method, that will be used to determine if a Unit is a source and also
             * will be used at the isSink method.
             */
            @Override
            protected List<Statement> getSourceStatements() {
                return definition.getSinkStatements();
            }

            /**
             * As in this case we want to detect flows between sink and base, this methods defines isSink as all units
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
         * Some Jimple units actually don't represent real lines and are not in inside the method body.
         */
        return unit.getJavaSourceStartLineNumber() > 0;
    }

    private boolean isNotSourceOrSink(Unit unit) {
        return unitIsNotOnList(definition.getSourceStatements(), unit) &&
                unitIsNotOnList(definition.getSinkStatements(), unit);
    }

    private boolean unitIsNotOnList(List<Statement> statements, Unit unit) {
        return statements.stream().map(stmt -> stmt.getUnit()).noneMatch(u -> u.equals(unit));
    }

    public int getVisitedMethods() {
        return visitedMethods;
    }

    public void setGraphSize(DFPAnalysisSemanticConflicts source, DFPAnalysisSemanticConflicts sink){
        this.getGraphSize = (source.svg().graph().size()+","+source.svg().edges().size()+","+sink.svg().graph().size()+","+sink.svg().edges().size());
    }

    public String getGraphSize(){
        return this.getGraphSize;
    }

    public void setVisitedMethods(int visitedMethods) {
        this.visitedMethods = visitedMethods;
    }
    public void setCp(String cp) {
        this.cp = cp;
    }

    public int getDepthLimit() {
        return depthLimit;
    }
}
