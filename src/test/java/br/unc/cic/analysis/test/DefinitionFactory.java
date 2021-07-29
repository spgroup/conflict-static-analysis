package br.unc.cic.analysis.test;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public static AbstractMergeConflictDefinition definition(List<Marking> markings) {
        return new AbstractMergeConflictDefinition(false) {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                for(Marking m: markings){
                    List<Integer> lines = Arrays.stream(m.getSourceLines()).boxed().collect(Collectors.toList());
                    res.put(m.getClassName(), lines);
                }
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                for(Marking m: markings) {
                    List<Integer> lines = Arrays.stream(m.getSinkLines()).boxed().collect(Collectors.toList());
                    res.put(m.getClassName(), lines);
                }
                return res;
            }
        };
    }
}