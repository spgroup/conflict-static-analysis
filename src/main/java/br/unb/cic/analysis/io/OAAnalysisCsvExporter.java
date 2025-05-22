package br.unb.cic.analysis.io;

import br.unb.cic.analysis.model.OAAnalysisRecord;
import br.unb.cic.analysis.model.Statement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class OAAnalysisCsvExporter {

    public void export(List<OAAnalysisRecord> records, String filePath) {
        File file = new File(filePath);
        boolean isNewFile = !file.exists() || file.length() == 0;

        try (FileWriter writer = new FileWriter(file, true)) {
            // Cabeçalho
            if (isNewFile) {
                writer.append("DeepLimit;ClassName;MethodName;SourceCodeLine;TargetUnit;PossibleImplementations;CallGraphType;AnalysisType\n");
            }

            for (OAAnalysisRecord record : records) {
                Statement s = record.getStatement();

                writer.append(String.valueOf(record.getDeepLimit())).append(";");
                writer.append(s.getSootClass().getName().replace(";", " ")).append(";");
                writer.append(s.getSootMethod().getName().replace(";", " ")).append(";");
                writer.append(String.valueOf(s.getSourceCodeLineNumber())).append(";");
                writer.append(s.getUnit().toString().replace("\n", " ").replace("\r", " ").replace(";", " ")).append(";");
                writer.append(String.valueOf(record.getCallGraphEdgesSize())).append(";");
                writer.append(record.getCallGraphType().name()).append(";");
                writer.append(record.getAnalysisType().name()).append("\n");
            }

            writer.flush();
            System.out.println("OAAnalysis CSV export successful!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
