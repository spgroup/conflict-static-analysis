package br.unb.cic.analysis.dfp;

import br.ufpe.cin.soot.analysis.jimple.JDFP;
import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.soot.graph.*;
import scala.collection.JavaConverters;
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

    public List<String> generateDFPReportConflict(AbstractMergeConflictDefinition definition){
        List<String> conflicts_report = new ArrayList<>();
        for (List<StatementNode> stmt_list: this.findSourceSinkPaths()){
            StatementNode begin_stmt = stmt_list.get(0);
            StatementNode end_stmt = stmt_list.get(stmt_list.size()-1);

            StringBuilder report_entry_point = new StringBuilder("");
            for (Statement stmt: definition.getSourceStatements()){
                if (checkIfStatementIsEqualsStatmentNode(begin_stmt, stmt)){
                    report_entry_point.append(stmt.getTraversedLine().toString());
                    break;
                }
            }

            String report_stmts = "Begin Statement: "+begin_stmt.unit()+", line "+begin_stmt.line()+" => End Statement: "+end_stmt.unit()+", line "+end_stmt.line();

            for (Statement stmt: definition.getSinkStatements()){
                if (checkIfStatementIsEqualsStatmentNode(end_stmt, stmt)){
                    report_entry_point.append(" to " + stmt.getTraversedLine().toString());
                    break;
                }
            }

            System.out.println("\n"+report_entry_point);
            System.out.println(report_stmts);
            System.out.println("Path Statements: "+ stmt_list.toString());

            conflicts_report.add(report_entry_point+" "+report_stmts+" Path Statements: "+ stmt_list.toString());

        }

        return conflicts_report;
    }

    public boolean checkIfStatementIsEqualsStatmentNode(StatementNode stmt_node, Statement stmt){
        return stmt.getUnit().equals(stmt_node.value().sootUnit()) &&
                stmt.getSootMethod().getSignature().equals(stmt_node.value().method()) &&
                stmt.getSourceCodeLineNumber().equals(stmt_node.value().line());
    }
}
