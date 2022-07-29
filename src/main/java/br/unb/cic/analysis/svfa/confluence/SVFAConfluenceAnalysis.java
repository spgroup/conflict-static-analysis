package br.unb.cic.analysis.svfa.confluence;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.analysis.svfa.SVFAAnalysis;
import br.ufpe.cin.soot.graph.LambdaNode;
import soot.Unit;

import java.util.*;

public class SVFAConfluenceAnalysis {

    private String cp;
    private boolean interprocedural;
    private AbstractMergeConflictDefinition definition;
    private Set<ConfluenceConflict> confluentFlows = new HashSet<>();

    public SVFAConfluenceAnalysis(String classPath, AbstractMergeConflictDefinition definition, boolean interprocedural) {
        this.cp = classPath;
        this.definition = definition;
        this.interprocedural = interprocedural;
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
    public void execute() {
        SVFAAnalysis sourceBaseAnalysis = sourceBaseAnalysis(interprocedural);
        sourceBaseAnalysis.buildSparseValueFlowGraph();
        Set<List<LambdaNode>> sourceBasePaths = sourceBaseAnalysis.findSourceSinkPaths();

        SVFAAnalysis sinkBaseAnalysis = sinkBaseAnalysis(interprocedural);
        sinkBaseAnalysis.buildSparseValueFlowGraph();
        Set<List<LambdaNode>> sinkBasePaths = sinkBaseAnalysis.findSourceSinkPaths();

        confluentFlows = intersectPathsByLastNode(sourceBasePaths, sinkBasePaths);
    }

    /**
     * Intersects the list of paths looking for paths that have the same last nodes
     * also ignores redundant node (that represent different jimple lines but the same Java line)
     * @param paths1 A set of lists of nodes with at least 2 nodes
     * @param paths1 A set of lists of nodes with at least 2 nodes
     * @return A set of confluence conflicts
     */
    private Set<ConfluenceConflict> intersectPathsByLastNode(Set<List<LambdaNode>> paths1, Set<List<LambdaNode>> paths2) {
        Map<LambdaNode, List<LambdaNode>> pathEndHash = new HashMap<>();

        for (List<LambdaNode> path: paths1) {
            pathEndHash.put(getLastNode(path), path);
        }

        Set<ConfluenceConflict> result = new HashSet<>();
        for (List<LambdaNode> path : paths2) {
            LambdaNode lastNode = getLastNode(path);
            if (pathEndHash.containsKey(lastNode)) {
                result.add(new ConfluenceConflict(pathEndHash.get(lastNode), path));
            }
        }

        return result;
    }

    /**
     * @param path A list of nodes with at least 2 nodes
     * @return The last node of the list
     */
    private LambdaNode getLastNode(List<LambdaNode> path) {
        int pathSize = path.size();
        assert pathSize > 1; // assume that all paths have at least one source and one sink
        return path.get(pathSize - 1);
    }

    /**
     * @return A instance of a child class of the SVFAAnalysis class that redefine source and sink as source and base
     */
    private SVFAAnalysis sourceBaseAnalysis(boolean interprocedural) {
        return new SVFAAnalysis(this.cp, this.definition) {

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

            @Override
            public boolean propagateObjectTaint() {
                return true;
            }
        };
    }

    /**
     * @return A instance of a child class of the SVFAAnalysis class that redefine source and sink as source and base
     */
    private SVFAAnalysis sinkBaseAnalysis(boolean interprocedural) {
        return new SVFAAnalysis(this.cp, this.definition) {

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

            @Override
            public boolean propagateObjectTaint() {
                return true;
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
}
