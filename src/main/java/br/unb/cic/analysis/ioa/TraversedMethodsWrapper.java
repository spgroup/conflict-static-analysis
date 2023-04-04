package br.unb.cic.analysis.ioa;

import soot.SootClass;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TraversedMethodsWrapper<E> {

    private List<E> traversedMethods;

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
        traversedMethods.add(element);
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
        Set<SootClass> ancestors = new HashSet<>();
        SootClass sootClass = method.getDeclaringClass();
        ancestors.add(sootClass);
        getSuperclasses(sootClass, ancestors);
        getInterfaceAncestors(ancestors);
        return getAncestorsWithMethod(method, ancestors);
    }

    private void getSuperclasses(SootClass sootClass, Set<SootClass> ancestors) {
        while (sootClass.hasSuperclass()) {
            sootClass = sootClass.getSuperclass();
            ancestors.add(sootClass);
        }
    }

    private void getInterfaceAncestors(Set<SootClass> ancestors) {
        Set<SootClass> newAncestors = new HashSet<>();
        for (SootClass interfaceClass : ancestors) {
            for (SootClass interfaceAncestor : interfaceClass.getInterfaces()) {
                newAncestors.add(interfaceAncestor);
            }
        }
        ancestors.addAll(newAncestors);
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
                Logger.getLogger(
                        TraversedMethodsWrapper.class.getName()).log(Level.INFO, e.getMessage());
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
