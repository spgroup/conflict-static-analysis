package br.unb.cic.analysis.samples;

import java.util.HashMap;
import java.util.Map;

public class InterproceduralDataflowUsingInterface {

    public void foo() {
        Map<String, Integer> map = new HashMap<>();

        map.put("key1", 1); // source
        map.put("key2", 2); // sink
    }

}
