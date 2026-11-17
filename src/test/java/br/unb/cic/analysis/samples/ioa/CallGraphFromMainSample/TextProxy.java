package br.unb.cic.analysis.samples.ioa.CallGraphFromMainSample;

import br.unb.cic.analysis.samples.ioa.PointsToDifferentMethodsSample.Ox;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TextProxy {
    private Report r;
    private Ox ox;

    TextProxy() {

        try {
            this.ox = new Ox();
            Report realReport = new ReportAdvanced();// (Report) clazz.getDeclaredConstructor().newInstance();
            r = (Report) Proxy.newProxyInstance(
                    Report.class.getClassLoader(),
                    new Class<?>[]{Report.class},
                    new RestHandler(realReport)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void generateReport() {
        r.countDupWords(); // LEFT
        r.countComments();
        r.countDupWhiteSpace(); // RIGHT
        ox.x = 0;
    }

    private static class RestHandler implements InvocationHandler {
        private final Report realReport;

        public RestHandler(Report realReport) {
            this.realReport = realReport;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("Interceptando chamada ao método: " + method.getName());
            return method.invoke(realReport, args); // Encaminha chamada ao objeto real
        }
    }
}
