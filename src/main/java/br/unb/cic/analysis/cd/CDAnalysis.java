package br.unb.cic.analysis.cd;

import br.ufpe.cin.soot.analysis.jimple.JCD;
import br.ufpe.cin.soot.graph.*;
import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Statement;
import scala.collection.JavaConverters;
import soot.SootMethod;
import soot.Unit;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An analysis wrapper around the Sparse value
 * flow analysis implementation.
 */
public abstract class CDAnalysis extends JCD {

    private String cp;

    private AbstractMergeConflictDefinition definition;

    /**
     * PDGAAnalysis constructor
     * @param classPath a classpath to the software under analysis
     * @param definition a definition with the sources and sinks unities
     */
    public CDAnalysis(String classPath, AbstractMergeConflictDefinition definition) {
        this.cp = classPath;
        this.definition = definition;
    }

    @Override
    public String sootClassPath() {
        //TODO: what is the role of soot classPath here??
        return "";
    }

    @Override
    public scala.collection.immutable.List<String> getIncludeList() {
        String[] array = new String[0];
        return JavaConverters.asScalaBuffer(Arrays.asList(array)).toList();
    }

    /**
     * Computes the source-sink paths
     * @return a set with a list of nodes that together builds a source-sink path.
     */
    public Set<List<LambdaNode>> findSourceSinkPaths() {
        Set<List<LambdaNode>> paths = new HashSet<>();

        JavaConverters
                .asJavaCollection(cd().findConflictingPaths())
                .forEach(p -> paths.add(new ArrayList(JavaConverters.asJavaCollection(p))));

       return paths;
    }

    @Override
    public final scala.collection.immutable.List<String> applicationClassPath() {
        String[] array = new String[100];
        if (cp.contains(":/") || cp.contains(":\\")){ // Está com erro aqui!
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
    public final boolean isFieldSensitiveAnalysis() {
        return true;
    }
}
