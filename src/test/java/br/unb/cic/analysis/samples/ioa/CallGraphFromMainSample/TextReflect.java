package br.unb.cic.analysis.samples.ioa.CallGraphFromMainSample;

import br.unb.cic.analysis.samples.ioa.PointsToDifferentMethodsSample.Ox;

public class TextReflect {
    private Report r;
    private Ox ox;

    TextReflect() {
        try {
            //r = new ReportSimple();
            Class<?> clazz = Class.forName("br.unb.cic.analysis.samples.ioa.CallGraphFromMainSample.ReportAdvanced");
            r = (Report) clazz.getDeclaredConstructor().newInstance();
            Class<?> clazzOx = Class.forName("br.unb.cic.analysis.samples.ioa.PointsToDifferentMethodsSample.Ox");
            ox = (Ox) clazz.getDeclaredConstructor().newInstance();
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
}
/*
 r.getClass().getMethod("countDupWords").invoke(r);  // LEFT
            r.getClass().getMethod("countComments").invoke(r);
            r.getClass().getMethod("countDupWhiteSpace").invoke(r);  // RIGHT
            ox.getClass().getDeclaredField("x").setInt(ox, 0);
 */