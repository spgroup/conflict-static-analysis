package br.unb.cic.analysis.samples;

import java.util.HashMap;
import java.util.Map;

public class DFPMotivating {
    private static final int KRYO_OFFSET = 40;
    private static final int RESERVED_ID_OFFSET = 256;

    public static void main(String[] args) {
        DFPMotivating aux = new DFPMotivating();
        Serializer serializer = new KryoSerializer();
        aux.initialize(serializer);
    }

    public void initialize(Serializer serializer) { // Serializer Serializer1 KryoSerializer
        // 31
        serializer.registerClass(float[].class, KRYO_OFFSET + 30); //left

        // 52 a 55
        serializer.registerClass(Parameter.class, KRYO_OFFSET + 33); //right
        serializer.registerClass(Parameter[].class, KRYO_OFFSET + 34); //right
        serializer.registerClass(IndexParameters.class, KRYO_OFFSET + 35); //right
        serializer.registerClass(IndexParameters[].class, KRYO_OFFSET + 36); //right

    }

    public static class Parameter {}
    public static class IndexParameters {}

}

interface AttributeHandling {}

interface Serializer extends AttributeHandling {
    <T> void registerClass(Class<T> type, int id);
}

class KryoSerializer extends DefaultAttributeHandling implements Serializer {
    public Map<Class<?>, Integer> registrations = new HashMap<>();

    @Override
    public <T> void registerClass(Class<T> type, int id) {
        registrations.put(type, id);
    }
}

class DefaultAttributeHandling implements AttributeHandling {}