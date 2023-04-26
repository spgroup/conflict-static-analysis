package br.unb.cic.analysis;

import soot.PackManager;
import soot.Scene;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CHATransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A fluent API for executing the soot framework
 * in the context of the conflict static
 * analysis tool.
 */
public class SootWrapper {

    private String classPath;
    private String classes;

    private SootWrapper(String classPath, String classes) {
        this.classPath = classPath;
        this.classes = classes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void execute() {
        soot.Main.main(new String[]{"-w"                          // whole program mode
                , "-allow-phantom-refs"        // allow phantom types
                , "-f", "J"                    // Jimple format
                , "-keep-line-number"          // keep line numbers
                , "-p", "jb", "optimize:false" // disable the optimizer
                , "-p", "jb", "use-original-names:true" // enable original names
                , "-cp", classPath             // soot class path
                , classes});                   // set of classes
    }

    private static List<String> getIncludeList() {
        //"java.lang.*, java.util.*"
        List<String> stringList = new ArrayList<String>(Arrays.asList("")); // java.util.* java.util.HashMap
        return stringList;
    }

    public static void configureSootOptionsToRunInterproceduralOverrideAssignmentAnalysis(List<String> classpath) {
        soot.options.Options.v().set_no_bodies_for_excluded(true);
        soot.options.Options.v().set_allow_phantom_refs(true);
        soot.options.Options.v().set_output_format(soot.options.Options.output_format_jimple);
        soot.options.Options.v().set_whole_program(true);
        soot.options.Options.v().set_process_dir(classpath);
        soot.options.Options.v().set_full_resolver(true);
        soot.options.Options.v().set_keep_line_number(true);
        soot.options.Options.v().set_prepend_classpath(false);
        soot.options.Options.v().set_include(getIncludeList());
        //Options.v().setPhaseOption("cg.spark", "on");
        //Options.v().setPhaseOption("cg.spark", "verbose:true");
        soot.options.Options.v().setPhaseOption("cg.spark", "enabled:true");
        soot.options.Options.v().setPhaseOption("jb", "use-original-names:true");

        Scene.v().loadNecessaryClasses();

        enableSparkCallGraph();
        //enableCHACallGraph();
    }

    private static void enableSparkCallGraph() {
        //Enable Spark
        HashMap<String, String> opt = new HashMap<String, String>();
        //opt.put("propagator","worklist");
        //opt.put("simple-edges-bidirectional","false");
        opt.put("on-fly-cg", "true");
        //opt.put("set-impl","double");
        //opt.put("double-set-old","hybrid");
        //opt.put("double-set-new","hybrid");
        //opt.put("pre_jimplify", "true");
        SparkTransformer.v().transform("", opt);
        soot.options.Options.v().setPhaseOption("cg.spark", "enable:true");
    }

    private static void enableCHACallGraph() {
        CHATransformer.v().transform();
    }

    private static List<String> configurePackagesWithCallGraph() {
        List<String> packages = new ArrayList<String>();
        packages.add("cg");
        packages.add("wjtp");
        return packages;
    }

    public static void applyPackages() {
        configurePackagesWithCallGraph().forEach(p -> {
            PackManager.v().getPack(p).apply();
        });
    }

    public static class Builder {
        private String classPath;
        private String classes;


        public Builder() {
            classPath = "";
            classes = "";
        }

        /**
         * Set the class path
         */
        public Builder withClassPath(String classPath) {
            this.classPath = classPath;
            return this;
        }

        /**
         * Add a class as a target of the soot
         * analysis
         */
        public Builder addClass(String aClass) {
            if(classes.isEmpty()) {
                classes += aClass;
            }
            else {
                classes += " " + aClass;
            }
            return this;
        }

        public SootWrapper build() {
            if(classes.isEmpty() || classPath.isEmpty()) {
                throw new RuntimeException("You should only call the build method " +
                        "after setting the class path and adding at least " +
                        "one class.");
            }
            return new SootWrapper(classPath, classes);
        }
    }
}
