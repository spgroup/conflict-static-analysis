package br.unb.cic.analysis.dfp;

import br.ufpe.cin.soot.analysis.jimple.JDFP;
import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.soot.graph.*;
import scala.collection.JavaConverters;
import soot.PackManager;
import soot.SootMethod;
import soot.Unit;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An analysis wrapper around the Sparse value
 * flow analysis implementation.
 */
public abstract class DFPAnalysisSemanticConflicts extends JDFP {

    private String cp;
    private int depthLimit;

    private AbstractMergeConflictDefinition definition;

    /**
     * PDGAAnalysis constructor
     * @param classPath a classpath to the software under analysis
     * @param definition a definition with the sources and sinks unities
     */
    public DFPAnalysisSemanticConflicts(String classPath, AbstractMergeConflictDefinition definition) {
        this.cp = classPath;
        this.definition = definition;
        this.depthLimit = 5;
    }

    public DFPAnalysisSemanticConflicts(String classPath, AbstractMergeConflictDefinition definition, int depthLimit) {
        this.cp = classPath;
        this.definition = definition;
        this.depthLimit = depthLimit;
    }

    @Override
    public String sootClassPath() {
        //TODO: what is the role of soot classPath here??
        return cp;
    }

    @Override
    public scala.collection.immutable.List<String> getIncludeList() {
        return JavaConverters.asScalaBuffer(Arrays.asList("")).toList();
    }

    /**
     * Computes the source-sink paths
     * @return a set with a list of nodes that together builds a source-sink path.
     */
    public Set<List<StatementNode>> findSourceSinkPaths() {
        Set<List<StatementNode>> paths = new HashSet<>();

        JavaConverters
                .asJavaCollection(svg().findConflictingPaths())
                .forEach(p -> paths.add(new ArrayList(JavaConverters.asJavaCollection(p))));

       return paths;
    }

    @Override
    public final scala.collection.immutable.List<String> applicationClassPath() {
        String[] array = new String[100];
        if (cp.contains(":/") || cp.contains(":\\")){ // Windows class path error
            array[0] = cp.toString();
        }else{
            array = cp.split(":");
        }
        return JavaConverters.asScalaBuffer(Arrays.asList(array)).toList();
    }

    @Override
    public final scala.collection.immutable.List<SootMethod> getEntryPoints() {
        definition.loadSourceStatements();
        definition.loadSinkStatements();
        return JavaConverters.asScalaBuffer(getSourceStatements()
                .stream()
                .map(Statement::getSootMethod)
                .collect(Collectors.toList())).toList();
    }

    @Override
    public final NodeType analyze(Unit unit) {
        if(isSource(unit)) {
            return SourceNode.instance();
        }
        else if(isSink(unit)) {
            return SinkNode.instance();
        }
        return SimpleNode.instance();
    }

    protected boolean isSource(Unit unit) {
        return getSourceStatements()
                .stream()
                .map(stmt -> stmt.getUnit())
                .anyMatch(u -> u.equals(unit));
    }

    protected boolean isSink(Unit unit) {
        return getSinkStatements()
                .stream()
                .map(stmt -> stmt.getUnit())
                .anyMatch(u -> u.equals(unit));
    }

    protected List<Statement> getSourceStatements() {
        return definition.getSourceStatements();
    }

    protected List<Statement> getSinkStatements() {
        return definition.getSinkStatements();
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
        return depthLimit;
    }

    public void setDepthLimit(int depthLimit) {
        this.depthLimit = depthLimit;
    }

    public List<String> reportDFConflicts(){
        Set<List<StatementNode>>  conflicts = findSourceSinkPaths();
        List<String> conflicts_report = new ArrayList<>();
        for (List<StatementNode> conflict: conflicts){
            try{

                StatementNode p1 = conflict.get(0);
                StatementNode p2 = conflict.get(conflict.size()-1);

                System.out.println("DF interference in "+ p1.getPathVisitedMethods().head().getMethod().method());
                System.out.println("Data flows from execution of line "+p1.getPathVisitedMethods().head().line()+" to "+p2.getPathVisitedMethods().head().line()+", defined in "+p1.unit()+" and propagated in "+p2.unit());
                System.out.println("Caused by line "+p1.getPathVisitedMethods().head().line()+ " flow: "+p1.pathVisitedMethodsToString());
                System.out.println("Caused by line "+p2.getPathVisitedMethods().head().line()+ " flow: "+p2.pathVisitedMethodsToString());

                conflicts_report.add("DF interference in "+ p1.getPathVisitedMethods().head().getMethod().method());
                conflicts_report.add("Data flows from execution of line "+p1.getPathVisitedMethods().head().line()+" to "+p2.getPathVisitedMethods().head().line()+", defined in "+p1.unit()+" and propagated in "+p2.unit());
                conflicts_report.add("Caused by line "+p1.getPathVisitedMethods().head().line()+ " flow: "+p1.pathVisitedMethodsToString());
                conflicts_report.add("Caused by line "+p2.getPathVisitedMethods().head().line()+ " flow: "+p2.pathVisitedMethodsToString()+"\n");
            }catch (Exception e){
                System.out.println("Empty list for reporting data flow! Error: "+ e.getMessage());
            }
        }

        return conflicts_report;
    }


}
