package br.unb.cic.analysis.model;

import br.unb.cic.analysis.CallGraphAlgorithm;
import soot.SootMethod;

import java.util.List;
import java.util.Map;

public class AnalysisRecord {

    private static AnalysisRecord instance;

    private List<SootMethod> callGraphEntryPoint;
    private List<SootMethod> analysisEntryPoint;
    private CallGraphAlgorithm callGraphAlgorithm;
    private int callGraphEdgeCount;
    private int depthLimit;
    private int visitedMethodsCount;
    private Map<String, Long> callGraphBuildTimeMs;
    private long analysisExecutionTimeMs;
    private long usedMemoryMb;

    private AnalysisRecord() {
    }

    public static AnalysisRecord getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AnalysisRecord ainda não foi inicializado. Use o Builder primeiro.");
        }
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }

    public static class Builder {
        private List<SootMethod> callGraphEntryPoint;
        private List<SootMethod> analysisEntryPoint;
        private CallGraphAlgorithm callGraphAlgorithm;
        private int callGraphEdgeCount;
        private int depthLimit;
        private int visitedMethodsCount;
        private Map<String, Long> callGraphBuildTimeMs;
        private long analysisExecutionTimeMs;

        public Builder callGraphEntryPoint(List<SootMethod> value) {
            this.callGraphEntryPoint = value;
            return this;
        }

        public Builder analysisEntryPoint(List<SootMethod> value) {
            this.analysisEntryPoint = value;
            return this;
        }

        public Builder callGraphAlgorithm(CallGraphAlgorithm value) {
            this.callGraphAlgorithm = value;
            return this;
        }

        public Builder callGraphEdgeCount(int value) {
            this.callGraphEdgeCount = value;
            return this;
        }

        public Builder depthLimit(int value) {
            this.depthLimit = value;
            return this;
        }

        public Builder visitedMethodsCount(int value) {
            this.visitedMethodsCount = value;
            return this;
        }

        public Builder callGraphBuildTimeMs(Map<String, Long> value) {
            this.callGraphBuildTimeMs = value;
            return this;
        }

        public Builder analysisExecutionTimeMs(long value) {
            this.analysisExecutionTimeMs = value;
            return this;
        }

        public AnalysisRecord build() {
            if (instance == null) {
                instance = new AnalysisRecord();
                instance.callGraphEntryPoint = this.callGraphEntryPoint;
                instance.analysisEntryPoint = this.analysisEntryPoint;
                instance.callGraphAlgorithm = this.callGraphAlgorithm;
                instance.callGraphEdgeCount = this.callGraphEdgeCount;
                instance.depthLimit = this.depthLimit;
                instance.visitedMethodsCount = this.visitedMethodsCount;
                instance.callGraphBuildTimeMs = this.callGraphBuildTimeMs;
                instance.analysisExecutionTimeMs = this.analysisExecutionTimeMs;

                Runtime runtime = Runtime.getRuntime();
                instance.usedMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            }
            return instance;
        }
    }

    public List<SootMethod> getCallGraphEntryPoint() {
        return callGraphEntryPoint;
    }

    public List<SootMethod> getAnalysisEntryPoint() {
        return analysisEntryPoint;
    }

    public CallGraphAlgorithm getCallGraphAlgorithm() {
        return callGraphAlgorithm;
    }

    public int getCallGraphEdgeCount() {
        return callGraphEdgeCount;
    }

    public int getDepthLimit() {
        return depthLimit;
    }

    public int getVisitedMethodsCount() {
        return visitedMethodsCount;
    }

    public Map<String, Long> getCallGraphBuildTimeMs() {
        return callGraphBuildTimeMs;
    }

    public long getAnalysisExecutionTimeMs() {
        return analysisExecutionTimeMs;
    }

    public void setAnalysisExecutionTimeMs(long time) {
        this.analysisExecutionTimeMs = time;
    }

    public long getUsedMemoryMb() {
        return usedMemoryMb;
    }

    public void setCallGraphBuildTimeMs(Map<String, Long> callGraphBuildTimeMs) {
        this.callGraphBuildTimeMs = callGraphBuildTimeMs;
    }

    public void setCallGraphAlgorithm(CallGraphAlgorithm callGraphAlgorithm) {
        this.callGraphAlgorithm = callGraphAlgorithm;
    }

    public void setCallGraphEdgeCount(int callGraphEdgeCount) {
        this.callGraphEdgeCount = callGraphEdgeCount;
    }

    public void setUsedMemoryMb(long usedMemoryMb) {
        this.usedMemoryMb = usedMemoryMb;
    }
}
