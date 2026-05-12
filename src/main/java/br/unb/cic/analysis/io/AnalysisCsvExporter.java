package br.unb.cic.analysis.io;

import br.unb.cic.analysis.model.AnalysisRecord;
import soot.SootMethod;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalysisCsvExporter {

    public void export(List<AnalysisRecord> records, String filePath) {
        File file = new File(filePath);
        boolean isNewFile = !file.exists() || file.length() == 0;

        try (FileWriter writer = new FileWriter(file, true)) {
            // Cabeçalho
            if (isNewFile) {
                writer.append("CallGraphAlgorithm;CallGraphEdgeCount;DepthLimit;VisitedMethodsCount;AnalysisType;CallGraphBuildTimeMs;AnalysisExecutionTimeMs;MemoryUsedMB;CallGraphEntryPointsCount;AnalysisEntryPointsCount;CallGraphEntryPoints;AnalysisEntryPoints;\n");
            }

            for (AnalysisRecord record : records) {
                writeRecord(writer, record);
            }

            writer.flush();
            System.out.println("OAAnalysis CSV export successful!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void export(AnalysisRecord record, String filePath) {
        export(Collections.singletonList(record), filePath);
    }

    private void writeRecord(FileWriter writer, AnalysisRecord record) throws IOException {
        long cgTime = 0L;
        Map<String, Long> times = record.getCallGraphBuildTimeMs();
        if (times != null) {
            cgTime = times.values().stream().mapToLong(Long::longValue).sum();
        }

        String cgEntryPoints = record.getCallGraphEntryPoint() != null ?
                record.getCallGraphEntryPoint().stream().map(SootMethod::getSignature).limit(50).collect(Collectors.joining(" | ")) : "";
        String analysisEntryPoints = record.getAnalysisEntryPoint() != null ?
                record.getAnalysisEntryPoint().stream().map(SootMethod::getSignature).limit(50).collect(Collectors.joining(" | ")) : "";

        writer.append(record.getCallGraphAlgorithm() != null ? record.getCallGraphAlgorithm().name() : "").append(";");
        writer.append(String.valueOf(record.getCallGraphEdgeCount())).append(";");
        writer.append(String.valueOf(record.getDepthLimit())).append(";");
        writer.append(String.valueOf(record.getVisitedMethodsCount())).append(";");
        writer.append(record.getAnalysisType() != null ? record.getAnalysisType().name() : "").append(";");
        writer.append(String.valueOf(cgTime)).append(";");
        writer.append(String.valueOf(record.getAnalysisExecutionTimeMs())).append(";");
        writer.append(String.valueOf(record.getUsedMemoryMb())).append(";");
        writer.append(String.valueOf(record.getCallGraphEntryPoint() != null ? record.getCallGraphEntryPoint().size() : 0)).append(";");
        writer.append(String.valueOf(record.getAnalysisEntryPoint() != null ? record.getAnalysisEntryPoint().size() : 0)).append(";");
        writer.append(sanitize(cgEntryPoints)).append(";");
        writer.append(sanitize(analysisEntryPoints)).append("\n");
    }

    private String sanitize(String value) {
        if (value == null) return "";
        // Remove quebras de linha
        String sanitized = value.replace("\n", " ")
                .replace("\r", " ");
        // Escapa aspas duplas
        sanitized = sanitized.replace("\"", "\"\"");
        // Se tiver ; ou aspas, envolve o campo em aspas duplas
        if (sanitized.contains(";") || sanitized.contains("\"") || sanitized.contains(" ")) {
            sanitized = "\"" + sanitized + "\"";
        }
        return sanitized;
    }
}
