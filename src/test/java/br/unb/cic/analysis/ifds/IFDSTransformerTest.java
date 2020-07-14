package br.unb.cic.analysis.ifds;

import br.unb.cic.analysis.ifds.IFDSDataFlowTransformer;
import org.junit.Before;
import org.junit.Test;
import soot.*;
import soot.options.Options;

import java.util.ArrayList;
import java.util.List;

public class IFDSTransformerTest {

    private IFDSDataFlowTransformer transformer;

    @Before
    public void configure() {
        G.reset();

        transformer = new IFDSDataFlowTransformer();

        // Set Soot's internal classpath
        String classpath = "target/test-classes";
        String mainClass = "br.unb.cic.analysis.samples.IntraproceduralDataFlowMain";

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
        SootMethod entryPoint = c.getMethodByName("main");
        List<SootMethod> entryPoints = new ArrayList<>();
        entryPoints.add(entryPoint);
        Scene.v().setEntryPoints(entryPoints);

        PackManager.v().getPack("wjtp").add(
                new Transform("wjtp.herosifds",
                transformer));

        String[] a = {"-w"                          // whole program mode
                , "-allow-phantom-refs"        // allow phantom types
                , "-f", "J"                    // Jimple format
                , "-keep-line-number"          // keep line numbers
                , "-p", "jb", "optimize:false" // disable the optimizer
                , mainClass};

        soot.Main.main(a);
    }

    @Test
    public void testIFDSTransformerability() {
        // transformer ...
    }
}
