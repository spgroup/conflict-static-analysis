package br.unb.cic.analysis.samples.ioa;

import java.util.LinkedHashMap;
import java.util.Map;

// Conflict: [left, m():11] --> [right, m():12]
public class ChainedMethodCallsConflictSample {

    public void m() {

        Settings settings = Settings.settingsBuilder()
                .loadFromStream("json", "getClass().getResourceAsStream(json)") // RIGHT
                .put("path.home", "createHome()") // LEFT
                .build();
    }

    public static final class Settings {

        private Map<String, String> settings;

        Settings(Map<String, String> settings) {
            this.settings = settings;
        }

        public static Builder settingsBuilder() {
            return new Builder();
        }

        public static class Builder {


            private final Map<String, String> map = new LinkedHashMap<>();

            public Builder loadFromStream(String resourceName, String is) {

                Map<String, String> loadedSettings = new LinkedHashMap<>();
                loadedSettings.put(resourceName, is);
                put(loadedSettings);

                return this;
            }

            public Builder put(Object... settings) {
                Map<String, String> loadedSettings = new LinkedHashMap<>();
                for (int i = 0; i < settings.length; i++) {
                    loadedSettings.put(settings[i].toString(), settings[i].toString());
                }
                put(loadedSettings);
                return this;
            }

            public Builder put(Map<String, String> loadedSettings) {
                map.putAll(loadedSettings);
                return this;
            }

            public Settings build() {
                return new Settings(map);
            }


        }
    }
}