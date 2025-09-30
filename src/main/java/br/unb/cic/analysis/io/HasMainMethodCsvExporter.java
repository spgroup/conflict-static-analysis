package br.unb.cic.analysis.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HasMainMethodCsvExporter {

    public void export(boolean hasMainMethod, String filePath) {
        File file = new File(filePath);
        boolean isNewFile = !file.exists() || file.length() == 0;

        try (FileWriter writer = new FileWriter(file, true)) {
            // Header
            if (isNewFile) {
                writer.append("hasMainMethod;\n");
            }
            // Data );rows
            writer.append(String.valueOf(hasMainMethod)).append(";").append("\n");

            writer.flush();
            System.out.println("CSV file generated successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
