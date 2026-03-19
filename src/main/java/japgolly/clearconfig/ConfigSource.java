package japgolly.clearconfig;

import java.util.Map;

public interface ConfigSource {

    public String name();
    public Map<String, String> allConfig();

    public default String get(String key) {
        return allConfig().get(key);
    }

    public static ConfigSource manual(String name, Map<String, String> map) {
        return new ConfigSource() {

            @Override
            public String name() {
                return name;
            }

            @Override
            public Map<String, String> allConfig() {
                return map;
            }
        };
    }
}
