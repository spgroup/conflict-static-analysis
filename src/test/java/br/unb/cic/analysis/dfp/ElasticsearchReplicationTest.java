package br.unb.cic.analysis.dfp;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.ClassChangeDefinition;
import br.unb.cic.analysis.io.DefaultReader;
import br.unb.cic.analysis.io.MergeConflictReader;
import br.unb.cic.analysis.model.Statement;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

public class ElasticsearchReplicationTest {

    private String cp = "output/files/elasticsearch/d896886973660785aac45275ddb110c1a6babc57/original-without-dependencies/merge/elasticsearch-tests.jar";
    private String csv = "output/files/elasticsearch/d896886973660785aac45275ddb110c1a6babc57/changed-methods/org.elasticsearch.action.support.replication.ReplicationOperationTests/testReplication()/left-right-lines.csv";

    private AbstractMergeConflictDefinition loadDefinition(String filePath) throws Exception {
        MergeConflictReader reader = new DefaultReader(filePath);
        List<ClassChangeDefinition> changes = reader.read();
        Map<String, List<Integer>> sourceDefs = new HashMap<>();
        Map<String, List<Integer>> sinkDefs = new HashMap<>();

        for (ClassChangeDefinition change : changes) {
            if (change.getType().equals(Statement.Type.SOURCE)) {
                addChange(sourceDefs, change);
            } else {
                addChange(sinkDefs, change);
            }
        }

        return new AbstractMergeConflictDefinition() {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                return sourceDefs;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                return sinkDefs;
            }
        };
    }

    private void addChange(Map<String, List<Integer>> map, ClassChangeDefinition change) {
        if (map.containsKey(change.getClassName())) {
            map.get(change.getClassName()).add(change.getLineNumber());
        } else {
            List<Integer> lines = new ArrayList<>();
            lines.add(change.getLineNumber());
            map.put(change.getClassName(), lines);
        }
    }

    @Test
    public void testElasticsearchReplicationAnomalySPARK() throws Exception {
        AbstractMergeConflictDefinition definition = loadDefinition(csv);

        // Mode: dfp-inter, CG: SPARK
        DFPInterProcedural analysis = new DFPInterProcedural(cp, definition, 5, new ArrayList<>());
        analysis.setCallGraph("SPARK");

        analysis.configureSoot();
        analysis.buildDFP();

        List<String> conflicts = analysis.reportDFConflicts();
        System.out.println("Elasticsearch Anomaly Conflicts: " + conflicts.size());

        try (java.io.PrintWriter writer = new java.io.PrintWriter("conflict_report.txt")) {
            conflicts.forEach(writer::println);
        }

        // O objetivo é encontrar o conflito que antes era perdido
        Assert.assertTrue("Should find at least one conflict in Elasticsearch replication scenario",
                conflicts.size() > 0);
    }

    @Test
    public void testElasticsearchReplicationAnomalyCHA() throws Exception {
        AbstractMergeConflictDefinition definition = loadDefinition(csv);

        // Mode: dfp-inter, CG: CHA
        DFPInterProcedural analysis = new DFPInterProcedural(cp, definition, 5, new ArrayList<>());
        analysis.setCallGraph("CHA");

        analysis.configureSoot();
        analysis.buildDFP();

        List<String> conflicts = analysis.reportDFConflicts();
        System.out.println("Elasticsearch Anomaly Conflicts: " + conflicts.size());

        // O objetivo é encontrar o conflito que antes era perdido
        Assert.assertTrue("Should find at least one conflict in Elasticsearch replication scenario",
                conflicts.size() > 0);
    }

    @Test
    public void testElasticsearchReplicationAnomalyVTA() throws Exception {
        AbstractMergeConflictDefinition definition = loadDefinition(csv);

        // Mode: dfp-inter, CG: VTA
        DFPInterProcedural analysis = new DFPInterProcedural(cp, definition, 5, new ArrayList<>());
        analysis.setCallGraph("VTA");

        analysis.configureSoot();
        analysis.buildDFP();

        List<String> conflicts = analysis.reportDFConflicts();
        System.out.println("Elasticsearch Anomaly Conflicts: " + conflicts.size());

        // O objetivo é encontrar o conflito que antes era perdido
        Assert.assertTrue("Should find at least one conflict in Elasticsearch replication scenario",
                conflicts.size() > 0);
    }

    @Ignore
    @Test
    public void testElasticsearchReplicationAnomalyRTA() throws Exception {
        AbstractMergeConflictDefinition definition = loadDefinition(csv);

        // Mode: dfp-inter, CG: RTA
        DFPInterProcedural analysis = new DFPInterProcedural(cp, definition, 5, new ArrayList<>());
        analysis.setCallGraph("RTA");

        analysis.configureSoot();
        analysis.buildDFP();

        List<String> conflicts = analysis.reportDFConflicts();
        System.out.println("Elasticsearch Anomaly Conflicts: " + conflicts.size());

        // O objetivo é encontrar o conflito que antes era perdido
        Assert.assertTrue("Should find at least one conflict in Elasticsearch replication scenario",
                conflicts.size() > 0);
    }
}
