package br.unb.cic.analysis;

import br.unb.cic.analysis.model.Statement;
import scala.collection.JavaConverters;
import soot.SootClass;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StatementsUtil {

    private AbstractMergeConflictDefinition definition;

    private List<String> entrypoints;

    public StatementsUtil(AbstractMergeConflictDefinition definition, List<String> entrypoints) {
        this.definition = definition;
        this.entrypoints = entrypoints;
    }

    /**
     * Combines all source and sink statements into a single list.
     *
     * @return a list of all source and sink statements.
     */
    private List<Statement> getAllSourceAndSinkStatements() {
        List<Statement> allStatements = new ArrayList<>();
        allStatements.addAll(this.definition.getSourceStatements());
        allStatements.addAll(this.definition.getSinkStatements());
        return allStatements;
    }

    /**
     * Retrieves the entry point methods from the source.
     * This method identifies and returns the entry point methods that are relevant for analysis.
     *
     * @return A Scala list of SootMethod instances representing the entry points.
     */
    private scala.collection.immutable.List<SootMethod> retrieveEntryPointsFromSource() {
        SootMethod traversedMethod = getTraversedMethod();

        if (traversedMethod != null) {
            return JavaConverters.asScalaBuffer(Collections.singletonList(traversedMethod)).toList();
        }

        return JavaConverters.asScalaBuffer(
                this.definition.getSourceStatements()
                        .stream()
                        .map(Statement::getSootMethod)
                        .distinct()
                        .collect(Collectors.toList())
        ).toList();
    }

    /**
     * Configures and retrieves the entry points using the provided entrypoints list and all statements.
     *
     * @return a Scala list of SootMethod representing the configured entry points.
     */
    private scala.collection.immutable.List<SootMethod> getConfiguredEntryPoints() throws NoSuchMethodException {
        return JavaConverters.asScalaBuffer(
                new ArrayList<>(this.definition.configureEntryPoints(this.entrypoints))
        ).toList();
    }

    public final scala.collection.immutable.List<SootMethod> getEntryPoints() {
        this.definition.loadSourceStatements();
        this.definition.loadSinkStatements();

        List<Statement> allStatements = getAllSourceAndSinkStatements();

        if (this.entrypoints == null || this.entrypoints.isEmpty()) {
            return retrieveEntryPointsFromSource();
        } else {
            try {
                return getConfiguredEntryPoints();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private SootMethod getTraversedMethod() {
        try {
            SootClass sootClass = this.definition.getSourceStatements().get(0).getSootClass();
            return sootClass.getMethodByName("callRealisticRun");
        } catch (RuntimeException e) {
            return null;
        }
    }

    public AbstractMergeConflictDefinition getDefinition() {
        return this.definition;
    }

}
