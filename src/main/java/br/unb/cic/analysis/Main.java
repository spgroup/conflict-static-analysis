package br.unb.cic.analysis;

import br.unb.cic.analysis.cd.CDAnalysisSemanticConflicts;
import br.unb.cic.analysis.cd.CDIntraProcedural;
import br.unb.cic.analysis.df.ConfluentAnalysis;
import br.unb.cic.analysis.df.ConfluentTaintedAnalysis;
import br.unb.cic.analysis.df.ReachDefinitionAnalysis;
import br.unb.cic.analysis.df.TaintedAnalysis;
import br.unb.cic.analysis.df.pessimistic.PessimisticTaintedAnalysis;
import br.unb.cic.analysis.dfp.DFPAnalysisSemanticConflicts;
import br.unb.cic.analysis.dfp.DFPInterProcedural;
import br.unb.cic.analysis.dfp.DFPIntraProcedural;
import br.unb.cic.analysis.io.AnalysisCsvExporter;
import br.unb.cic.analysis.io.DefaultReader;
import br.unb.cic.analysis.io.MergeConflictReader;
import br.unb.cic.analysis.io.PANotResolveCsvExporter;
import br.unb.cic.analysis.model.AnalysisRecord;
import br.unb.cic.analysis.model.Conflict;
import br.unb.cic.analysis.model.Statement;
import br.unb.cic.analysis.oa.OverrideAssignment;
import br.unb.cic.analysis.oa.OverrideAssignmentWithPointerAnalysis;
import br.unb.cic.analysis.oa.OverrideAssignmentWithoutPointerAnalysis;
import br.unb.cic.analysis.pdg.PDGAnalysisSemanticConflicts;
import br.unb.cic.analysis.pdg.PDGIntraProcedural;
import br.unb.cic.analysis.reachability.ReachabilityAnalysis;
import br.unb.cic.analysis.svfa.SVFAAnalysis;
import br.unb.cic.analysis.svfa.SVFAInterProcedural;
import br.unb.cic.analysis.svfa.SVFAIntraProcedural;
import br.unb.cic.analysis.svfa.confluence.ConfluenceConflict;
import br.unb.cic.analysis.svfa.confluence.DFPConfluenceAnalysis;
import br.unb.cic.diffclass.DiffClass;
import com.google.common.base.Stopwatch;
import org.apache.commons.cli.*;
import scala.collection.JavaConverters;
import soot.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {

    public static Stopwatch stopwatch;
    private Options options;
    private CommandLine cmd;
    private AbstractMergeConflictDefinition definition;
    private Set<String> targetClasses;
    private List<String> conflicts = new ArrayList<>();
    private List<String> JSONconflicts = new ArrayList<>();
    private ReachDefinitionAnalysis analysis;

    // Guards against double-export when shutdown hook fires after normal completion
    private static volatile boolean resultsExported = false;
    private static final Object exportLock = new Object();

    public static void main(String args[]) {
        Main m = new Main();
        try {
            m.createOptions();

            CommandLineParser parser = new DefaultParser();
            m.cmd = parser.parse(m.options, args);
            CommandLine cmd = m.cmd;
            String mode = "dataflow";
            if (cmd.hasOption("mode")) {
                mode = cmd.getOptionValue("mode");
            }

            // Register a shutdown hook so that partial results are written to out.json even
            // when
            // the process is terminated early (e.g., by the mining framework timeout via
            // SIGTERM on Unix).
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                synchronized (exportLock) {
                    if (!resultsExported) {
                        System.err.println("Shutdown detected. Exporting partial results to out.json...");
                        try {
                            m.exportResults();
                        } catch (Exception e) {
                            System.err.println("Error exporting partial results: " + e.getMessage());
                        }
                        resultsExported = true;
                    }
                }
            }));

            if (cmd.hasOption("repo") && cmd.hasOption("commit")) {
                DiffClass module = new DiffClass();
                module.getGitRepository(cmd.getOptionValue("repo"));
                module.diffAnalysis(cmd.getOptionValue("commit"));
                m.loadDefinitionFromDiffAnalysis(module);
            } else {
                m.loadDefinition(cmd.getOptionValue("csv"));
            }
            
            int depthLimit = Integer.parseInt(cmd.getOptionValue("depthLimit", "5"));
            m.definition.setDepthLimit(depthLimit);

            m.runAnalysis(mode, m.parseClassPath(cmd.getOptionValue("cp")));

            synchronized (exportLock) {
                if (!resultsExported) {
                    m.exportResults();
                    resultsExported = true;
                }
            }

        } catch (ParseException e) {
            System.out.println("Error: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java Main", m.options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportAnalysisRecord(long time) {
        try {
            AnalysisRecord record = AnalysisRecord.getInstance();
            record.setAnalysisExecutionTimeMs(time);
            new AnalysisCsvExporter().export(record, "AnalysisRecords.csv");
            System.out.println("AnalysisRecords.csv generated successfully.");
        } catch (IllegalStateException e) {
            System.err.println("AnalysisRecord not initialized. CSV not generated.");
        } catch (Exception e) {
            System.err.println("Error generating AnalysisRecords.csv: " + e.getMessage());
        }
    }

    private String parseClassPath(String cp) {
        File f = new File(cp);
        String res = cp;
        if (f.exists() && f.isDirectory()) {
            for (File file : f.listFiles()) {
                if (file.getName().endsWith(".jar")) {
                    res += ":";
                    res += file.getAbsolutePath();
                }
            }
        }
        return res;
    }

    private void exportResults() throws Exception {
        System.out.println(" Analysis results");
        System.out.println("----------------------------");

        if (conflicts.size() == 0) {
            System.out.println(" No conflicts detected");
            System.out.println("----------------------------");
            return;
        }

        // write results to out.txt
        System.out.println(" Number of conflicts: " + conflicts.size());
        final String out = "out.txt";
        final FileWriter fw = new FileWriter(out, true);
        conflicts.forEach(c -> {
            try {
                fw.write(c + "\n\n");
            } catch (Exception e) {
                System.out.println("error exporting the results " + e.getMessage());
            }
        });
        fw.close();
        System.out.println(" Results exported to " + out);

        // write results to out.json
        final String outJSON = "out.json";

        org.json.JSONArray allScenarios;
        try {
            String prevContent = new String(Files.readAllBytes(Paths.get(outJSON)));
            allScenarios = new org.json.JSONArray(prevContent);
        } catch (Exception e) {
            allScenarios = new org.json.JSONArray();
            System.out.println("Error getting the previous content of the JSON file " + e.getMessage());
        }

        if (!JSONconflicts.isEmpty()) {
            org.json.JSONArray scenarioConflicts = new org.json.JSONArray();
            for (String c : JSONconflicts) {
                try {
                    scenarioConflicts.put(new org.json.JSONObject(c));
                } catch (Exception e) {
                    System.out.println("error exporting the results " + e.getMessage());
                }
            }
            org.json.JSONObject scenario = new org.json.JSONObject();
            scenario.put("conflicts", scenarioConflicts);
            allScenarios.put(scenario);
        }

        try (FileWriter fwJSON = new FileWriter(outJSON)) {
            fwJSON.write(allScenarios.toString(2));
        }
        System.out.println(" JSON Results exported to " + outJSON);
        System.out.println("----------------------------");
    }

    private void createOptions() {
        options = new Options();
        Option classPathOption = Option.builder("cp").argName("class-path")
                .required().hasArg().desc("the classpath used in the analysis")
                .build();

        Option inputFileOption = Option.builder("csv").argName("csv")
                .hasArg().desc("the input csv files with the list of changes")
                .build();

        Option analysisOption = Option.builder("mode").argName("mode")
                .hasArg()
                .desc("analysis mode [data-flow, tainted, reachability, svfa-{interprocedural | intraprocedural}" +
                        ", svfa-confluence-{interprocedural | intraprocedural}, pessimistic-dataflow]")
                .build();

        Option repoOption = Option.builder("repo").argName("repo")
                .hasArg().desc("the path or url of git repository")
                .build();

        Option commitOption = Option.builder("commit").argName("commit")
                .hasArg().desc("the commit merge to analysis")
                .build();

        Option verboseOption = Option.builder("verbose").argName("verbose").hasArg().desc("run in the verbose mode")
                .build();

        Option recursiveOption = Option.builder("recursive").argName("recursive").hasArg()
                .desc("run using the recursive strategy for mapping sources and sinks")
                .build();

        Option depthLimitOption = Option.builder("depthLimit").argName("depthLimit").hasArg()
                .desc("sets the depth limit on accessing methods when performing Overriding Assignment " +
                        "Interprocedural analysis")
                .build();

        Option depthMethodsVisitedSVFAOption = Option.builder("printDepthSVFA").argName("printDepthSVFA").hasArg()
                .desc("sets depthMethodsVisited from SVFA")
                .build();

        Option entrypointsOption = Option.builder("entrypoints").argName("entrypoints").hasArg()
                .desc("entrypoints")
                .build();

        Option callGraphOption = Option.builder("cg").argName("cg")
                .hasArg()
                .desc("call graph algorithm [CHA, RTA, VTA, SPARK]")
                .build();

        options.addOption(classPathOption);
        options.addOption(inputFileOption);
        options.addOption(analysisOption);
        options.addOption(repoOption);
        options.addOption(commitOption);
        options.addOption(verboseOption);
        options.addOption(recursiveOption);
        options.addOption(depthLimitOption);
        options.addOption(depthMethodsVisitedSVFAOption);
        options.addOption(entrypointsOption);
        options.addOption(callGraphOption);
    }

    private void runAnalysis(String mode, String classpath) {
        switch (mode) {
            case "svfa-interprocedural":
                runSparseValueFlowAnalysis(classpath, true);
                break;
            case "svfa-intraprocedural":
                runSparseValueFlowAnalysis(classpath, false);
                break;
            case "dfp-confluence-interprocedural":
                runDFPConfluenceAnalysis(classpath, true);
                break;
            case "dfp-confluence-intraprocedural":
                runDFPConfluenceAnalysis(classpath, false);
                break;
            case "reachability":
                runReachabilityAnalysis(classpath);
                break;
            case "ioa":
                runOverrideAssignmentAnalysis(classpath, true, AnalysisType.WITH_POINTER_ANALYSIS);
                break;
            case "ioa-without-pa":
                runOverrideAssignmentAnalysis(classpath, true, AnalysisType.WITHOUT_POINTER_ANALYSIS);
                break;
            case "oa":
                runOverrideAssignmentAnalysis(classpath, false, AnalysisType.WITH_POINTER_ANALYSIS);
                break;
            case "oa-without-pa":
                runOverrideAssignmentAnalysis(classpath, false, AnalysisType.WITHOUT_POINTER_ANALYSIS);
                break;
            case "dfp-intra":
                runDFPAnalysis(classpath, false);
                break;
            case "dfp-inter":
                runDFPAnalysis(classpath, true);
                break;
            case "pdg":
                runPDGAnalysis(classpath, true);
                break;
            case "cd":
                runCDAnalysis(classpath, true);
                break;
            case "pdg-e":
                runPDGAnalysis(classpath, false);
                break;
            case "cd-e":
                runCDAnalysis(classpath, false);
                break;
            case "pessimistic-dataflow":
                runPessimisticDataFlowAnalysis(classpath);
                break;
            default:
                runDataFlowAnalysis(classpath, mode);
        }
    }

    private void addConflictText(String conflict) {
        conflicts.add(conflict);
        System.out.println("[CONFLICT_FOUND]");
    }

    private void runPessimisticDataFlowAnalysis(String classpath) {
        PackManager.v().getPack("jtp").add(
                new Transform("jtp.analysis", new BodyTransformer() {
                    @Override
                    protected void internalTransform(Body body, String s, Map<String, String> map) {
                        PessimisticTaintedAnalysis analysis = new PessimisticTaintedAnalysis(body, definition);

                        analysis.getConflicts().stream()
                                .map(Conflict::toString)
                                .forEach(Main.this::addConflictText);

                        analysis.getConflicts().stream()
                                .map(Conflict::toJSON)
                                .forEach(JSONconflicts::add);
                    }
                }));
        SootWrapper.builder()
                .withClassPath(classpath)
                .addClass(targetClasses.stream().collect(Collectors.joining(" ")))
                .build()
                .execute();

    }

    private void runDataFlowAnalysis(String classpath, String mode) {
        PackManager.v().getPack("jtp").add(
                new Transform("jtp.analysis", new BodyTransformer() {
                    @Override
                    protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
                        switch (mode) {
                            case "dataflow":
                                analysis = new ReachDefinitionAnalysis(body, definition);
                                break;
                            case "tainted":
                                analysis = new TaintedAnalysis(body, definition);
                            case "confluence":
                                analysis = new ConfluentAnalysis(body, definition);
                                break;
                            case "confluence-tainted":
                                analysis = new ConfluentTaintedAnalysis(body, definition);
                                break;
                            default: {
                                System.out.println("Error: " + "invalid mode " + mode);
                                System.exit(-1);
                            }
                        }
                    }
                }));
        SootWrapper.builder()
                .withClassPath(classpath)
                .addClass(targetClasses.stream().collect(Collectors.joining(" ")))
                .build()
                .execute();
        if (analysis != null) {
            analysis.getConflicts().stream().map(c -> c.toString()).forEach(this::addConflictText);
            analysis.getConflicts().stream().map(c -> c.toJSON()).forEach(JSONconflicts::add);
        }
    }

    private void runOverrideAssignmentAnalysis(String classpath, Boolean interprocedural, AnalysisType analysisType) {
        int depthLimit = Integer.parseInt(cmd.getOptionValue("depthLimit", "5"));
        List<String> entrypoints = convertStringEntrypointsToList(cmd.getOptionValue("entrypoints"));

        stopwatch = Stopwatch.createStarted();
        String modeLabel = interprocedural ? "Inter" : "Intra";

        OverrideAssignment overrideAssignment = buildOverrideAssignment(analysisType, depthLimit, interprocedural,
                entrypoints, classpath);

        overrideAssignment.configureEntryPoints();

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.analysis", overrideAssignment));
        System.out.println("Depth limit: " + overrideAssignment.getDepthLimit());

        saveExecutionTime("Configure Soot OA " + modeLabel);

        SootWrapper.applyPackages();

        overrideAssignment.getConflicts().stream()
                .map(Object::toString)
                .forEach(this::addConflictText);

        overrideAssignment.getFilteredConflicts().stream()
                .map(c -> c.toJSON())
                .forEach(JSONconflicts::add);

        saveExecutionTime("Time to perform OA " + modeLabel);

        int visitedMethods = overrideAssignment.getVisitedMethodsCount();
        System.out.println("OA " + modeLabel + " Visited methods: " + visitedMethods);

        saveVisitedMethods("OA " + modeLabel, String.valueOf(visitedMethods));
        saveConflictsLog("OA " + modeLabel, conflicts);

        long time = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        new PANotResolveCsvExporter().export(overrideAssignment.getPointerAnalysisMissingRefs(), "PANotResolve.csv");

        AnalysisRecord.getInstance().setAnalysisExecutionTimeMs(time);
        new AnalysisCsvExporter().export(AnalysisRecord.getInstance(), "AnalysisRecords.csv");
    }

    private OverrideAssignment buildOverrideAssignment(
            AnalysisType type,
            int depthLimit,
            boolean interprocedural,
            List<String> entrypoints,
            String classpath) {
        String cg = cmd.getOptionValue(
                "cg",
                type.equals(AnalysisType.WITH_POINTER_ANALYSIS) ? "SPARK" : "CHA");
        OverrideAssignment overrideAssignment;
        switch (type) {
            case WITH_POINTER_ANALYSIS:
                overrideAssignment = new OverrideAssignmentWithPointerAnalysis(definition, depthLimit, interprocedural,
                        entrypoints, classpath);
                SootWrapper.configureSootOptionsToRunInterproceduralOverrideAssignmentAnalysis(classpath, cg);
                return overrideAssignment;
            case WITHOUT_POINTER_ANALYSIS:
                overrideAssignment = new OverrideAssignmentWithoutPointerAnalysis(definition, depthLimit,
                        interprocedural, entrypoints, classpath);
                SootWrapper.configureSootOptionsToRunInterproceduralOverrideAssignmentAnalysis(classpath, cg);
                return overrideAssignment;
            default:
                throw new IllegalArgumentException("Unknown analysis type: " + type);
        }
    }

    /*
     * After discussing this algorithm with the researchers at
     * UFPE, we decided that we should not support this analysis
     * any more. It might lead to a huge number of false-positives.
     */
    @Deprecated
    private void runReachabilityAnalysis(String classpath) {
        ReachabilityAnalysis analysis = new ReachabilityAnalysis(definition);

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.analysis", analysis));
        soot.options.Options.v().setPhaseOption("cg.spark", "on");
        soot.options.Options.v().setPhaseOption("cg.spark", "verbose:true");

        SootWrapper.builder()
                .withClassPath(classpath)
                .addClass(targetClasses.stream().collect(Collectors.joining(" ")))
                .build()
                .execute();

        analysis.getConflicts().stream().map(c -> c.toString()).forEach(this::addConflictText);
        analysis.getConflicts().stream().map(c -> c.toJSON()).forEach(JSONconflicts::add);
    }

    private void runPDGAnalysis(String classpath, Boolean omitExceptingUnitEdges) {
        List<String> entrypoints = convertStringEntrypointsToList(cmd.getOptionValue("entrypoints"));
        PDGAnalysisSemanticConflicts analysis = new PDGIntraProcedural(classpath, definition, entrypoints);
        CDAnalysisSemanticConflicts cd = new CDIntraProcedural(classpath, definition, entrypoints);
        cd.setOmitExceptingUnitEdges(omitExceptingUnitEdges);
        DFPAnalysisSemanticConflicts dfp = new DFPIntraProcedural(classpath, definition, entrypoints);

        String type_analysis = omitExceptingUnitEdges ? "" : "e";

        stopwatch = Stopwatch.createStarted();
        analysis.configureSoot();
        saveExecutionTime("Configure Soot PDG" + type_analysis);

        stopwatch = Stopwatch.createStarted();

        analysis.buildPDG(cd, dfp);

        JavaConverters.asJavaCollection(analysis.reportConflictsPDG())
                .stream()
                .map(p -> formatConflict(p.toString()))
                .forEach(this::addConflictText);

        saveExecutionTime("Time to perform PDG" + type_analysis);

        System.out.println("CONFLICTS: " + conflicts.toString());

        saveConflictsLog("PDG" + type_analysis, conflicts);
    }

    private void runDFPAnalysis(String classpath, Boolean interprocedural) {
        int depthLimit = Integer.parseInt(cmd.getOptionValue("depthLimit", "5"));
        List<String> entrypoints = convertStringEntrypointsToList(cmd.getOptionValue("entrypoints"));

        definition.setRecursiveMode(cmd.hasOption("recursive"));
        DFPAnalysisSemanticConflicts analysis = interprocedural
                ? new DFPInterProcedural(classpath, definition, depthLimit, entrypoints)
                : new DFPIntraProcedural(classpath, definition, entrypoints);

        // boolean depthMethodsVisited =
        // Boolean.parseBoolean(cmd.getOptionValue("printDepthSVFA", "false"));
        String cg = cmd.getOptionValue("cg", "SPARK");
        analysis.setCallGraph(cg);
        // analysis.setPrintDepthVisitedMethods(depthMethodsVisited);
        String type_analysis = interprocedural ? "Inter" : "Intra";
        stopwatch = Stopwatch.createStarted();

        analysis.configureSoot();

        saveExecutionTime("Configure Soot DFP " + type_analysis);

        stopwatch = Stopwatch.createStarted();
        System.out.println("CallGraph: " + analysis.callGraph());
        analysis.buildDFP();

        try {
            JavaConverters.asJavaCollection(analysis.reportConflictsSVG())
                    .stream()
                    .map(p -> formatConflict(p.toString()))
                    .forEach(this::addConflictText);

            JavaConverters.asJavaCollection(analysis.reportConflictsSVGJSON()).forEach(JSONconflicts::add);
        } catch (Exception e) {
            System.err.println("Error reporting conflicts: " + e.getMessage());
        }

        saveExecutionTime("Time to perform DFP " + type_analysis);
        System.out.println("Depth limit: " + analysis.getDepthLimit());

        System.out.println("Visited methods: " + analysis.getNumberVisitedMethods());
        // System.out.print("CONFLICTS: ");
        List<String> conflicts_report = new ArrayList<>();
        try {
            conflicts_report = analysis.reportDFConflicts();
        } catch (Exception e) {
            System.err.println("Error generating DFP conflicts report: " + e.getMessage());
        }

        // conflicts_report.addAll(conflicts);

        saveVisitedMethods("DFP " + type_analysis, (analysis.getNumberVisitedMethods() + ","
                + analysis.svg().graph().size() + "," + analysis.svg().edges().size()));

        saveConflictsLog("DFP " + type_analysis, conflicts_report);

        long time = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        new PANotResolveCsvExporter().export(analysis.getPointerAnalysisMissingRefs(), "PANotResolve.csv");

        analysis.createAnalysisReportLog(SootWrapper.countEdges(Scene.v().getCallGraph()),
                scala.collection.JavaConverters.seqAsJavaList(analysis.getAnalysisEntryPoints()));

        exportAnalysisRecord(time);
    }

    private void runCDAnalysis(String classpath, Boolean omitExceptingUnitEdges) {
        List<String> entrypoints = convertStringEntrypointsToList(cmd.getOptionValue("entrypoints"));
        CDAnalysisSemanticConflicts analysis = new CDIntraProcedural(classpath, definition, entrypoints);
        String type_analysis = omitExceptingUnitEdges ? "" : "e";

        analysis.setOmitExceptingUnitEdges(omitExceptingUnitEdges);
        stopwatch = Stopwatch.createStarted();
        analysis.configureSoot();
        saveExecutionTime("Configure Soot CD" + type_analysis);

        stopwatch = Stopwatch.createStarted();

        analysis.buildCD();

        JavaConverters.asJavaCollection(analysis.reportConflictsCD())
                .stream()
                .map(p -> formatConflict(p.toString()))
                .forEach(this::addConflictText);

        saveExecutionTime("Time to perform CD" + type_analysis);

        System.out.println("CONFLICTS: " + conflicts.toString());

        saveConflictsLog("CD" + type_analysis, conflicts);
    }

    private void runSparseValueFlowAnalysis(String classpath, boolean interprocedural) {
        List<String> entrypoints = convertStringEntrypointsToList(cmd.getOptionValue("entrypoints"));
        definition.setRecursiveMode(cmd.hasOption("recursive"));

        SVFAAnalysis analysis = interprocedural
                ? new SVFAInterProcedural(classpath, definition, entrypoints)
                : new SVFAIntraProcedural(classpath, definition, entrypoints);

        boolean depthMethodsVisited = Boolean.parseBoolean(cmd.getOptionValue("printDepthSVFA", "false"));
        analysis.setPrintDepthVisitedMethods(depthMethodsVisited);

        String type_analysis = interprocedural ? "Inter" : "Intra";

        stopwatch = Stopwatch.createStarted();
        analysis.configureSoot();
        saveExecutionTime("Configure Soot DF " + type_analysis);

        stopwatch = Stopwatch.createStarted();

        analysis.buildSparseValueFlowGraph();

        try {
            JavaConverters.asJavaCollection(analysis.reportConflictsSVG())
                    .stream()
                    .map(p -> formatConflict(p.toString()))
                    .forEach(this::addConflictText);

            JavaConverters.asJavaCollection(analysis.reportConflictsSVGJSON()).forEach(JSONconflicts::add);
        } catch (Exception e) {
            System.err.println("Error reporting SVFA conflicts: " + e.getMessage());
        }

        saveExecutionTime("Time to perform DF " + type_analysis);

        System.out.println("CONFLICTS: " + conflicts.toString());

        saveConflictsLog("DF " + type_analysis, conflicts);

        long time = stopwatch.elapsed(TimeUnit.MILLISECONDS);

        exportAnalysisRecord(time);
    }

    private void runDFPConfluenceAnalysis(String classpath, boolean interprocedural) {
        int depthLimit = Integer.parseInt(cmd.getOptionValue("depthLimit", "5"));
        List<String> entrypoints = convertStringEntrypointsToList(cmd.getOptionValue("entrypoints"));
        String type_analysis = interprocedural ? "Inter" : "Intra";

        definition.setRecursiveMode(cmd.hasOption("recursive"));
        DFPConfluenceAnalysis analysis = new DFPConfluenceAnalysis(classpath, this.definition, interprocedural,
                depthLimit, entrypoints);
        boolean depthMethodsVisited = Boolean.parseBoolean(cmd.getOptionValue("printDepthSVFA", "false"));
        String cg = cmd.getOptionValue("cg", "SPARK");
        System.out.println("Depth limit: " + analysis.getDepthLimit());
        analysis.execute(depthMethodsVisited, cg);

        try {
            analysis.getConfluentConflicts(false)
                    .stream()
                    .map(p -> formatConflict(p.toString()))
                    .forEach(this::addConflictText);
            analysis.getConfluentConflicts(true)
                    .stream()
                    .map(ConfluenceConflict::toJSON)
                    .forEach(JSONconflicts::add);
        } catch (Exception e) {
            System.err.println("Error reporting confluence conflicts: " + e.getMessage());
        }

        // System.out.println("CONFLICTS: " + conflicts.toString());
        List<String> conflicts_report = new ArrayList<>();
        try {
            conflicts_report = analysis.reportConflictsConfluence();
        } catch (Exception e) {
            System.err.println("Error generating confluence conflicts report: " + e.getMessage());
        }

        saveVisitedMethods("Confluence " + type_analysis,
                (analysis.getVisitedMethods() + "," + analysis.getGraphSize()));
        saveConflictsLog("Confluence " + type_analysis, conflicts_report);

        long time = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        new PANotResolveCsvExporter().export(analysis.getPointerAnalysisMissingRefs(), "PANotResolve.csv");

        exportAnalysisRecord(time);
    }

    private void loadDefinition(String filePath) throws Exception {
        MergeConflictReader reader = new DefaultReader(filePath);
        List<ClassChangeDefinition> changes = reader.read();
        Map<String, List<Integer>> sourceDefs = new HashMap<>();
        Map<String, List<Integer>> sinkDefs = new HashMap<>();
        targetClasses = new HashSet<>();
        for (ClassChangeDefinition change : changes) {
            if (change.getType().equals(Statement.Type.SOURCE)) {
                addChange(sourceDefs, change);
            } else {
                addChange(sinkDefs, change);
            }
            targetClasses.add(change.getClassName());
        }
        definition = new AbstractMergeConflictDefinition() {
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

    private void loadDefinitionFromDiffAnalysis(DiffClass module) {
        ArrayList<Entry<String, Integer>> sourceClasses = module.getSourceModifiedClasses();
        ArrayList<Entry<String, Integer>> sinkClasses = module.getSinkModifiedClasses();
        Map<String, List<Integer>> sourceDefs = new HashMap<>();
        Map<String, List<Integer>> sinkDefs = new HashMap<>();
        targetClasses = new HashSet<>();
        for (Entry<String, Integer> change : sourceClasses) {
            addChangeFromDiffAnalysis(sourceDefs, change);
            targetClasses.add(change.getKey());
        }
        for (Entry<String, Integer> change : sinkClasses) {
            addChangeFromDiffAnalysis(sinkDefs, change);
            targetClasses.add(change.getKey());
        }

        definition = new AbstractMergeConflictDefinition() {
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

    private void addChangeFromDiffAnalysis(Map<String, List<Integer>> map, Entry<String, Integer> change) {
        if (map.containsKey(change.getKey())) {
            map.get(change.getKey()).add(change.getValue());
        } else {
            List<Integer> lines = new ArrayList<>();
            lines.add(change.getValue());
            map.put(change.getKey(), lines);
        }
    }

    public void saveExecutionTime(String description) {

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

    public void saveVisitedMethods(String description, String visited_methods) {
        try {
            FileWriter myWriter = new FileWriter("visited_methods.txt", true);
            myWriter.write(description + "; " + visited_methods + "\n");
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void saveConflictsLog(String description, List<String> logs) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter("conflicts_log.txt", true))) {

            writer.write("==== " + description + " ====");
            writer.newLine();

            for (String log : logs) {
                writer.write(log);
                writer.newLine();
            }

            writer.newLine(); // separador
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String formatConflict(String p) {
        if (p == null || p.isEmpty()) {
            return p;
        }

        // Verificar se a substituição é necessária antes de executar
        if (!p.contains("), Node")) {
            return p;
        }

        // Para strings muito grandes, usar StringBuilder
        // THRESHOLD: 2000 caracteres (conservador e seguro)
        // - Abaixo disso: String.replace() é 3-4x mais rápido
        // - Acima disso: StringBuilder previne OutOfMemoryError
        if (p.length() > 2000) {
            StringBuilder sb = new StringBuilder(p.length() + 100);
            int index = 0;
            int pos;

            while ((pos = p.indexOf("), Node", index)) != -1) {
                sb.append(p, index, pos);
                sb.append(") => Node");
                index = pos + 7; // length of "), Node"
            }
            sb.append(p, index, p.length());
            return sb.toString();
        }

        return p.replace("), Node", ") => Node");
    }

    private List<String> convertStringEntrypointsToList(String str) {
        if (str == null) {
            return Collections.emptyList();
        }

        String trimmedStr = str.substring(1, str.length() - 1);
        String[] elements = trimmedStr.split(", ");

        List<String> entrypointsList = new ArrayList<>();
        for (String element : elements) {
            entrypointsList.add(element);
        }

        return entrypointsList;
    }

    public enum AnalysisType {
        WITH_POINTER_ANALYSIS,
        WITHOUT_POINTER_ANALYSIS,
    }
}
