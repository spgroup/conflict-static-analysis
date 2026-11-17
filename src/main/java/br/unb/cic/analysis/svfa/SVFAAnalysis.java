package br.unb.cic.analysis.svfa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.StatementsUtil;
import br.unb.cic.soot.graph.*;
import br.unb.cic.soot.svfa.jimple.JSVFA;
import scala.collection.JavaConverters;
import soot.SootMethod;
import soot.Unit;

import java.io.File;
import java.util.*;

/**
 * An analysis wrapper around the Sparse value
 * flow analysis implementation.
 */
public abstract class SVFAAnalysis extends JSVFA {

    private String cp;
    private StatementsUtil statementsUtils;

    /**
     * SVFAAnalysis constructor
     *
     * @param classPath   a classpath to the software under analysis
     * @param definition  a definition with the sources and sinks unities
     * @param entrypoints the list of entry points for the analysis
     */

    public SVFAAnalysis(String classPath, AbstractMergeConflictDefinition definition, List<String> entrypoints) {
        this.cp = classPath;
        this.statementsUtils = new StatementsUtil(definition, entrypoints);
    }

    public SVFAAnalysis(String classPath, AbstractMergeConflictDefinition definition) {
        this(classPath, definition, new ArrayList<>());
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
    public java.util.Set<java.util.List<StatementNode>> findSourceSinkPaths() {
        Set<java.util.List<StatementNode>> paths = new HashSet<>();

        JavaConverters
                .asJavaCollection(svg().findConflictingPaths())
                .forEach(p -> paths.add(new ArrayList(JavaConverters.asJavaCollection(p))));

       return paths;
    }

    @Override
    public final scala.collection.immutable.List<String> applicationClassPath() {
        String[] array = this.cp.split(File.pathSeparator);
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

    @Override
    public final boolean isFieldSensitiveAnalysis() {
        return true;
    }
}
