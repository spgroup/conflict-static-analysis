package br.unb.cic.analysis.io;

import br.unb.cic.analysis.ClassChangeDefinition;

import java.util.List;

public interface MergeConflictReader {
    public List<ClassChangeDefinition> read() throws Exception;
}
