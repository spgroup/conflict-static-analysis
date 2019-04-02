package br.unb.cic.analysis.pt;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.df.Collector;
import br.unb.cic.analysis.model.Pair;
import br.unb.cic.analysis.pt.PointsToAnalysis;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import soot.*;
import soot.jimple.spark.SparkTransformer;
import soot.options.Options;

import java.io.File;
import java.util.*;

public class PointsToAnalysisTest {

    private br.unb.cic.analysis.pt.PointsToAnalysis analysis;
    private AbstractMergeConflictDefinition definition;

    @Before
    public void configure() {
        definition = new AbstractMergeConflictDefinition() {
            @Override
            protected Map<String, List<Integer>> sourceDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                res.put("br.unb.cic.analysis.samples.InterproceduralPointsTo", Arrays.asList(new Integer[]{6}));
                return res;
            }

            @Override
            protected Map<String, List<Integer>> sinkDefinitions() {
                Map<String, List<Integer>> res = new HashMap<>();
                res.put("br.unb.cic.analysis.samples.InterproceduralPointsTo", Arrays.asList(new Integer[]{21}));
                return res;
            }
        };
        initializeSoot();
        analyze();
    }

    protected SceneTransformer createAnalysisTransform() {
        return new SceneTransformer() {
            @Override
            protected void internalTransform(String phaseName, Map<String, String> options) {
                HashMap opt = new HashMap();
                opt.put("verbose","true");
                opt.put("propagator","worklist");
                opt.put("simple-edges-bidirectional","true");
                opt.put("on-fly-cg","true");
                opt.put("set-impl","double");
                opt.put("double-set-old","hybrid");
                opt.put("double-set-new","hybrid");
                SparkTransformer.v().transform("",opt);
                analysis = new PointsToAnalysis(definition);
                analysis.doAnalysis();
            }
        };
    }

    protected void analyze() {
        Transform transform = new Transform("wjtp.myTransform", createAnalysisTransform());
        PackManager.v().getPack("wjtp").add(transform);
        PackManager.v().getPack("cg").apply();
        PackManager.v().getPack("wjtp").apply();
        PackManager.v().runPacks();
    }

    protected void initializeSoot() {
        G.v().reset();

        Options.v().set_whole_program(true);
        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().setPhaseOption("cg.spark", "verbose:true");
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().set_output_format(Options.output_format_none);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(getSootClassPath());
        Options.v().set_keep_line_number(true);

        SootClass sootClass = Scene.v().forceResolve(getTestCaseClassName(), SootClass.BODIES);

        sootClass.setApplicationClass();

        Scene.v().loadNecessaryClasses();

        Set<SootMethod> entryPoints = new HashSet<>();
        for(SootMethod m: sootClass.getMethods()) {
            m.retrieveActiveBody();
            //for(Unit u: m.getActiveBody().getUnits()) {
                //if(definition.isSourceStatement(u)) {
                    entryPoints.add(m);
                //}
            //}
        }
        definition.loadSourceStatements();
        definition.loadSinkStatements();
        Scene.v().setEntryPoints(new ArrayList<>(entryPoints));
    }




    protected String getTestCaseClassName() {
        return "br.unb.cic.analysis.samples.InterproceduralPointsTo";
    }

    protected String getSootClassPath() {
        String userdir = System.getProperty("user.dir");
        String javaHome = System.getProperty("java.home");
        if (javaHome == null || javaHome.equals(""))
            throw new RuntimeException("Could not get property java.home!");

        String sootCp = userdir + "/target/test-classes";
        sootCp += File.pathSeparator + javaHome + "/lib/rt.jar";
        return sootCp;
    }

    @Test
    public void testDataFlowAnalysis() {
        Assert.assertNotNull(analysis);
        Assert.assertNotNull(Collector.instance().getConflicts());
        Assert.assertEquals(1, Collector.instance().getConflicts().size());
    }
}
