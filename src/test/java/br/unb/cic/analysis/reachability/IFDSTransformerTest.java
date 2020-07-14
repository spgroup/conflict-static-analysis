package br.unb.cic.analysis.reachability;

import br.unb.cic.analysis.AbstractMergeConflictDefinition;
import br.unb.cic.analysis.IFDSDataFlowTransformer;
import br.unb.cic.analysis.SootWrapper;
import br.unb.cic.analysis.df.ReachDefinitionAnalysis;
import org.junit.Before;
import org.junit.Test;
import soot.*;
import soot.options.Options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IFDSTransformerTest {

    private ReachabilityAnalysis interproceduralSameClass;

    @Before
    public void configure() {
        G.reset();

        // Set Soot's internal classpath
        String classpath = "target/test-classes";
        String mainClass = "br.unb.cic.analysis.samples.IntraproceduralDataFlow";

        Options.v().set_soot_classpath(classpath);

        // Enable whole-program mode
        Options.v().set_whole_program(true);
        Options.v().set_app(true);

        // Call-graph options
        Options.v().setPhaseOption("cg", "safe-newinstance:true");
        Options.v().setPhaseOption("cg.cha", "enabled:false");

        // Enable SPARK call-graph construction
        Options.v().setPhaseOption("cg.spark", "enabled:true");
        Options.v().setPhaseOption("cg.spark", "verbose:true");
        Options.v().setPhaseOption("cg.spark", "on-fly-cg:true");

        Options.v().set_allow_phantom_refs(true);

        // Set the main class of the application to be analysed
        Options.v().set_main_class(mainClass);

        // Load the main class
        SootClass c = Scene.v().loadClass(mainClass, SootClass.BODIES);
        c.setApplicationClass();

        // Load the "main" method of the main class and set it as a Soot entry point
        SootMethod entryPoint = c.getMethodByName("foo");
        List<SootMethod> entryPoints = new ArrayList<SootMethod>();
        entryPoints.add(entryPoint);
        Scene.v().setEntryPoints(entryPoints);

        PackManager.v().getPack("wjtp").add(
                new Transform("wjtp.herosifds",
                new IFDSDataFlowTransformer()));

        //PackManager.v().allPacks().stream().forEach(p -> p.apply());

        String[] a = {};

        soot.Main.main(a);
        //FIXME: This solved it, but why?
        //SootWrapper.builder().withClassPath(classpath).addClass(mainClass).build().execute();

        //TODO: Plot CFG(?)
    }
    @Test
    public void testIFDSTransformerability() {

    }
}
