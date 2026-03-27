package japgolly.clearconfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A source of configuration data, typically representing a file, environment variables, or system properties.
 *
 * Each source has a name (for reporting) and a set of key-value pairs.
 */
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

    /**
     * Returns all key-value pairs in this source.
     */
    public Map<String, String> all() {
        return all;
    }

    /**
     * Retrieves the value for the given key, or null if not found.
     */
    public String get(String key) {
        return all().get(key);
    }

    /**
     * Returns a new ConfigSource that applies the given transformation to all keys looked up.
     */
    public ConfigSource mapKeyQueries(Function<String, String> f) {
        var self = this;
        return new ConfigSource(name, all) {
            @Override
            public String get(String key) {
                return self.get(f.apply(key));
            }
        };
    }

    /**
     * Returns a new ConfigSource that applies the given transformation to all values retrieved.
     */
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

    /**
     * Returns a new ConfigSource that only contains keys matching the given predicate.
     */
    public ConfigSource filter(Predicate<String> f) {
        var allFiltered = all().entrySet().stream()
                            .filter(e -> f.test(e.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return ConfigSource.ofMap(name, allFiltered);
    }

    // =================================================================================================================

    /** Environment variables. */
    public static final ConfigSource Environment =
        ofMap("Environment", System.getenv());

    /** Java System Properties. */
    public static final ConfigSource SystemProps =
        ofProperties("System Properties", System.getProperties());

    public static ConfigSource empty(String name) {
        return ofMap(name, Map.of());
    }

    /** Creates a source from a Map. */
    public static ConfigSource ofMap(String name, Map<String, String> map) {
        return new ConfigSource(name, map);
    }

    /** Creates a source from Java Properties. */
    public static ConfigSource ofProperties(String name, Properties p) {
        var map = new HashMap<String, String>();
        for (String key : p.stringPropertyNames()) {
            map.put(key, p.getProperty(key));
        }
        return ofMap(name, map);
    }

    /** Creates a source from an InputStream containing Java Properties. */
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

    /** Creates a source from a Java Properties file on the classpath. */
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

    /** Creates a source from a Java Properties file. */
    public static ConfigSource ofPropFile(String filename, Boolean mandatory) throws IOException {
        return ofPropFile(new File(filename), mandatory);
    }

    /** Creates a source from a Java Properties file. */
    public static ConfigSource ofPropFile(File file, Boolean mandatory) throws IOException {
        final var name = file.getName();
        if (!file.exists()) {
            if (mandatory)
                throw new FileNotFoundException(file.getAbsolutePath());
            return empty(name);
        }
        final var is = new FileInputStream(file);
        return ofPropsFromInputStream(name, is, true);
    }

}
