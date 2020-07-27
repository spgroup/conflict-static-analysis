package br.unb.cic.analysis.svfa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;

import br.unb.cic.soot.graph.*;
import br.unb.cic.soot.svfa.jimple.JSVFA;
import scala.collection.JavaConverters;
import scala.collection.immutable.List;
import soot.SootMethod;
import soot.Unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An analysis wrapper around the Sparse value
 * flow analysis implementation.
 */
public class SVFAAnalysis extends JSVFA  {

    private String cp;
    private AbstractMergeConflictDefinition definition;

    /**
     * SVFAAnalysis constructor
     * @param classPath a classpath to the software under analysis
     * @param definition a definition with the sources and sinks unities
     */
    public SVFAAnalysis(String classPath, AbstractMergeConflictDefinition definition) {
        this.cp = classPath;
        this.definition = definition;
    }

    @Override
    public String sootClassPath() {
        //TODO: what is the role of soot classPath here??
        return "";
    }

//    @Override
    public List<String> getIncludeList() {
        String[] array = new String[0];
        return JavaConverters.asScalaBuffer(Arrays.asList(array)).toList();
    }

    /**
     * Computes the source-sink paths
     * @return a set with a list of nodes that together builds a source-sink path.
     */
//    public java.util.Set<java.util.List<Node>> findSourceSinkPaths() {
//        Set<java.util.List<Node>> paths = new HashSet<>();
//
//        JavaConverters
//                .asJavaCollection(findConflictingPaths())
//                .forEach(p -> paths.add(new ArrayList<>(JavaConverters.asJavaCollection(p))));
//
//       return paths;
//    }

    @Override
    public final List<String> applicationClassPath() {
        String[] array = cp.split(":");
        return JavaConverters.asScalaBuffer(Arrays.asList(array)).toList();
    }

    @Override
    public final List<SootMethod> getEntryPoints() {
        definition.loadSourceStatements();
        definition.loadSinkStatements();
        return JavaConverters.asScalaBuffer(definition.getSourceStatements()
                .stream()
                .map(stmt -> stmt.getSootMethod())
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

    private boolean isSource(Unit unit) {
        return definition.getSourceStatements()
                .stream()
                .map(stmt -> stmt.getUnit())
                .anyMatch(u -> u.equals(unit));
    }

    private boolean isSink(Unit unit) {
        return definition.getSinkStatements()
                .stream()
                .map(stmt -> stmt.getUnit())
                .anyMatch(u -> u.equals(unit));
    }

    @Override
    public final boolean isFieldSensitiveAnalysis() {
        return true;
    }
}
