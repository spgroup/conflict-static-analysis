package br.unb.cic.analysis.pdg;

import br.ufpe.cin.soot.analysis.jimple.JPDG;
import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.StatementsUtil;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.soot.graph.*;
import br.unb.cic.soot.svfa.CG;
import br.unb.cic.soot.svfa.CHA$;
import br.unb.cic.soot.svfa.SPARK$;
import scala.collection.JavaConverters;
import soot.SootMethod;
import soot.Unit;

import java.io.File;
import java.util.*;

/**
 * An analysis wrapper around the Sparse value
 * flow analysis implementation.
 */
public abstract class PDGAnalysisSemanticConflicts extends JPDG {

    private String cp;

    private StatementsUtil statementsUtils;
    private CG callGraph = SPARK$.MODULE$;
    private AbstractMergeConflictDefinition definition;

    /**
     * PDGAAnalysis constructor
     *
     * @param classPath   a classpath to the software under analysis
     * @param definition  a definition with the sources and sinks unities
     * @param entrypoints the list of entry points for the analysis
     */
    public PDGAnalysisSemanticConflicts(String classPath, AbstractMergeConflictDefinition definition, List<String> entrypoints) {
        this.cp = classPath;
        this.statementsUtils = new StatementsUtil(definition, entrypoints);
    }

    public PDGAnalysisSemanticConflicts(String classPath, AbstractMergeConflictDefinition definition) {
        this(classPath, definition, new ArrayList<>());
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
    public Set<List<LambdaNode>> findSourceSinkPaths() {
        Set<List<LambdaNode>> paths = new HashSet<>();

        JavaConverters
                .asJavaCollection(svg().findConflictingPaths())
                .forEach(p -> paths.add(new ArrayList(JavaConverters.asJavaCollection(p))));

       return paths;
    }

    @Override
    public final scala.collection.immutable.List<String> applicationClassPath() {
        String[] array = cp.split(File.pathSeparator);
        return JavaConverters.asScalaBuffer(Arrays.asList(array)).toList();
    }

    @Override
    public final scala.collection.immutable.List<SootMethod> getEntryPoints() {
        return this.statementsUtils.getEntryPoints();
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

    protected List<Statement> getSourceStatements() {
        return this.statementsUtils.getDefinition().getSourceStatements();
    }

    protected List<Statement> getSinkStatements() {
        return this.statementsUtils.getDefinition().getSinkStatements();
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
    public CG callGraph() {
        return this.callGraph;
    }

    public void setCallGraph(String callGraph){
        if (callGraph.toUpperCase().contains("SPARK")){
            this.callGraph = SPARK$.MODULE$;
        }else{
            this.callGraph = CHA$.MODULE$;
        }
    }

}
