package br.unb.cic.analysis.samples.ioa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggingConflictSample {
    private final Log logger = LogFactory.getLog(getClass());

    public void logAutoConfigurationReport() {
        this.logger.info("Error starting ApplicationContext.");

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Unable to provide auto-configuration report");
        }
    }

}
