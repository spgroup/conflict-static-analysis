package br.unb.cic.analysis.samples.ioa;

public class LoggingConflictSample {
    private Logger logger = new Logger();

    public void logAutoConfigurationReport() {
        LoggingConflictSample l = new LoggingConflictSample();
        l.logger.info("Error starting ApplicationContext."); // LEFT

        if (l.logger.isDebugEnabled()) {
            l.logger.debug("Unable to provide auto-configuration report"); // RIGHT
        }
    }

}

class Logger {
    private String log;

    void info(String o) {
        this.log += o;
    }

    void debug(String o) {
        this.log += o;
    }

    boolean isDebugEnabled() {
        return true;
    }
}
