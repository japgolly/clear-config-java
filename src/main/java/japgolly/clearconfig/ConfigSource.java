package japgolly.clearconfig;

import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

public interface ConfigSource {

    public String name();

    public String get(String key);

    public default ConfigSource mapKeyQueries(Function<String, String> f) {
        var self = this;
        return new ConfigSource() {

            @Override
            public String name() {
                return self.name();
            }

            @Override
            public String get(String key) {
                return self.get(f.apply(key));
            }
        };
    }

    // =================================================================================================================

    public static final ConfigSource environment =
        ofMap("Environment", System.getenv());

    public static final ConfigSource systemProps =
        ofProperties("System Properties", System.getProperties());

    public static ConfigSource ofMap(String name, Map<String, String> map) {
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

    public static ConfigSource ofProperties(String name, Properties p) {
        return new ConfigSource() {

            @Override
            public String name() {
                return name;
            }

            @Override
            public String get(String key) {
                return p.getProperty(key);
            }
        };
    }
}
