package br.unb.cic.analysis;

import br.unb.cic.analysis.model.Conflict;

import java.util.Set;

/**
 * A simple and common interface to our
 * analysis.
 */
public interface AbstractAnalysis {
    /**
     * Remove all collected conflicts from the
     * analysis.
     */
    public void clear();

    /**
     * Return a set of conflicts collected during the
     * analysis
     *
     * @return a set of conflicts.
     */
    public Set<Conflict> getConflicts();
}
