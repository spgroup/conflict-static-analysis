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
        return definition(Arrays.asList(new MarkingClass(className, sourceLines, sinkLines)), recursive);
    };

    /**
     *
     * @param markingClassList It is a list of tags. Each tag as lines marked as source and as lines marked as sink
     *                         and the name of the class where the lines were marked.
     * @param recursive Indicates whether the loading of statements will be done recursively or not.
     * @return
     */
    public static AbstractMergeConflictDefinition definition(List<MarkingClass> markingClassList, boolean recursive) {
        return new AbstractMergeConflictDefinition(recursive) {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                for(MarkingClass m: markingClassList){
                    List<Integer> lines = Arrays.stream(m.getSourceLines()).boxed().collect(Collectors.toList());
                    res.put(m.getClassName(), lines);
                }
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                for(MarkingClass m: markingClassList) {
                    List<Integer> lines = Arrays.stream(m.getSinkLines()).boxed().collect(Collectors.toList());
                    res.put(m.getClassName(), lines);
                }
                return res;
            }
        };
    }
}