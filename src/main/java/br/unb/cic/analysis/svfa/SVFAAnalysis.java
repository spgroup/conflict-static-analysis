package br.unb.cic.analysis.svfa;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.soot.graph.NodeType;
import br.unb.cic.soot.graph.SimpleNode;
import br.unb.cic.soot.graph.SinkNode;
import br.unb.cic.soot.graph.SourceNode;
import br.unb.cic.soot.svfa.jimple.JSVFA;
import scala.collection.JavaConverters;
import scala.collection.immutable.List;
import soot.SootMethod;
import soot.Unit;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SVFAAnalysis extends JSVFA {

    private String cp;
    private AbstractMergeConflictDefinition definition;

    public SVFAAnalysis(String classPath, AbstractMergeConflictDefinition definition) {
        this.cp = classPath;
        this.definition = definition;
    }

    @Override
    public String sootClassPath() {
        //TODO: what is the role of soot classPath here??
        return "";
    }

    @Override
    public List<String> applicationClassPath() {
        String[] array = cp.split(":");
        return JavaConverters.asScalaBuffer(Arrays.asList(array)).toList();
    }

    @Override
    public List<SootMethod> getEntryPoints() {
        definition.loadSourceStatements();
        definition.loadSinkStatements();
        return JavaConverters.asScalaBuffer(definition.getSourceStatements()
                .stream()
                .map(stmt -> stmt.getSootMethod())
                .collect(Collectors.toList())).toList();
    }

    @Override
    public NodeType analyze(Unit unit) {
        if(isSource(unit)) {
            return new SourceNode();
        }
        else if(isSink(unit)) {
            return new SinkNode();
        }
        return new SimpleNode();
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
}
