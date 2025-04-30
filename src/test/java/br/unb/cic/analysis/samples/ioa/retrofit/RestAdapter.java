package br.unb.cic.analysis.samples.ioa.retrofit;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RestAdapter {
    public interface Log {
        /**
         * Log a debug message to the appropriate console.
         */
        void log(String message);

        /**
         * A {@link Log} implementation which does not log anything.
         */
        Log NONE = new Log() {
            @Override
            public void log(String message) {
            }
        };
    }

    private final Log log;

    private RestAdapter(Log log) {
        this.log = log;
    }

    private Object logAndReplaceRequest() {
        log.log("Left");

        log.log("Right");
        return new Object();

    }

    public <T> T create(Class<T> service) {
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new RestHandler());
    }

    private class RestHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return invokeRequest();
        }
    }

    public Object invokeRequest() {
        return logAndReplaceRequest();
    }

    public static class Builder {

        private Log log;


        public Builder setLog(Log log) {
            this.log = log;
            return this;
        }

        public RestAdapter build() {
            ensureSaneDefaults();
            return new RestAdapter(log);
        }

        private void ensureSaneDefaults() {

            if (log == null) {
                log = Platform.get().defaultLog();
            }

        }
    }
}
