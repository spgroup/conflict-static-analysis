package br.unb.cic.analysis.io;

import br.unb.cic.analysis.model.Statement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PANotResolveCsvExporter {

    public void export(List<Statement> statements, String filePath) {
        File file = new File(filePath);
        boolean isNewFile = !file.exists() || file.length() == 0;

        try (FileWriter writer = new FileWriter(file, true)) {
            // Header
            if (isNewFile) {
                writer.append("ClassName;IsAbstractClass;IsFinalClass;IsStaticClass;IsInnerClass;IsPhantomClass;IsInterface;IsConcreteClass;IsEnumClass;")
                        .append("MethodName;IsAbstractMethod;IsFinalMethod;IsStaticMethod;IsConstructor;IsPhantomMethod;IsEntryMethod;IsConcreteMethod;hasActiveBody;")
                        .append("UnitStatement;SourceCodeLineNumber;IsAssign;IsInvoke;").append("TraversedLines\n");
            }
            // Data rows
            for (Statement s : statements) {
                writer.append(s.getSootClass().getName().replace(";", " ")).append(";");
                writer.append(String.valueOf(s.getSootClass().isAbstract())).append(";");
                writer.append(String.valueOf(s.getSootClass().isFinal())).append(";");
                writer.append(String.valueOf(s.getSootClass().isStatic())).append(";");
                writer.append(String.valueOf(s.getSootClass().isInnerClass())).append(";");
                writer.append(String.valueOf(s.getSootClass().isPhantom())).append(";");
                writer.append(String.valueOf(s.getSootClass().isInterface())).append(";");
                writer.append(String.valueOf(s.getSootClass().isConcrete())).append(";");
                writer.append(String.valueOf(s.getSootClass().isEnum())).append(";");

                writer.append(s.getSootMethod().getDeclaration().replace(";", " ")).append(";");
                writer.append(String.valueOf(s.getSootMethod().isAbstract())).append(";");
                writer.append(String.valueOf(s.getSootMethod().isFinal())).append(";");
                writer.append(String.valueOf(s.getSootMethod().isStatic())).append(";");
                writer.append(String.valueOf(s.getSootMethod().isConstructor())).append(";");
                writer.append(String.valueOf(s.getSootMethod().isPhantom())).append(";");
                writer.append(String.valueOf(s.getSootMethod().isEntryMethod())).append(";");
                writer.append(String.valueOf(s.getSootMethod().isConcrete())).append(";");
                writer.append(String.valueOf(s.getSootMethod().hasActiveBody())).append(";");

                writer.append(s.getUnit().toString().replace("\n", " ").replace("\r", " ").replace(";", " ")).append(";");
                writer.append(s.getSourceCodeLineNumber().toString()).append(";");
                writer.append(String.valueOf(s.isAssign())).append(";");
                writer.append(String.valueOf(s.isInvoke())).append(";");
                writer.append(String.valueOf(s.getTraversedLine())).append("\n");
            }

            writer.flush();
            System.out.println("PA Not Resolve CSV file generated successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
