package br.unb.cic.analysis.dfp;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.Main;
import br.unb.cic.analysis.SootWrapper;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.analysis.model.TraversedLine;
import br.unb.cic.analysis.svfa.confluence.ConfluenceConflict;
import br.unb.cic.analysis.svfa.confluence.DFPConfluenceAnalysis;
import br.unb.cic.soot.graph.StatementNode;
import br.unc.cic.analysis.test.DefinitionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class DFPAnalysisBaseTest {

    private DFPAnalysisSemanticConflicts analysis;
    AbstractMergeConflictDefinition definition;
    @Before
    public void configure() {
        definition = new AbstractMergeConflictDefinition(true) {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(11);
                res.put("br.unb.cic.analysis.samples.DFPBaseSample", lines);
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                List<Integer> lines = new ArrayList<>();
                lines.add(13);
                res.put("br.unb.cic.analysis.samples.DFPBaseSample", lines);
                return res;
            }
        };

        String cp = "target/test-classes";
        analysis = new DFPInterProcedural(cp, definition);
    }

    @Test
    public void testDFPAnalysisExpectingOneConflict() {
        analysis.configureSoot();
//        analysis.setPrintDepthVisitedMethods(true);

        analysis.buildDFP();
        generateDFPReportConflict(analysis);
        System.out.println(analysis.svg().reportConflicts().size());
//        System.out.println(analysis.svgToDotModel());
//        System.out.println(analysis.findSourceSinkPaths());
        System.out.println(analysis.svg().findConflictingPaths());
//        Assert.assertTrue(analysis.svg().reportConflicts().size() >= 1);
    }



    public List<String> generateDFPReportConflict(DFPAnalysisSemanticConflicts analysis){
        List<String> conflicts_string = new ArrayList<>();
        for (List<StatementNode> stmt_list: analysis.findSourceSinkPaths()){
            StatementNode begin_stmt = stmt_list.get(0);
            StatementNode end_stmt = stmt_list.get(stmt_list.size()-1);

            String report_entry_point = "";
            for (Statement stmt: definition.getSourceStatements()){
                String aux = begin_stmt.value(). stmt();

                if (stmt.toString().equals(aux)){
                    report_entry_point = stmt.getTraversedLine().toString();
                    break;
                }
            }

            String report_stmts = "Begin Statement: "+begin_stmt.unit()+", line "+begin_stmt.line()+" => End Statement: "+end_stmt.unit()+", line "+end_stmt.line();

            for (Statement stmt: definition.getSinkStatements()){
                String aux = end_stmt.value().stmt();

                if (stmt.toString().equals(aux)){
                    report_entry_point = report_entry_point+ " to " + stmt.getTraversedLine().toString();
                    break;
                }
            }

            System.out.println("\n"+report_entry_point);
            System.out.println(report_stmts);
            System.out.println("Path Statements: "+ stmt_list.toString());

            conflicts_string.add(report_entry_point+" "+report_stmts+" Path Statements: "+ stmt_list.toString());

        }

        return conflicts_string;
    }
}
