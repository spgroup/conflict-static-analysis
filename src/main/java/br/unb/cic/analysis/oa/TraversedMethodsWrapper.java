package br.unb.cic.analysis.oa;

import soot.SootClass;
import soot.SootMethod;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TraversedMethodsWrapper<E> {

    private final List<E> traversedMethods;
    private final Map<String, Integer> allVisitedMethodSignatures = new LinkedHashMap<>();
    private final Map<String, Set<SootClass>> ancestorCache = new HashMap<>();
    private int visitedMethodsCount = 0;

    private int missedAncestorEvents = 0;
    private int totalExtraAncestors = 0;

    public int getMissedAncestorEvents() {
        return missedAncestorEvents;
    }

    public int getTotalExtraAncestors() {
        return totalExtraAncestors;
    }

    public TraversedMethodsWrapper() {
        this.traversedMethods = new ArrayList<>();
    }

    public int size() {
        return traversedMethods.size();
    }

    public boolean isEmpty() {
        return traversedMethods.isEmpty();
    }

    public boolean contains(E element) {
        return traversedMethods.contains(element);
    }

    public void add(E element) {
        this.traversedMethods.add(element);
        this.visitedMethodsCount++;
        if (element instanceof SootMethod) {
            String signature = ((SootMethod) element).getSignature();
            boolean wasAdded = !allVisitedMethodSignatures.containsKey(signature);
            if (wasAdded) {
                allVisitedMethodSignatures.put(signature, this.traversedMethods.size());
            }
            // Salva no disco a cada 100 novos métodos únicos encontrados
            if (wasAdded && allVisitedMethodSignatures.size() % 100 == 0) {
                dumpVisitedMethodsToFile();
                dumpDiagnosticsToFile();
            }
        }
    }

    private void dumpVisitedMethodsToFile() {
        try {
            List<String> lines = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : allVisitedMethodSignatures.entrySet()) {
                lines.add(entry.getKey() + " - " + entry.getValue());
            }
            Files.write(Paths.get("all_visited_methods.txt"), lines);
        } catch (Exception e) {
            // Ignora falhas silenciosamente para não interromper a análise
        }
    }

    public void dumpDiagnosticsToFile() {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("[DIAG] Total de metodos analisados: " + visitedMethodsCount);
            lines.add("[DIAG] Metodos unicos visitados: " + allVisitedMethodSignatures.size());
            lines.add("[DIAG] Eventos de ancestrais perdidos: " + missedAncestorEvents);
            lines.add("[DIAG] Total de classes extras perdidas: " + totalExtraAncestors);
            Files.write(Paths.get("oa_diagnostics.txt"), lines);
        } catch (Exception e) {
            // Ignora falhas
        }
    }

    public Set<String> getAllVisitedMethodSignatures() {
        return allVisitedMethodSignatures.keySet();
    }

    public int getVisitedMethodsCount() {
        return visitedMethodsCount;
    }

    public void remove(E element) {
        traversedMethods.remove(element);
    }

    public E get(int index) {
        return traversedMethods.get(index);
    }

    public List<E> getTraversedMethods() {
        return traversedMethods;
    }

    public boolean hasRelativeBeenTraversed(SootMethod method) {
        if (traversedMethods.contains(method)) return true;
        return hasTraversedMethodWithCommonSuperclassAndSignature(method);
    }

    private boolean haveCommonAncestorClass(SootMethod method1, SootMethod method2) {
        Set<SootClass> ancestors1 = getAncestors(method1);
        Set<SootClass> ancestors2 = getAncestors(method2);

        for (SootClass ancestor1 : ancestors1) {
            for (SootClass ancestor2 : ancestors2) {
                SootMethod ancestorMethod1 = ancestor1.getMethod(method1.getName(), method1.getParameterTypes());
                SootMethod ancestorMethod2 = ancestor2.getMethod(method2.getName(), method2.getParameterTypes());
                if (ancestorMethod1 == ancestorMethod2) {
                    return true;
                }
            }
        }

        return false;
    }

    private Set<SootClass> getAncestors(SootMethod method) {
        String cacheKey = method.getDeclaringClass().getName() + "#" + method.getSubSignature();
        Set<SootClass> cached = ancestorCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Set<SootClass> ancestors = new HashSet<>();
        SootClass sootClass = method.getDeclaringClass();
        ancestors.add(sootClass);
        getSuperclasses(sootClass, ancestors);
        getInterfaceAncestors(ancestors);
        Set<SootClass> result = getAncestorsWithMethod(method, ancestors);

        ancestorCache.put(cacheKey, result);
        return result;
    }

    private void getSuperclasses(SootClass sootClass, Set<SootClass> ancestors) {
        while (sootClass.hasSuperclass()) {
            sootClass = sootClass.getSuperclass();
            ancestors.add(sootClass);
        }
    }

    private void getInterfaceAncestors(Set<SootClass> ancestors) {
        Set<SootClass> toProcess = new HashSet<>(ancestors);
        int discoveredCount = 0;
        while (!toProcess.isEmpty()) {
            Set<SootClass> newInterfaces = new HashSet<>();
            for (SootClass cls : toProcess) {
                for (SootClass iface : cls.getInterfaces()) {
                    if (ancestors.add(iface)) {
                        newInterfaces.add(iface);
                        discoveredCount++;
                    }
                }
            }
            toProcess = newInterfaces;
        }
        if (discoveredCount > 0) {
            missedAncestorEvents++;
            totalExtraAncestors += discoveredCount;
        }
    }

    private Set<SootClass> getAncestorsWithMethod(SootMethod method, Set<SootClass> ancestors) {
        Set<SootClass> ancestorsWithMethod = new HashSet<>();
        for (SootClass ancestor : ancestors) {
            try {
                SootMethod ancestorMethod = ancestor.getMethod(method.getName(), method.getParameterTypes());
                if (ancestorMethod != null) {
                    ancestorsWithMethod.add(ancestor);
                }
            } catch (RuntimeException e) {
                // ignore
            }
        }
        return ancestorsWithMethod;
    }

    private boolean hasTraversedMethodWithCommonSuperclassAndSignature(SootMethod method) {
        for (E traversedMethod : this.getTraversedMethods()) {
            if (haveCommonAncestorClass(method, (SootMethod) traversedMethod)) {
                return true;
            }
        }
        return false;
    }

}
