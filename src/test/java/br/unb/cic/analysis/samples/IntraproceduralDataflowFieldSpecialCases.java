package br.unb.cic.analysis.samples;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IntraproceduralDataflowFieldSpecialCases {

    Map<String, String> map = new HashMap<>();

    public void a() {
        this.map.put("a", "a"); // source

        this.map.put("b", "b"); // sink
    }

    public void b() {
        this.map = new ConcurrentHashMap<>(); // source

        this.map.put("a", "a"); // sink
    }

    public void c() {
        this.map.put("a", "a"); // source
        this.map.put("c", "c");
        this.map.put("b", "b"); // sink
    }

}
