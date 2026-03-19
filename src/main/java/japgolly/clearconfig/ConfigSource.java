package japgolly.clearconfig;

import java.util.Map;
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

    public static ConfigSource environment = new ConfigSource() {

        @Override
        public String name() {
            return "Environment";
        }

        @Override
        public String get(String key) {
            return System.getenv(key);
        }
    };

    public static ConfigSource systemProps = new ConfigSource() {

        @Override
        public String name() {
            return "System Properties";
        }

        @Override
        public String get(String key) {
            return System.getProperty(key);
        }
    };

}
