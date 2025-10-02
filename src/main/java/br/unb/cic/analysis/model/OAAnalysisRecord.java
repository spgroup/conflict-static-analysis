package br.unb.cic.analysis.model;

import br.unb.cic.analysis.CallGraphAlgorithm;
import br.unb.cic.analysis.Main;
import soot.SootMethod;

import java.util.List;
import java.util.Map;

public class OAAnalysisRecord {

    private static OAAnalysisRecord instance;

    private List<SootMethod> callGraphEntryPoint;
    private List<SootMethod> analysisEntryPoint;
    private CallGraphAlgorithm callGraphAlgorithm;
    private int callGraphEdgeCount;
    private int depthLimit;
    private int visitedMethodsCount;
    private Main.AnalysisType analysisType;
    private Map<String, Long> callGraphBuildTimeMs;
    private long analysisExecutionTimeMs;

    private OAAnalysisRecord() {
    }

    public static OAAnalysisRecord getInstance() {
        if (instance == null) {
            throw new IllegalStateException("OAAnalysisRecord ainda não foi inicializado. Use o Builder primeiro.");
        }
        return instance;
    }

    public static class Builder {
        private List<SootMethod> callGraphEntryPoint;
        private List<SootMethod> analysisEntryPoint;
        private CallGraphAlgorithm callGraphAlgorithm;
        private int callGraphEdgeCount;
        private int depthLimit;
        private int visitedMethodsCount;
        private Main.AnalysisType analysisType;
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

        public Builder analysisType(Main.AnalysisType value) {
            this.analysisType = value;
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

        public OAAnalysisRecord build() {
            if (instance == null) {
                instance = new OAAnalysisRecord();
                instance.callGraphEntryPoint = this.callGraphEntryPoint;
                instance.analysisEntryPoint = this.analysisEntryPoint;
                instance.callGraphAlgorithm = this.callGraphAlgorithm;
                instance.callGraphEdgeCount = this.callGraphEdgeCount;
                instance.depthLimit = this.depthLimit;
                instance.visitedMethodsCount = this.visitedMethodsCount;
                instance.analysisType = this.analysisType;
                instance.callGraphBuildTimeMs = this.callGraphBuildTimeMs;
                instance.analysisExecutionTimeMs = this.analysisExecutionTimeMs;
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

    public Main.AnalysisType getAnalysisType() {
        return analysisType;
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
}
