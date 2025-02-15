package br.unb.cic.analysis;

import soot.PackManager;
import soot.Scene;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.options.Options;

import java.io.File;
import java.util.*;

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

    public static void configureSootOptionsToRunInterproceduralOverrideAssignmentAnalysis(String classpath) {
        configureSootOptionsToRunInterproceduralOverrideAssignmentAnalysis(classpath, true);
    }

    public static void configureSootOptionsToRunInterproceduralOverrideAssignmentAnalysis(String classpath, boolean usePointsToAnalysis) {
        List<String> classes = Collections.singletonList(classpath);

        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(soot.options.Options.output_format_jimple);
        Options.v().set_whole_program(true);
        Options.v().set_process_dir(classes);
        Options.v().set_full_resolver(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_include(getIncludeList());
        //Options.v().set_exclude(Arrays.asList("java.lang.*","javax.*", "com.sun.*", "com.metamx.common.*", "com.netflix.curator.*", "com.google.*", "kafka.*", "org.*", "scala.*"));
        //Options.v().set_exclude(Arrays.asList( "org.*",  "com.google.*")); // "scala.*",
        // Options.v().set_no_bodies_for_excluded(true);

        // JAVA 8
        if (getJavaVersion() < 9) {
            Options.v().set_prepend_classpath(true);
            Options.v().set_soot_classpath(classpath + File.pathSeparator + pathToJCE() + File.pathSeparator + pathToRT());
        }
        // JAVA VERSION 9 && IS A CLASSPATH PROJECT
        else if (getJavaVersion() >= 9) {
            Options.v().set_soot_classpath("VIRTUAL_FS_FOR_JDK" + File.pathSeparator + classpath);
        }
        //Options.v().setPhaseOption("jb.ls", "off"); // remove x = 1; x#2 = 2
        Options.v().setPhaseOption("jb", "use-original-names:true");

        enableCallGraph(usePointsToAnalysis);
        //enableCHACallGraph();

        Scene.v().loadNecessaryClasses();
    }

    public static void enableCallGraph() {
        enableCallGraph(true);
    }

    public static void enableCallGraph(boolean usePointsToAnalysis) {
        System.out.println("CG configuration init.");

        if (usePointsToAnalysis) {
            //enableRtaCallGraph();
            enableSparkCallGraph();
            //enableVtaCallGraph();
        } else {
            // Configurações para análise conservadora (CHA)
            //enableCHACallGraph();

            Options.v().setPhaseOption("cg.cha", "enabled:true");
            Options.v().setPhaseOption("cg.cha", "verbose:true");
            Options.v().setPhaseOption("cg.cha", "apponly:true");

            //Options.v().setPhaseOption("cg.spark", "on");
            // Options.v().setPhaseOption("cg.spark", "rta:true");


            // Ativa CHA para considerar todas as possíveis implementações
        }
        System.out.println("CG configuration completed.");
    }

    private static void enableCHACallGraph() {
        CHATransformer.v().transform();
    }

    private static void enableVtaCallGraph() {
        Options.v().setPhaseOption("cg", "vta");

    }

    private static void enableRtaCallGraph() {
        Options.v().setPhaseOption("cg", "rta");

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

        // Configurações para análise de points-to precisa
        //Options.v().setPhaseOption("cg.spark", "on"); // Ativa o Spark
        Options.v().setPhaseOption("cg.spark", "enabled:true"); // Habilita o Spark
    }

    private static List<String> configurePackagesWithCallGraph() {
        List<String> packages = new ArrayList<String>();
        packages.add("cg");
        packages.add("wjtp");
        return packages;
    }

    public static void applyPackages() {
        List<String> packages = configurePackagesWithCallGraph();

        for (String p : packages) {
            System.out.println("Applying package: " + p);

            try {
                PackManager.v().getPack(p).apply(); //.runPacks(); //
                System.out.println("Successfully applied package: " + p);
            } catch (Exception e) {
                System.err.println("Error applying package: " + p);
                e.printStackTrace();
            }
        }
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
            if (classes.isEmpty() || classPath.isEmpty()) {
                throw new RuntimeException("You should only call the build method " +
                        "after setting the class path and adding at least " +
                        "one class.");
            }
            return new SootWrapper(classPath, classes);
        }
    }

    public static String pathToJCE() {
        String javaHome = System.getProperty("java.home");
        File jreDir = new File(javaHome, "jre");
        if (jreDir.exists() && jreDir.isDirectory()) {
            return jreDir.getPath() + File.separator + "lib" + File.separator + "jce.jar";
        } else {
            return javaHome + File.separator + "lib" + File.separator + "jce.jar";
        }
    }

    public static String pathToRT() {
        String javaHome = System.getProperty("java.home");
        File jreDir = new File(javaHome, "jre");
        if (jreDir.exists() && jreDir.isDirectory()) {
            return jreDir.getPath() + File.separator + "lib" + File.separator + "rt.jar";
        } else {
            return javaHome + File.separator + "lib" + File.separator + "rt.jar";
        }
    }

    public static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

}
