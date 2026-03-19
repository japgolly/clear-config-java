package japgolly.clearconfig;

import java.util.Map;

public interface ConfigSource {

    public String name();
    public String get(String key);

    public static ConfigSource manual(String name, Map<String, String> map) {
        return new ConfigSource() {

            @Override
            public String name() {
                return name;
            }

            @Override
            public String get(String key) {
                return map.get(key);
            }
        };
    }
}
