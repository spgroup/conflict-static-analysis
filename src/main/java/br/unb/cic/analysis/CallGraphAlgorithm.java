package br.unb.cic.analysis;

public enum CallGraphAlgorithm {
    CHA,
    RTA,
    VTA,
    SPARK;

    public static CallGraphAlgorithm fromString(String value) {
        try {
            return CallGraphAlgorithm.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "Invalid call graph algorithm: " + value + ". Allowed values: CHA, RTA, VTA, SPARK"
            );
        }
    }
}
