package br.unb.cic.analysis.io;

import br.unb.cic.analysis.model.OAAnalysisRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class OAAnalysisCsvExporter {

    public void export(List<OAAnalysisRecord> records, String filePath) {
        File file = new File(filePath);
        boolean isNewFile = !file.exists() || file.length() == 0;

        try (FileWriter writer = new FileWriter(file, true)) {
            // Cabeçalho
            if (isNewFile) {
                writer.append("CallGraphAlgorithm;CallGraphEdgeCount;DepthLimit;VisitedMethodsCount;AnalysisType;CallGraphBuildTimeMs;AnalysisExecutionTimeMs;CallGraphEntryPointsCount;AnalysisEntryPointsCount;CallGraphEntryPoints;AnalysisEntryPoints;\n");
            }

            for (OAAnalysisRecord record : records) {
                writeRecord(writer, record);
            }

            writer.flush();
            System.out.println("OAAnalysis CSV export successful!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void export(OAAnalysisRecord record, String filePath) {
        export(Collections.singletonList(record), filePath);
    }

    private void writeRecord(FileWriter writer, OAAnalysisRecord record) throws IOException {
        writer.append(record.getCallGraphAlgorithm() != null ? record.getCallGraphAlgorithm().name() : "").append(";");
        writer.append(String.valueOf(record.getCallGraphEdgeCount())).append(";");
        writer.append(String.valueOf(record.getDepthLimit())).append(";");
        writer.append(String.valueOf(record.getVisitedMethodsCount())).append(";");
        writer.append(record.getAnalysisType() != null ? record.getAnalysisType().name() : "").append(";");
        writer.append(String.valueOf(record.getCallGraphBuildTimeMs().get("cg"))).append(";");
        writer.append(String.valueOf(record.getAnalysisExecutionTimeMs())).append(";");
        writer.append(String.valueOf(record.getCallGraphEntryPoint().size())).append(";");
        writer.append(String.valueOf(record.getAnalysisEntryPoint().size())).append(";");
        writer.append(record.getCallGraphEntryPoint().toString()).append(";");
        writer.append(record.getAnalysisEntryPoint().toString()).append("\n");
    }
}
