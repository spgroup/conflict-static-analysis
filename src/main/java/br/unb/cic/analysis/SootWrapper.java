package br.unb.cic.analysis;

import com.google.common.base.Stopwatch;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * A fluent API for executing the soot framework
 * in the context of the conflict static
 * analysis tool.
 */
public class SootWrapper {
    static CallGraph sparkCG, chaCG;
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
        G.reset();
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
        //Options.v().set_no_bodies_for_excluded(true);

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

        Scene.v().loadNecessaryClasses();
        //applyPackage("cg");

    }

    public static int countEdges(CallGraph cg) {
        int count = 0;
        Iterator<Edge> it = cg.iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
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
            enableCHACallGraph();
        }
        System.out.println("CG configuration completed.");
    }

    private static void enableCHACallGraph() {
        Options.v().setPhaseOption("cg.cha", "enabled:true");
    }

    private static void enableSparkCallGraph() {
        Options.v().setPhaseOption("cg.spark", "on");
    }


    private static void enableVtaCallGraph() {
        Options.v().setPhaseOption("cg", "vta");
    }

    private static void enableRtaCallGraph() {
        Options.v().setPhaseOption("cg", "rta");
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
            applyPackage(p);
        }
    }

    public static void applyPackage(String p) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        System.out.println("Applying package: " + p);
        stopwatch = Stopwatch.createStarted();
        try {
            PackManager.v().getPack(p).apply();
            //System.out.println("Successfully applied package: " + p);
        } catch (Exception e) {
            System.err.println("Error applying package: " + p);
            e.printStackTrace();
        } finally {
            saveExecutionTime("Successfully applied package: " + p, stopwatch);
        }
    }

    public static void saveExecutionTime(String description, Stopwatch stopwatch) {

        NumberFormat formatter = new DecimalFormat("#0.00000");

        long time = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        try {
            FileWriter myWriter = new FileWriter("time.txt", true);
            myWriter.write(description + ";" + formatter.format(time / 1000d) + "\n");
            System.out.println(description + " " + formatter.format(time / 1000d));
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
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

    public static CallGraph getSparkCG() {
        return sparkCG;
    }

    public static void setSparkCG(CallGraph sparkCG) {
        SootWrapper.sparkCG = sparkCG;
    }

    public static CallGraph getChaCG() {
        return chaCG;
    }

    public static void setChaCG(CallGraph chaCG) {
        SootWrapper.chaCG = chaCG;
    }

    public static Main.AnalysisType getAnalysisType() {
        CallGraph sparkCG = getSparkCG();
        CallGraph chaCG = getChaCG();

        if (sparkCG != null && chaCG == null) {
            return Main.AnalysisType.WITH_POINTER_ANALYSIS;
        } else if (sparkCG == null && chaCG != null) {
            return Main.AnalysisType.WITHOUT_POINTER_ANALYSIS;
        } else if (sparkCG != null && chaCG != null) {
            return Main.AnalysisType.HYBRID_POINTER_ANALYSIS;
        } else {
            throw new IllegalStateException("Nenhum grafo de chamadas disponível.");
        }
    }

    public static CallGraph getCallGraphForAnalysisType(CallGraph currentGraph) {
        Main.AnalysisType analysisType = SootWrapper.getAnalysisType();

        if (analysisType.equals(Main.AnalysisType.WITHOUT_POINTER_ANALYSIS) ||
                (analysisType.equals(Main.AnalysisType.HYBRID_POINTER_ANALYSIS) && currentGraph == SootWrapper.getSparkCG())) {
            return SootWrapper.getChaCG();
        }

        return SootWrapper.getSparkCG();
    }
}

