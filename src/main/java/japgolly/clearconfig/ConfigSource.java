package japgolly.clearconfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConfigSource {
    private final String name;
    private final Map<String, String> all;

    public ConfigSource(String name, Map<String, String> all) {
        this.name = name;
        this.all = all;
    }

    public String name() {
        return name;
    }

    public Map<String, String> all() {
        return all;
    }

    public String get(String key) {
        return all().get(key);
    }

    public ConfigSource mapKeyQueries(Function<String, String> f) {
        var self = this;
        return new ConfigSource(name, all) {
            @Override
            public String get(String key) {
                return self.get(f.apply(key));
            }
        };
    }

    public ConfigSource mapValues(Function<String, String> f) {
        var self = this;
        return new ConfigSource(name, all) {
            @Override
            public String get(String key) {
                var value = self.get(key);
                return value == null ? null : f.apply(value);
            }
        };
    }

    public ConfigSource filter(Predicate<String> f) {
        var allFiltered = all().entrySet().stream()
                            .filter(e -> f.test(e.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return ConfigSource.ofMap(name, allFiltered);
    }

    // =================================================================================================================

    public static final ConfigSource Environment =
        ofMap("Environment", System.getenv());

    public static final ConfigSource SystemProps =
        ofProperties("System Properties", System.getProperties());

    public static ConfigSource ofMap(String name, Map<String, String> map) {
        return new ConfigSource(name, map);
    }

    public static ConfigSource ofProperties(String name, Properties p) {
        var map = new HashMap<String, String>();
        p.putAll(map);
        return ofMap(name, map);
    }
}
