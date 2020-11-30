package br.unc.cic.analysis.test;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;

import java.util.*;
import java.util.stream.Collectors;

public class DefinitionFactory {

    public static AbstractMergeConflictDefinition definition(String className, int sourceLines[], int sinkLines[]) {
        return definition(className, sourceLines, sinkLines, false);
    }

    public static AbstractMergeConflictDefinition definition(String className, int sourceLines[], int sinkLines[], boolean recursive) {
        return new AbstractMergeConflictDefinition(recursive) {
                @Override
                protected Map<String, List<Integer>> sourceDefinitions() {
                    Map<String, List<Integer>> res = new HashMap<>();
                    List<Integer> lines = Arrays.stream(sourceLines).boxed().collect(Collectors.toList());
                    res.put(className, lines);
                    return res;
                }

                @Override
                protected Map<String, List<Integer>> sinkDefinitions() {
                    Map<String, List<Integer>> res = new HashMap<>();
                    List<Integer> lines = Arrays.stream(sinkLines).boxed().collect(Collectors.toList());
                    res.put(className, lines);
                    return res;
                }
            };
        }
}
