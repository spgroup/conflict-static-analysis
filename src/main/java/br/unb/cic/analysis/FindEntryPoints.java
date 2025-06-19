package br.unb.cic.analysis;

import scala.collection.JavaConverters;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import java.util.ArrayList;
import java.util.List;

public class FindEntryPoints {

    public static scala.collection.immutable.List<SootMethod> getEntryMethods() {
        return getCallgraphEntryPoints();
    }

    public static scala.collection.immutable.List<SootMethod> getCallgraphEntryPoints() {
        List<SootMethod> mainMethods = findMainMethods();

        if (mainMethods.isEmpty()) {
            mainMethods = findPublicMethods();
        }
        return JavaConverters.asScalaBuffer(mainMethods).toList();
    }

    public static List<SootMethod> findMainMethods() {
        List<SootMethod> mainMethods = new ArrayList<>();

        for (SootClass sootClass : Scene.v().getApplicationClasses()) {
            for (SootMethod method : sootClass.getMethods()) {
                if (isMainMethod(method)) {
                    mainMethods.add(method);
                }
            }
        }

        return mainMethods;
    }

    private static boolean isMainMethod(SootMethod method) {
        return method.getName().equals("main")
                && method.isStatic()
                && method.getReturnType().toString().equals("void")
                && method.getParameterCount() == 1
                && method.getParameterType(0).toString().equals("java.lang.String[]")
                && method.getDeclaringClass().toString().equals("br.unb.cic.analysis.samples.teste.Main");
    }

    public static List<SootMethod> findPublicMethods() {
        List<SootMethod> publicMethods = new ArrayList<>();

        for (SootClass sootClass : Scene.v().getApplicationClasses()) {
            for (SootMethod method : sootClass.getMethods()) {
                publicMethods.add(method);
            }
        }

        return publicMethods;
    }
}
