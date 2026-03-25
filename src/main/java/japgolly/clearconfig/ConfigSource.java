package japgolly.clearconfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

    public static ConfigSource empty(String name) {
        return ofMap(name, Map.of());
    }

    public static ConfigSource ofMap(String name, Map<String, String> map) {
        return new ConfigSource(name, map);
    }

    public static ConfigSource ofProperties(String name, Properties p) {
        var map = new HashMap<String, String>();
        for (String key : p.stringPropertyNames()) {
            map.put(key, p.getProperty(key));
        }
        return ofMap(name, map);
    }

    public static ConfigSource ofPropsFromInputStream(String name, InputStream is, Boolean close) throws IOException {
        try {
            var p = new Properties();
            p.load(is);
            return ofProperties(name, p);
        } finally {
            if (close)
                is.close();
        }
    }

    public static ConfigSource ofPropFileOnClasspath(String filename, Boolean mandatory) throws IOException {
        filename = filename.replaceFirst("^/*", "/");
        final var name = "cp:" + filename;
        final var is = ConfigSource.class.getResourceAsStream(filename);
        if (is != null)
            return ofPropsFromInputStream(name, is, true);
        if (mandatory)
            throw new FileNotFoundException(filename);
        return empty(name);
    }

}
